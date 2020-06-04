package com.infa.HL7_vds_source;

import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

class HL7Source_Config {
	
	static final int EVENT_QUEUE_SIZE = 4096;
	static final int INPUT_BUFFER_SIZE = 4096;
	int eventSize;
	LinkedBlockingQueue<HL7Wrapper> eventQueue;
	
	boolean closeSource = false;
	BufferedInputStream bis = null;
	Socket client = null;
	ServerSocket listener = null;
	boolean isListener = true;
	boolean useDT = false;
	boolean ackMessage = true;
	public boolean sendRawToErrorQueue = true;
	public boolean USE_NO_DELAY = true;
	
    /** The Carriage Return String  */
	//String newLine = System.lineSeparator();
	//String newLine = "/n";
	//String newLine = "/M";
	//String carriageReturn = "\r";
	//String lineFeed = "\n";
	String carriageReturn = "\r";
	//FROM JL7
	/**
	 * Character used to mark the end of a segment and the beginning of the next
	 * segment (i.e. segment delimiter). Default: ASCII 10
	 * 
	 * @since 0.1
	 */
	public static char segmentTerminator = 0x0D;

    /** The Host (if in client mode) */
	String host = "127.0.0.1";

    /** The Port  */
	int port = 3300;
	
    /** The start byte. */
    public byte startByte = 0x0B;

    /** The end byte. */
    public byte endByte = 0x1C;
    
    /** Used to track the DT Event Queue **/
	public int highWaterMark = 100;
	public int warningMark = 10;
	public long highWaterSleep = 100;
	
 
	//Check Queue depth to see if we're backing up
	public boolean checkWarningMark()
	{
		int queueDepth = this.eventQueue.size();
		if(queueDepth>this.warningMark)
		{
			return true;
		}
		return false;
	}
	
	//Check Queue depth to see if we're backing up
	public boolean checkHighWaterMark()
	{
		int queueDepth = this.eventQueue.size();
		if(queueDepth>this.highWaterMark)
		{
			return true;
		}
		return false;
	}
	
	public void addToEventQueue(HL7Wrapper wrapper)
	{
		while(true)
		{
			//add to event queue to be transformed
			if(this.eventQueue.offer(wrapper))
			{
				break;
			}
		}
	}
}

		
