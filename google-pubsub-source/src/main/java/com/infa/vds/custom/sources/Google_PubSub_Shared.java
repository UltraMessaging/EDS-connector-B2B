package com.infa.vds.custom.sources;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;

class Google_PubSub_Shared {
	
    
    //Google Pub/Sub Stuff
	static final int EVENT_QUEUE_SIZE = 4096;
	//int eventSize;
	LinkedBlockingQueue<byte[]> eventQueue;
	
	volatile boolean closeSource = false;
	public boolean USE_NO_DELAY = true;
	
    String subscriptionId = "my-sub";
    String projectId;
    
    final int INITIAL_SLEEP_INTERVAL = 1000;

    ProjectSubscriptionName ProjectSubscriptionName;// = ProjectSubscriptionName.of(projectId, subscriptionId);
    Subscriber subscriber;
    Google_PubSub_source sourceObj = null;
    
   public volatile boolean msgPayloadOnly = false;
    
}

