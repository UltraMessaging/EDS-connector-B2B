/**
 * 
 */
package com.infa.vds.custom.sources;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.ProjectSubscriptionName;

/**
 * @author pyoung & rclemente
 *
 */
class Google_PubSub_Thread implements Runnable {

	//static final short Number_Connections = 1;
	//static final short Number_Disconnections = 2;

	Logger _logger = LoggerFactory.getLogger(Google_PubSub_source.class);
	GooglePubSubSourceMessageReceiver gMsgReceiver;
	Google_PubSub_Shared config;

	//Thread Constructor
	Google_PubSub_Thread(Google_PubSub_Shared sourceConfig) {
		config = sourceConfig;
	}
	static class GooglePubSubSourceMessageReceiver implements MessageReceiver {

		BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();
		static Logger _logger;
		static void initLogger(Logger l) {
			_logger = l;
		}

		@Override
		public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
			boolean retval = messages.offer(message);
			_logger.debug("messages.offer returned " + retval);
			if (retval) {
				consumer.ack();
			}
		}
	}
	public void run() {

		GooglePubSubSourceMessageReceiver.initLogger(_logger);
		//com.google.common.base.Preconditions.checkArgument(true);
		//Start the source and pause for a moment [[Initial Pause (any reason why? )]]
		try {
			Thread.sleep(config.INITIAL_SLEEP_INTERVAL); 
		} catch (Exception e) {
			_logger.error("Initial sleep for " + config.INITIAL_SLEEP_INTERVAL, e);
		}
		
			// create a subscriber bound to the asynchronous message receiver
			config.ProjectSubscriptionName = ProjectSubscriptionName.of(config.projectId, config.subscriptionId);

			gMsgReceiver = new GooglePubSubSourceMessageReceiver();
			_logger.info("Calling newBuilder");
		try {
			config.subscriber = Subscriber.newBuilder(config.ProjectSubscriptionName, gMsgReceiver).build();
		} catch (Exception e) {
			_logger.error("Failed Subscriber.newBuilder", e);
			config.closeSource = true;
			return;
		}
			_logger.info("Calling awaitRunning");
			config.subscriber.startAsync().awaitRunning();
			_logger.info("Google Pub Sub Source opened for reading!");
		
		//start reading the from the source
		while (!config.closeSource) {
			try
			{
		        PubsubMessage message = gMsgReceiver.messages.take();
		        _logger.debug("return from MessageReceiver");

		        byte[] payloadBytes = message.getData().toByteArray();
		        int datalen = ((payloadBytes == null) ? 0: payloadBytes.length);
				_logger.info("Message Id: " + message.getMessageId());
				_logger.info("Data payload length of " + datalen + " bytes");
	
				//Add to event queue for VDS to process down stream
				if (datalen != 0 && config.msgPayloadOnly) 
				{
					_logger.info("Adding event of " + datalen + " bytes");
					config.eventQueue.add(payloadBytes);
				}else if (datalen != 0 && !config.msgPayloadOnly)
				{
					//Build JSON
					Map<String,String> fullMsg = message.getAttributesMap();
					//int mapSize = fullMsg.size();
					//String jsonString = "";
					StringBuilder sb = new StringBuilder();
					sb.append("{");
					for (Map.Entry<String,String> entry : fullMsg.entrySet())
						sb.append("\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"," + System.getProperty("line.separator"));
					
					String pubsubTimestampString = null;
				    Timestamp timestampProto = message.getPublishTime();
				    if (timestampProto != null) {
				      pubsubTimestampString = String.valueOf(timestampProto.getSeconds()
				                                             + timestampProto.getNanos() / 1000L);
				    }
				    					
					sb.append("\"PublishTime\": \"");
					sb.append(pubsubTimestampString);
					sb.append("\"," + System.getProperty("line.separator"));
					sb.append("\"data\":\"");
					sb.append(message.getData().toStringUtf8());
					//Close the JSON
					sb.append("\"}");
					
					
					//Add to event queue for EDS to process down stream 
					config.eventQueue.add(sb.toString().getBytes());
				}
			}catch(Exception e) {
				_logger.error("Error reading messages ",e);
				config.closeSource = true;
			}
			try {
				Thread.sleep(1);
			} catch (Exception e) {
			}
		}
		_logger.info("Exiting Google_PubSub_Thread.run()");
	}
}
