package com.infa.vds.custom.sources;

import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.LinkedBlockingQueue;

class TxDOT_API_Shared {
	
	static final int EVENT_QUEUE_SIZE = 4096;
	static final int INPUT_BUFFER_SIZE = 2048;
	int eventSize;
	LinkedBlockingQueue<byte[]> eventQueue;
	
	boolean closeSource = false;
	public boolean USE_NO_DELAY = true;
	
	Integer polling_period_sec;
	Integer max_attempts;
	String main_url_string;
	String main_url_query;
	String auth_url_string;
	String auth_url_query;
	URL auth_url_obj = null;
	URL main_url_obj = null;
	String auth_token_value = "abcdef";
	String auth_token_xpath = "//Inrix/AuthResponse/AuthToken";
	BufferedReader bufRdr = null;
	long numConnections = 0;
    long numDisconnections = 0;
    TxDOT_API_source sourceObj = null;
    URLConnection connect = null;

}
