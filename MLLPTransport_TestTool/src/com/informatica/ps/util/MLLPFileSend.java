package com.informatica.ps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.jl7.hl7.HL7Message;
import org.jl7.hl7.HL7Parser;
import org.jl7.mllp.MLLPMetaData;
import org.jl7.mllp.MLLPTransport;
import org.jl7.mllp.MLLPTransportable;

//Updating to add to github
public class MLLPFileSend {
	MLLPMetaData meta;
	MLLPTransport transport;
	
	public static void main(String[] args){
		if (args.length < 4) {
			System.out.println("Invalid Usage.");
			System.out.println("MLLPFileSend [Host] [Port] [File] [WAIT FOR ACK = 0/DON'T WAIT = 1]");
			
			System.exit(1);
		}
		
		// Execution Variables
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String file = args[2];
		long startTime = System.currentTimeMillis();
		int numMsgSent = 0;
		int waitInt = Integer.parseInt(args[3]);
		boolean waitForAck = false;
		if (waitInt == 0)
		{
			waitForAck = true;
		}
		
		// File Exists
		System.out.println("Attempting to open: " + file);
		File f = new File(file);
		if(f.isDirectory())
		{
			System.out.println("It's a directory!");
		}
		if(f.exists()){
			// Files to process
			ArrayList<File> files = new ArrayList<File>();
			
			if(f.isDirectory()){
				files = new ArrayList<File>(Arrays.asList(f.listFiles()));
			} else {
				files.add(f);
			}
			
			MLLPFileSend mps = new MLLPFileSend(host, port);
			try {
				for (int i = 0; i< files.size(); i++){
					File iFile = files.get(i);
					
					// Not recursive
					// Add only files
					if(iFile.isFile()){
						mps.send(iFile,waitForAck);
						System.out.println(iFile.getName() + " sent.");
						numMsgSent++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.err.println(args[2] + " is not a valid file or directory.");
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Sent " + numMsgSent + " messages in " + (endTime - startTime) + "ms");
	}
	
	public MLLPFileSend(String host, int port){
		this.transport = new MLLPTransport();
		this.meta = new MLLPMetaData(host, port);
		
	}
	
	@SuppressWarnings("unused")
	private void send(File hl7File) throws UnknownHostException, IOException{
		// Parse HL7 Message
		HL7Message msg = HL7Parser.parseMessage(readFile(hl7File), false);
		
		// Setup the Transportable
		MLLPTransportable mt = new MLLPTransportable();
		mt.Message = msg;
		mt.MetaData = this.meta;
		
		// Connect and send
		transport.connect(this.meta);
		transport.sendMessage(mt);
		transport.disconnect();
	}
	
	private void send(File hl7File, boolean waitForAck) throws UnknownHostException, IOException{
		// Parse HL7 Message
		HL7Message msg = HL7Parser.parseMessage(readFile(hl7File), false);
		
		// Setup the Transportable
		MLLPTransportable mt = new MLLPTransportable();
		mt.Message = msg;
		mt.MetaData = this.meta;
		
		// Connect and send
		if(!transport.isConnected())
		{
			transport.connect(this.meta);
		}
		
		if(waitForAck)
		{
			//transport.sendMessage(mt, waitForAck);
			transport.sendMessage(mt);
		}
		else 
		{
			transport.sendMessage(mt);
		}
		//TODO see if we can remove this and stay connected for the duration of the test
		transport.disconnect();
	}
	
	private static String readFile(File f) throws IOException {
	  FileInputStream stream = new FileInputStream(f);
	  try {
	    FileChannel fc = stream.getChannel();
	    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
	    /* Instead of using default, pass in a decoder. */
	    return Charset.defaultCharset().decode(bb).toString();
	  }
	  finally {
	    stream.close();
	  }
	}
}
