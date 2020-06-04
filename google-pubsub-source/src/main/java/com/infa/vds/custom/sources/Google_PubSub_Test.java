package com.infa.vds.custom.sources;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

//import com.google.cloud.Timestamp;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.net.*;

public class Google_PubSub_Test {
	private static Logger _logger = LoggerFactory.getLogger(Google_PubSub_Test.class);
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

	static boolean test = true;

	public static void main(String[] args) throws Exception {
		// int retries;
		// String strContents;
		// int statusCode;
		Google_PubSub_Shared config = new Google_PubSub_Shared();
		GooglePubSubSourceMessageReceiver gMsgReceiver;
		config.projectId = "testproject-1518896311922";
		config.subscriptionId = "MySub";
		try {
			// create a subscriber bound to the asynchronous message receiver
			config.ProjectSubscriptionName = ProjectSubscriptionName.of(config.projectId, config.subscriptionId);

			GooglePubSubSourceMessageReceiver.initLogger(_logger);
			gMsgReceiver = new GooglePubSubSourceMessageReceiver();
			System.out.println("Calling newBuilder");
			config.subscriber = Subscriber.newBuilder(config.ProjectSubscriptionName, gMsgReceiver).build();
			System.out.println("Calling awaitRunning");
			config.subscriber.startAsync().awaitRunning();
			System.out.println("Google Pub Sub Source opened for reading!");
		} catch (Exception e) {
			e.printStackTrace();
			config.closeSource = true;
			return;
		}
		// start reading the from the source
		while (!config.closeSource) {
			try {
		        PubsubMessage message = gMsgReceiver.messages.take();
		        _logger.debug("return from MessageReceiver");

		        byte[] payloadBytes = message.getData().toByteArray();
		        int datalen = ((payloadBytes == null) ? 0: payloadBytes.length);
				_logger.info("Message Id: " + message.getMessageId());
				_logger.info("Data payload length of " + datalen + " bytes");
	
				config.msgPayloadOnly = false;
				//Add to event queue for VDS to process down stream
				if (datalen != 0 && config.msgPayloadOnly) 
				{
					_logger.info("Adding event of " + datalen + " bytes");
					//config.eventQueue.add(payloadBytes);
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
					//config.eventQueue.add(sb.toString().getBytes());
					_logger.info(sb.toString());
				}
			} catch (Exception e) {
				_logger.error("Error reading messages ", e );
				config.closeSource = true;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// Woke up for another reason
				// continue;
			}
		}
	}

}
