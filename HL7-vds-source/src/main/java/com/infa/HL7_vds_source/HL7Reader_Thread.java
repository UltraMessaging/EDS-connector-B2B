/**
 * 
 */
package com.infa.HL7_vds_source;
import java.io.BufferedInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author pyoung & rclemente
 *
 */
class HL7Reader_Thread implements Runnable {

	Logger _logger = LoggerFactory.getLogger(HL7Source.class);

	HL7Source_Config config;
	
	//TODO: Do we need to persist this across restarts some how?  Hopefully not...
	int ackSeqNum = 1;

	HL7Reader_Thread(HL7Source_Config sourceConfig) {
		config = sourceConfig;
	}

	public void run() {

		
		while (!config.closeSource) {
			

			String connectType = config.isListener ? "Listen" : "Connect";
			
			try {
				//If configured as Listener open server socket
				if (config.isListener) {
					_logger.info("Opening Server Socket, listening on: " + config.port);
					if (config.listener == null) {
						config.listener = new ServerSocket(config.port);
						config.listener.setSoTimeout(60000);
					}
					//Wait for a connection
					config.client = config.listener.accept();
					config.client.setTcpNoDelay(config.USE_NO_DELAY);
					
				} else {
					//TODO Do we need to add retry logic in here? Will this just sit and wait if unavailable? 
					_logger.info("Opening Client Socket, connecting to: " + config.host + ":" + config.port);
					config.client = new Socket(config.host, config.port);
				}
				config.bis = new BufferedInputStream(System.in);
			} catch (java.net.SocketTimeoutException ste) {
				_logger.info("Server Socket timed out - retrying...");
				continue;
			}
			catch (Exception e) {
				_logger.error(connectType, e);
				config.closeSource = true;
				break;
			}
			_logger.info("HL7 Source {} connected to {}", connectType, 
					config.client.getInetAddress().getHostName() + ":" + config.client.getPort());

			
			//Look for and pull HL7 messages off the socket
			getMessages();
		}
	}
	
	// TODO Improve the logic, add batching (if possible?)
	public void getMessages()
	{
		//check to make sure we are connected
		if (config.client != null) {

			try {
				//Try and grab the input stream from the socket 
				InputStream networkStream = config.client.getInputStream();

				// Create variables to manage while-loop & read HL7 messages off the socket
				int bytesRead;
				StringBuilder stringBuilder = new StringBuilder();
				boolean messageStarted = false;
				byte[] inBuf = new byte[HL7Source_Config.INPUT_BUFFER_SIZE];

				// read continuously until end of stream
				while ((bytesRead = networkStream.read(inBuf)) != -1) {
					_logger.info("read {} bytes", bytesRead);
					// Check each byte read, looking for start/end byte or message payload
					for (int i = 0; i < bytesRead; i++) {
						int inByte = inBuf[i];
						// Look for HL7 start byte
						if (!messageStarted) {
							if (inByte == config.startByte) {
								// When found set started to true, and off we go =)
								messageStarted = true;
							}
							// Once started, check for the end byte
						} else if (inByte != config.endByte) {
							
							// If not start or end byte keep building the message
							stringBuilder.append((char) inByte);
							// TODO is there a more efficient way to do this?
							
						// if end byte found, create message
						} else {
							
							String hl7String = stringBuilder.toString();
							_logger.info("HL7 INFO INPUT: " + hl7String);
							
							// get the raw HL7 bytes add them to the wrapper for event queue 
							byte[] stringBuffer = hl7String.getBytes();
							HL7Wrapper wrapper = new HL7Wrapper();
							wrapper.message = ByteBuffer.wrap(stringBuffer);

							//Grab message type + subtype from ||ADT^A01
							String[] splitMessage = hl7String.split("\\|",15);
							String msgTypeAndSubType[] = splitMessage[8].split("\\^");
							
							//Pull out message type & subtype
							wrapper.msgType = msgTypeAndSubType[0].trim();
							wrapper.msgSubType = msgTypeAndSubType[1].trim();
							
							//Pull out the version number
							wrapper.msgVersion = splitMessage[11];

							//add to event queue to be transformed
							//config.eventQueue.add(wrapper); Removed to use something more safe, leveraging offer
							config.addToEventQueue(wrapper);
							
							// generate and send acknowledgement message (if configured to)
							if(config.ackMessage)
							{
								ackMessage(splitMessage, wrapper.msgType, wrapper.msgSubType);
							}
							
							// message ended, start looking for the next message
							messageStarted = false;
							
							//Clear string builder 
							stringBuilder.delete(0, (stringBuilder.length()-1));
						}
					} // for (i < bytesRead) - still checking bytes
				} // while(read != -1) - still reading from socket
			} catch (IOException e) {
				_logger.warn("networkStream.read()", e);
			}

			// TODO : Implement the logic to populate plugin stat values
		}
	}
	
	//Method to Send the Acknowledgement 
	private void ackMessage(String[] splitMessage, String msgType, String msgSubType)
	{
		try {
			//Try and grab the input stream from the socket 
			OutputStream networkStream = this.config.client.getOutputStream();
			
			//Write the start byte, try and generate the ack
			networkStream.write(config.startByte);
			networkStream.write(generateAck(splitMessage, msgType, msgSubType));
			networkStream.write(config.endByte);
			networkStream.write(config.carriageReturn.getBytes());
			networkStream.flush();
			_logger.info("SENT HL7ACK ON NETWORK STREAM: " + this.config.client.toString());
			ackSeqNum++;
			//TODO Adding a sleep if we are hitting the high water mark, we should evaluate th
			if(config.checkHighWaterMark())
			{
				Thread.sleep(config.highWaterSleep);
			}
			
		} catch (IOException e) {
			_logger.warn("networkStream.write() error sending ACK", e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block from thread.sleep()
			e.printStackTrace();
		}
	}

	
	/************************************************************************************************
	
	Referenced from: https://catalyze.io/learn/hl7-202-the-hl7-ack-acknowledgement-message
	
	Example message: MSH|^~\&|EPICADT|DH|LABADT|DH|201301011226||ADT^A01|HL7MSG00001|P|2.3|           ...? I think this is just the header?
	Example Ack: MSH|^~\&|LABADT|DH|EPICADT|DH|201301011228||ACK^A01^ACK |HL7ACK00001|P|2.3
	Example Ack Continued: MSA|AA|HL7MSG00001
	
	Example Ack MD Anderson: MSH|^~\&Epic||AFS_Shelf|||20161017010107||ACK^A08|20161017010107|T|2.3|
						     MSA|AA|305485||0
	 
	 ***********************************************************************************************/

	//Sub Method to Generate the Acknowledgement
	private byte[] generateAck(String[] splitMessage, String msgType, String msgSubType)
	{
		//TODO: Switch this heavy print out crap to Debug Logs only (after finished developing)
		String ack = "";
		_logger.info("HL7 Msg: " + msgType + " : " + msgSubType + printableString(splitMessage));
		
		try{
			
			//Starts the exact same: MSH|^~\&|
			ack = splitMessage[0] + "|" + splitMessage[1] + "|" +
			//Reverse sender and receiver: EPICADT|DH|LABADT| ==> LABADT|DH|EPICADT|
						 splitMessage[4] + "|" + splitMessage[3] + "|" + splitMessage[2] + "|" +
			//For now next bit is the same DH|201301011228||
			//TODO: Should we update the time? Using the original msg time for now
						 splitMessage[5] + "|" + splitMessage[6] + "||" ;
			
			//So far we have: MSH|^~\&|LABADT|DH|EPICADT|DH|201301011228||
			//
			//Now we need to create: ACK^A01 |HL7ACK00001|P|2.3
			//
			//To do that we Grab Msg Sub Type to create: ACK^A01|
			ack = ack + "ACK^" + msgSubType + "|" +
			//Add Ack Sequence Number, eg: HL7ACK00001
				  "HL7ACK" + String.format("%05d", ackSeqNum) + "|" +
			//Add P|2.3 (take from original message) 
				  splitMessage[10] + "|" + splitMessage[11];
		    
			//Our message header should now look like: 
			//MSH|^~\&|LABADT|DH|EPICADT|DH|201301011228||ACK^A01^ACK|HL7ACK00001|P|2.3
			//
			//Now we add the message payload, a new line (carriage return)
			ack = ack + config.carriageReturn + 
			//then:MSA|AA|HL7MSG00001 (grab HL7MSG00001 from original message) add + ||0 + Carriage Return for MDA
				  "MSA|AA|" + splitMessage[9] + "||0" + config.carriageReturn;
	        
			_logger.info("HL7 INFO ACK: " + ack);
			
		}catch (Exception e) {
			_logger.warn("Error generating ACK", e);
		}
		
		return ack.getBytes();	
	}
	
	private String printableString(String[] split)
	{
		String printMe = "Printing String Array of length " + split.length;
		for(int i =0; i<split.length;i++)
		{
			printMe = printMe + " " + i + ": " + split[i];
		}
		return printMe;
	}
	
}
