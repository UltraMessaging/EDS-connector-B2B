package com.infa.vds.custom.sources;

import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.LinkedBlockingQueue;

class HttpXSource_Shared {
	
	static final int EVENT_QUEUE_SIZE = 4096;
	static final int INPUT_BUFFER_SIZE = 2048;
	int eventSize;
	LinkedBlockingQueue<byte[]> eventQueue;
	
	boolean closeSource = false;
	public boolean USE_NO_DELAY = true;
	
	String strURL = "http://127.0.0.1";
	String host = "";
	String path = "";
	String query = "";
	URL url = null;
	int port;
	BufferedReader bufRdr = null;
	long numConnections = 0;
    long numDisconnections = 0;
    HttpXSource sourceObj = null;
    URLConnection connect = null;

}
