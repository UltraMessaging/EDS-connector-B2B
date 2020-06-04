package com.infa.presales.spark;
import com.latencybusters.lbm.*;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

// See https://communities.informatica.com/infakb/faq/5/Pages/80008.aspx
import gnu.getopt.*;

import org.apache.log4j.Logger;
import org.apache.spark.storage.StorageLevel;
/*
 Copyright (c) 2005-2015 Informatica Corporation  Permission is granted to licensees to use
 or alter this software for any purpose, including commercial applications,
 according to the terms laid out in the Software License Agreement.

 This source code example is provided by Informatica for educational
 and evaluation purposes only.

 THE SOFTWARE IS PROVIDED "AS IS" AND INFORMATICA DISCLAIMS ALL WARRANTIES 
 EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF 
 NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR 
 PURPOSE.  INFORMATICA DOES NOT WARRANT THAT USE OF THE SOFTWARE WILL BE 
 UNINTERRUPTED OR ERROR-FREE.  INFORMATICA SHALL NOT, UNDER ANY CIRCUMSTANCES, BE 
 LIABLE TO LICENSEE FOR LOST PROFITS, CONSEQUENTIAL, INCIDENTAL, SPECIAL OR 
 INDIRECT DAMAGES ARISING OUT OF OR RELATED TO THIS AGREEMENT OR THE 
 TRANSACTIONS CONTEMPLATED HEREUNDER, EVEN IF INFORMATICA HAS BEEN APPRISED OF 
 THE LIKELIHOOD OF SUCH DAMAGES.
 */
import org.apache.spark.streaming.receiver.Receiver;

/**
 * Main Class - implements Spark entry points.
 * Processes command line arguments.
 * Creates a sub thread to handle processing. The subthread loops checking statistics and topic messages.
 * @author pyoung
 *
 */
public class UmqSparkReceiver extends Receiver<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2530056884573388947L;
	private static final int DEFAULT_MAX_NUM_SRCS = 10000;
	private static int nstats = 10;
	private static int reap_msgs = 0;
	private static int stat_secs = 0;
	private static boolean sequential = true;
	private static boolean end_on_eos = false;
	private static boolean summary = false;
	private static String broker = null;
	private static String purpose = "Purpose: Receive messages on a single topic.";

	private static String usage = "Usage: UmqSparkReceiver [options] topic\n"
			+ "Available options:\n"
			+ "  -B broker = use broker given by address.\n"
			+ "  -c filename = read config file filename\n"
			+ "  -D = deregister upon exit\n"
			+ "  -E = exit after source ends\n"
			+ "  -e = use LBM embedded mode\n"
			+ "  -X num_msgs = send an eXplicit ACK every num_msgs messages\n"
			+ "  -i offset = use offset to calculate Registration ID\n"
			+ "              (as source registration ID + offset)\n"
			+ "              offset of 0 forces creation of regid by store\n"
			+ "  -I ID = set Receiver Type ID to ID\n"
			+ "  -N seqnum_offset = display recovery sequence number info and set low seqnum to low+seqnum_offset\n"
			+ "  -S = exit after source ends, print throughput summary\n"
			+ "  -s num_secs = print statistics every num_secs along with bandwidth\n"
			+ "  -h = help\n"
			+ "  -r msgs = delete receiver after msgs messages\n";

	static Logger m_logger;
	cmdOptions m_options;

	private LBMContextThread ctxthread = null;
	public boolean isRunning = false;
	static String m_appName = "";

	public static void main(String[] args) throws Exception {
		m_appName = Thread.currentThread().getStackTrace()[1].getClassName();

		UmqSparkReceiver rcvapp = new UmqSparkReceiver(args);
		rcvapp.m_options.sparkMode = false;
		
		System.out.println("Starting " + m_appName);
		rcvapp.run_umqrcv();
	}
	
	class cmdOptions {
		long rcv_type_id = 0;
		boolean dereg = false;
		String topicName = "";
		boolean sparkMode = true;
		int ascii = 1;
	};
	
	/**
	 * Process command line arguments. Set flags in the m_options member;
	 * Called from class constructor.
	 * @param args
	 * @throws Exception
	 */
	void process_cmdline(String[] args) throws Exception {
		cmdOptions opts = m_options;
		final int OPTION_USE_SPARK = 9;
		

		LongOpt[] longopts = new LongOpt[] {
				new LongOpt("spark", LongOpt.REQUIRED_ARGUMENT, null, OPTION_USE_SPARK), 
				new LongOpt("ascii", LongOpt.NO_ARGUMENT, null, 'A'), 
		};

		Getopt gopt = new Getopt("UmqSparkReceiver", args, "+AB:c:DeEi:I:r:R:s:ShqvX:N:", longopts);
		int c = -1;
		boolean error = false;
		while ((c = gopt.getopt()) != -1) {
			try {
				switch (c) {
					case OPTION_USE_SPARK:
						opts.sparkMode = true;
						break;
					case 'A':
						opts.ascii++;
						break;
					case 'B':
						broker = gopt.getOptarg();
						break;
					case 'c':
						try {
							LBM.setConfiguration(gopt.getOptarg());
						} catch (LBMException ex) {
							m_logger.error("Error setting LBM configuration: "
									+ ex.toString());
							throw ex;
						}
						break;
					case 'D':
						opts.dereg = true;
						break;
					case 'E':
						end_on_eos = true;
						break;
					case 'e':
						sequential = false;
						break;
					case 'S':
						summary = true;
						break;
					case 's':
						stat_secs = Integer.parseInt(gopt.getOptarg());
						break;
					case 'h':
						print_help_exit(0);
					case 'I':
						opts.rcv_type_id = Long.parseLong(gopt.getOptarg());
						if(opts.rcv_type_id < 0)
						  error = true;
						break;
					case 'r':
						reap_msgs = Integer.parseInt(gopt.getOptarg());
						break;
					default:
						error = true;
				}
				if (error)
					break;
			} catch (Exception e) {
				/* type conversion exception */
				m_logger.error("process_cmdline", e);
				print_help_exit(1);
			}
		}
		if (error || gopt.getOptind() >= args.length) {
			/* An error occurred processing the command line - print help and exit */
			print_help_exit(1);
		}
		opts.topicName = args[gopt.getOptind()];
	}
	
	/**
	 * Constructor - initializes logging and option flags are parsed on exit.
	 * @param args
	 * @throws Exception
	 */
	public UmqSparkReceiver(String[] args) throws Exception {
		super(StorageLevel.MEMORY_AND_DISK_2());
		m_logger = Logger.getLogger(getClass());;
		m_options = new cmdOptions();
		LBM lbm = null;
		org.apache.log4j.BasicConfigurator.configure();
		try {
			lbm = new LBM();
		} catch (LBMException ex) {
			m_logger.error("Error initializing LBM: " + ex.toString());
			throw ex;
		}
		log4jLogger lbmlogger = new log4jLogger(m_logger);
		lbm.setLogger(lbmlogger);
		process_cmdline(args);
	}
	
	/**
	 * Spark entry point
	 */
	public void onStop() {
		// TODO - [SPARK] check stop actions
		isRunning = false;
	}

	/**
	 * Spark entry point. Creates thread to do umqrcv processing
	 */
	public void onStart() {
		isRunning = true;
		new Thread() {
			@Override
			public void run() {
				isRunning = true;
				try {
					run_umqrcv();
				} catch (Exception ex) {
					m_logger.warn("run_umqrcv exited", ex);
					isRunning = false;
				} finally {
					m_logger.warn("onStart COMPLETED");
					isRunning = false;
				}
			}
		}.start();
	}

	/**
	 * Executes umqrcv - Creates UMQ objects and loops until processing is done.
	 * @throws Exception
	 */
	void run_umqrcv() throws Exception {
		// TODO - [SPARK] Should check isStopped()
		cmdOptions opts = m_options;
		isRunning = true;

		LBMObjectRecycler objRec = new LBMObjectRecycler();
		LBMContextAttributes ctx_attr = null;
		try {
			ctx_attr = new LBMContextAttributes();
			ctx_attr.setObjectRecycler(objRec, null);
		} catch (LBMException ex) {
			m_logger.error("Error creating context attributes",ex);
			throw ex;
		}
		try {
			if (sequential) {
				ctx_attr.setProperty("operational_mode", "sequential");
			} else {
				ctx_attr.setProperty("operational_mode", "embedded");
			}
		} catch (LBMRuntimeException ex) {
			m_logger.error("Error setting operational mode", ex);
			throw ex;
		}
			
		UMQRcvCtxCB ctxcb = new UMQRcvCtxCB(m_logger);
		ctx_attr.setContextEventCallback(ctxcb);
		
		if (broker != null) {
			ctx_attr.setProperty("broker", broker);
		}
		
		LBMContext ctx = null;
		try {
			ctx = new LBMContext(ctx_attr);
		} catch (LBMException ex) {
			m_logger.error("Error creating context: " + ex.toString());
			throw ex;
		}
		LBMReceiverAttributes rcv_attr = null;
		try
		{
			rcv_attr = new LBMReceiverAttributes();
			rcv_attr.setObjectRecycler(objRec, null);
		}
		catch (LBMException ex)
		{
			m_logger.error("Error creating receiver attributes: "
					+ ex.toString());
			throw ex;
		}

		if (opts.rcv_type_id > 0) {
			try {
			    rcv_attr.setValue("umq_receiver_type_id", Long.toString(opts.rcv_type_id));
			} 
			catch (LBMException ex)
			{
				m_logger.error("Error setting umq_receiver_type_id="+opts.rcv_type_id + ex.toString());
				throw ex;
			} 
		}

		LBMTopic topic = null;
		try {
			topic = ctx.lookupTopic(opts.topicName, rcv_attr);
		} catch (LBMException ex) {
			m_logger.error("Error looking up topic: " + ex.toString());
			throw ex;
		}
		UMQRcvReceiver rcv = new UMQRcvReceiver(end_on_eos, summary, this, m_logger);
		LBMReceiver lbmrcv = null;
		try {
			if (sequential) {
				m_logger.info("No event queue, sequential mode");
				lbmrcv = new LBMReceiver(ctx, topic, rcv, null);
				ctx.enableImmediateMessageReceiver();
			} else {
				m_logger.info("No event queue, embedded mode");
				lbmrcv = new LBMReceiver(ctx, topic, rcv, null);
				ctx.enableImmediateMessageReceiver();
			}
		} catch (LBMException ex) {
			m_logger.error("Error creating receiver: " + ex.toString());
			throw ex;
		}

		// This immediate-mode receiver is *only* used for topicless
		// immediate-mode sends. Immediate sends that use a topic
		// are received with normal receiver objects.
		ctx.addImmediateMessageReceiver(rcv);

		long start_time;
		long end_time;
		long last_lost = 0, lost_tmp = 0, lost = 0;
//		if (sequential) {
//			// TODO - [SPARK] should call ctx.processEvents() in the processing loop if sequential
//			// create thread to handle event processing
//			ctxthread = new LBMContextThread(ctx);
//			ctxthread.start();
//		}
		LBMReceiverStatistics stats = null;
		boolean have_stats;
		long stat_millis = stat_secs * 1000;
		long stat_time = System.currentTimeMillis() + stat_millis;
		while (true) {
			start_time = System.currentTimeMillis();
			if (sequential) {
				ctx.processEvents(1000);
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) { }
			}
			end_time = System.currentTimeMillis();
			
			have_stats = false;
			while (!have_stats){
				try{
					stats = lbmrcv.getStatistics(nstats);
					have_stats = true;
				}
				catch (LBMException ex){
					/* Double the number of stats passed to the API to be retrieved */
					/* Do so until we retrieve stats successfully or hit the max limit */
					nstats *= 2;
					if (nstats > DEFAULT_MAX_NUM_SRCS){
						m_logger.error("Error getting receiver statistics: " + ex.toString());
						throw ex;
					}
					/* have_stats is still false */
				}
			}
			
			/* If we get here, we have the stats */
			try {
				lost = 0;
				for (int i = 0; i < stats.size(); i++){
					lost += stats.lost(i);
				}
				/* Account for loss in previous iteration */
				lost_tmp = lost;
				if (last_lost <= lost){
					lost -= last_lost;
				}
				else{
					lost = 0;
				}
				last_lost = lost_tmp;
				
				if (opts.ascii <= 0) {
					print_bw(end_time - start_time, rcv.msg_count, rcv.byte_count,
							rcv.unrec_count, lost, rcv.burst_loss, rcv.rx_msgs, rcv.otr_msgs);
				}
				rcv.msg_count = 0;
				rcv.byte_count = 0;
				rcv.unrec_count = 0;
				rcv.burst_loss = 0;
				rcv.rx_msgs = 0;
				rcv.otr_msgs = 0;
				
				if (stat_secs != 0 && stat_time <= end_time){
					stat_time = System.currentTimeMillis() + stat_millis;
					print_stats(stats);
				}
				objRec.doneWithReceiverStatistics(stats);
			}
			catch(LBMException ex){
				m_logger.error("Error manipulating receiver statistics: " + ex.toString());
				throw ex;
			}
			
			if (reap_msgs != 0 && rcv.total_msg_count >= reap_msgs){
				if(opts.dereg){
				    try{
						lbmrcv.deregister();
				    }
				    catch (LBMException ex){
				    	m_logger.error("Error deregistering from queues: " + ex.toString());
				    	throw ex;
				    }
				}
				else{
				    rcv.rcv_done = true;
				}	
			}

			if(rcv.rcv_done || !isRunning) {
				rcv.rcv_done = true;
				isRunning = false;
				break;
			}
		}
		if (ctxthread != null) {
			ctxthread.terminate();
		}
		System.out.println("UMQRCV Quitting.... received " + rcv.total_msg_count
				+ " messages");
		
		objRec.close();
		try
		{
			lbmrcv.close();
		}
		catch (LBMException ex)
		{
			m_logger.warn("Error closing receiver: " + ex.toString());
		}
		ctx.close();
	}
	
	private void print_help_exit(int exit_value) throws Exception
	{
		m_logger.error(LBM.version());
		m_logger.error(purpose);
		m_logger.error(usage);
		throw new Exception("UmqSparkReceiver: Exiting with value " + exit_value);
	}

	private void print_bw(long msec, long msgs, long bytes, long unrec,
			long lost, long burst_loss, long rx_msgs, long otr_msgs) throws Exception {
		double sec;
		double mps = 0.0, bps = 0.0;
		double kscale = 1000.0, mscale = 1000000.0;
		char mgscale = 'K', bscale = 'K';

		sec = msec / 1000.;
		if (sec == 0) return; /* avoid division by zero */

		mps = ((double) msgs) / sec;
		bps = ((double) bytes * 8) / sec;
		if (mps <= mscale) {
			mgscale = 'K';
			mps /= kscale;
		} else {
			mgscale = 'M';
			mps /= mscale;
		}
		if (bps <= mscale) {
			bscale = 'K';
			bps /= kscale;
		} else {
			bscale = 'M';
			bps /= mscale;
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		String outString = "";
					
		if ((rx_msgs > 0) || (otr_msgs > 0)){
			outString += (sec + " secs. " + nf.format(mps) + " " + mgscale
				+ "msgs/sec. " + nf.format(bps) + " " + bscale + "bps" + " [RX: " + rx_msgs + "][OTR: " + otr_msgs + "]" + "\n");
		}
		else{
			outString += (sec + " secs. " + nf.format(mps) + " " + mgscale
				+ "msgs/sec. " + nf.format(bps) + " " + bscale + "bps" + "\n");
		}		
				
		if (lost != 0 || unrec != 0 || burst_loss != 0) {
			outString += (" [" + lost + " pkts lost, " + unrec
					+ " msgs unrecovered, " + burst_loss + " bursts]" + "\n");
		}
		m_logger.info(outString);
	}

	private void print_stats(LBMReceiverStatistics stats) throws Exception {
		
		String outString = "";
		try {
			for (int i = 0; i < stats.size(); i++)
			{
				switch(stats.type(i))
				{
					case LBM.TRANSPORT_STAT_TCP:
						outString += ("TCP, source " + stats.source(i)
													 + ", received "
													 + stats.lbmMessagesReceived(i)
													 + "/"
													 + stats.bytesReceived(i)
													 + ", no topics "
													 + stats.noTopicMessagesReceived(i)
													 + ", requests "
													 + stats.lbmRequestsReceived(i) + "\n");
						break;
					case LBM.TRANSPORT_STAT_LBTRU:
					case LBM.TRANSPORT_STAT_LBTRM:
						if (stats.type() == LBM.TRANSPORT_STAT_LBTRU)
							outString += ("LBT-RU" + "\n");
						else
							outString += ("LBT-RM" + "\n");
						outString += (", source " + stats.source(i)
													 + ", received "
													 + stats.messagesReceived(i)
													 + "/"
													 + stats.bytesReceived(i)
													 + ", naks "
													 + stats.nakPacketsSent(i)
													 + "/"
													 + stats.naksSent(i)
													 + ", lost "
													 + stats.lost(i)
													 + ", ncfs "
													 + stats.ncfsIgnored(i)
													 + "/"
													 + stats.ncfsShed(i)
													 + "/"
													 + stats.ncfsRetransmissionDelay(i)
													 + "/"
													 + stats.ncfsUnknown(i)
													 + ", recovery "
													 + stats.minimumRecoveryTime(i)
													 + "/"
													 + stats.meanRecoveryTime(i)
													 + "/"
													 + stats.maximumRecoveryTime(i)
													 + ", nak tx "
													 + stats.minimumNakTransmissions(i)
													 + "/"
													 + stats.minimumNakTransmissions(i)
													 + "/"
													 + stats.maximumNakTransmissions(i)
													 + ", dup "
													 + stats.duplicateMessages(i)
													 + ", unrecovered "
													 + stats.unrecoveredMessagesWindowAdvance(i)
													 + "/"
													 + stats.unrecoveredMessagesNakGenerationTimeout(i)
													 + ", LBM msgs " + stats.lbmMessagesReceived(i)
													 + ", no topics "
													 + stats.noTopicMessagesReceived(i)
													 + ", requests "
													 + stats.lbmRequestsReceived(i) + "\n");
						break;
					case LBM.TRANSPORT_STAT_LBTIPC:
						outString += ("LBT-IPC, source "
									+ stats.source(i)
									+ ", received "
									+ stats.messagesReceived(i)
									+ " msgs/"
									+ stats.bytesReceived(i)
									+ " bytes. LBM msgs "
									+ stats.lbmMessagesReceived(i)
									+ ", no topics "
									+ stats.noTopicMessagesReceived(i)
									+ ", requests "
									+ stats.lbmRequestsReceived(i) + "\n");
						break;
					case LBM.TRANSPORT_STAT_LBTRDMA:
						outString += ("LBT-RDMA, source "
									+ stats.source(i)
									+ ", received "
									+ stats.messagesReceived(i)
									+ " msgs/"
									+ stats.bytesReceived(i)
									+ " bytes. LBM msgs "
									+ stats.lbmMessagesReceived(i)
									+ ", no topics "
									+ stats.noTopicMessagesReceived(i)
									+ ", requests "
									+ stats.lbmRequestsReceived(i) + "\n");
						break;
					case LBM.TRANSPORT_STAT_BROKER:
						outString += ("BROKER, source "
									+ stats.source(i)
									+ ", received "
									+ stats.messagesReceived(i)
									+ " msgs/"
									+ stats.bytesReceived(i)
									+ " bytes" + "\n");
						break;
				}
			}
		} catch (LBMException ex) {
			if (outString.length() > 0) {
				System.out.println(outString);
			}
			m_logger.error("print_stats error: " + ex.toString());
			throw ex;
		}
		if (outString.length() == 0) {
			System.out.println("No Stats");
		} else {
			System.out.println(outString);
		}
	}
}

class UMQRcvCtxCB implements LBMContextEventCallback
{
	Logger ctx_logger= null;
	

	public UMQRcvCtxCB(Logger logger)
	{
	      ctx_logger = logger;
	}

	public int onContextEvent(Object arg, LBMContextEvent contextEvent)
	{

//	    int semval;
	    switch (contextEvent.type())
	    {
			case LBM.CONTEXT_EVENT_UMQ_REGISTRATION_COMPLETE_EX:
				UMQContextEventRegistrationCompleteInfo regcomp = contextEvent.registrationCompleteInfo();
				ctx_logger.info("UMQ queue " + 
						   regcomp.queueName() + 
						   "[" + 
						   Long.toHexString(regcomp.queueId()) +
						   "] ctx registration complete. ID " +
						   regcomp.registrationId().toString(16) +
						   " FLags " +
						   Integer.toHexString(regcomp.flags()) +
						   (((regcomp.flags() & LBM.CONTEXT_EVENT_UMQ_REGISTRATION_COMPLETE_EX_FLAG_QUORUM)== LBM.CONTEXT_EVENT_UMQ_REGISTRATION_COMPLETE_EX_FLAG_QUORUM)?"QUORUM":""));

				break;
			case LBM.CONTEXT_EVENT_UMQ_REGISTRATION_SUCCESS_EX:
				UMQContextEventRegistrationSuccessInfo reginfo = contextEvent.registrationSuccessInfo();
				ctx_logger.info("UMQ queue " + 
						   reginfo.queueName() + 
						   "[" + 
						   Long.toHexString(reginfo.queueId()) +
						   "][" + 
						   reginfo.queueInstanceName() + 
						   "][" + 
						   reginfo.queueInstanceIndex() + 
						   "] ctx registration. ID " +
						   reginfo.registrationId().toString(16) +
						   " FLags " +
						   Integer.toHexString(reginfo.flags()) +
						   (((reginfo.flags() & LBM.CONTEXT_EVENT_UMQ_REGISTRATION_COMPLETE_EX_FLAG_QUORUM)== LBM.CONTEXT_EVENT_UMQ_REGISTRATION_COMPLETE_EX_FLAG_QUORUM)?"QUORUM":""));
				break;
			case LBM.CONTEXT_EVENT_UMQ_REGISTRATION_ERROR:
				ctx_logger.info("Error registering context with queue: " + contextEvent.dataString());
				break;
			case LBM.CONTEXT_EVENT_UMQ_INSTANCE_LIST_NOTIFICATION:
				ctx_logger.info("UMQ Instance list changed: " + contextEvent.dataString());
				break;
			default:
				ctx_logger.info("Unknown context event: " + contextEvent.dataString());
				break;
	    }
	    return 0;
	}
}

class UMQRcvReceiver implements LBMReceiverCallback, LBMImmediateMessageCallback {
	public long imsg_count = 0;
	public long msg_count = 0;
	public long total_msg_count = 0;
	public long subtotal_msg_count = 0;
	public long byte_count = 0;
	public long unrec_count = 0;
	public long total_unrec_count = 0;
	public long burst_loss = 0;
	public long rx_msgs = 0;
	public long otr_msgs = 0;
	public long data_start_time = 0;
	public long data_end_time = 0;
	public int stotal_msg_count = 0;
	public long total_byte_count = 0;
	public boolean rcv_done = false;

	boolean _end_on_eos = false;
	boolean _summary = false;
	UmqSparkReceiver m_Parent;
	Logger rcv_logger= null;
	
	public UMQRcvReceiver(boolean end_on_eos, boolean summary, UmqSparkReceiver parent, Logger logger) {
		_end_on_eos = end_on_eos;
		_summary = summary;
		m_Parent = parent;
		rcv_logger = logger;
	}

	// This immediate-mode receiver is *only* used for topicless
	// immediate-mode sends. Immediate sends that use a topic
	// are received with normal receiver objects.
	public int onReceiveImmediate(Object cbArg, LBMMessage msg) {
		imsg_count++;
		return onReceive(cbArg, msg);
	}

	public int onReceive(Object cbArg, LBMMessage msg)  {
		String outString = "";
		switch (msg.type()) {
		case LBM.MSG_DATA:
			if (stotal_msg_count == 0)
				data_start_time = System.currentTimeMillis();
			else
				data_end_time = System.currentTimeMillis();
			msg_count++;
			total_msg_count++;
			stotal_msg_count++;
			subtotal_msg_count++;
			byte_count += msg.data().length;
			total_byte_count += msg.data().length;
			// TODO - [SPARK] - copy message and call store() with the copy
			// TODO - [SPARK] - has to match data type of the parent (Receiver<datatype>)
			String strData = "<" + msg.dataLength() + "Bytes of Undecodable Data>";
				if (m_Parent.m_options.sparkMode || m_Parent.m_options.ascii > 0) {
					try {
						strData = new String(msg.data(), "UTF-8");
					} catch (UnsupportedEncodingException ee) {
						rcv_logger.warn("Decoding message of " + msg.dataLength() + "bytes", ee);
					}
					if (m_Parent.m_options.sparkMode) {
						m_Parent.store(strData);
						break;
					}
					if (m_Parent.m_options.ascii > 1) {
						strData += "\n";
					}
					rcv_logger.info(strData);
				}
			
			
			if ((msg.flags() & LBM.MSG_FLAG_RETRANSMIT) != 0)
			{
				rx_msgs++;
			}
			if ((msg.flags() & LBM.MSG_FLAG_OTR) != 0)
			{
				otr_msgs++;
			}
			
			break;
		case LBM.MSG_BOS:
			rcv_logger.info("[" + msg.topicName() + "][" + msg.source()
					+ "], Beginning of Transport Session");
			break;
		case LBM.MSG_EOS:
			rcv_logger.info("[" + msg.topicName() + "][" + msg.source()
					+ "], End of Transport Session");
			if (_summary) {
				print_summary();
			}
			if (_end_on_eos) {
				end();
			}
			subtotal_msg_count = 0;
			break;
		case LBM.MSG_UNRECOVERABLE_LOSS:
			unrec_count++;
			total_unrec_count++;
			break;
		case LBM.MSG_UNRECOVERABLE_LOSS_BURST:
			burst_loss++;
			break;
		case LBM.MSG_REQUEST:
			if (stotal_msg_count == 0)
				data_start_time = System.currentTimeMillis();
			else
				data_end_time = System.currentTimeMillis();
			msg_count++;
			stotal_msg_count++;
			subtotal_msg_count++;
			byte_count += msg.data().length;
			total_byte_count += msg.data().length;
			break;
		case LBM.MSG_UME_REGISTRATION_ERROR:
			rcv_logger.warn("[" + msg.topicName() + "][" + msg.source()
					+ "] UME registration error: " + msg.dataString());
			break;
		case LBM.MSG_UME_REGISTRATION_SUCCESS:
			rcv_logger.info("[" + msg.topicName() + "][" + msg.source()
					+ "] UME registration successful. Src RegID "
					+ msg.sourceRegistrationId() + " RegID "
					+ msg.receiverRegistrationId());
			break;
		case LBM.MSG_UME_REGISTRATION_SUCCESS_EX:
			outString = "";
			UMERegistrationSuccessInfo reg = msg.registrationSuccessInfo();
			outString += ("[" + msg.topicName() + "][" + msg.source()
					+ "] store " + reg.storeIndex() + ": "
					+ reg.store() + " UME registration successful. SrcRegID "
					+ reg.sourceRegistrationId() + " RcvRegID " + reg.receiverRegistrationId()
					+ ". Flags " + reg.flags() + " ");
			if ((reg.flags() & LBM.MSG_UME_REGISTRATION_SUCCESS_EX_FLAG_OLD) != 0)
				outString += ("OLD[SQN " + reg.sequenceNumber() + "] ");
			if ((reg.flags() & LBM.MSG_UME_REGISTRATION_SUCCESS_EX_FLAG_NOCACHE) != 0)
				outString += ("NOCACHE ");
			rcv_logger.info(outString);
			break;
		case LBM.MSG_UME_REGISTRATION_COMPLETE_EX:
			outString = "";
			UMERegistrationCompleteInfo regcomplete = msg.registrationCompleteInfo();
			outString += ("[" + msg.topicName() + "][" + msg.source()
					+ "] UME registration complete. SQN " + regcomplete.sequenceNumber()
					+ ". Flags " + regcomplete.flags() + " ");
			if ((regcomplete.flags() & LBM.MSG_UME_REGISTRATION_COMPLETE_EX_FLAG_QUORUM) != 0) {
				outString += ("QUORUM ");
			}
			if ((regcomplete.flags() & LBM.MSG_UME_REGISTRATION_COMPLETE_EX_FLAG_RXREQMAX) != 0) {
				outString += ("RXREQMAX ");
			}
			rcv_logger.info(outString);
			break;
		case LBM.MSG_UME_REGISTRATION_CHANGE:
			rcv_logger.info("[" + msg.topicName() + "][" + msg.source()
					+ "] UME registration change: " + msg.dataString());
			break;
		case LBM.MSG_UMQ_REGISTRATION_ERROR:
			rcv_logger.info("["
			    + msg.topicName()
			    + "]["
			    + msg.source()
			    + "] UMQ Registration error: "
			    + msg.dataString());
			break;
		case LBM.MSG_UMQ_REGISTRATION_COMPLETE_EX:
			UMQRegistrationCompleteInfo qregcomplete = msg.queueRegistrationCompleteInfo();
			outString = "";
			outString += ("["
			    + msg.topicName()
			    + "]["
			    + msg.source()
				+ "] "
				+ (((qregcomplete.flags() & LBM.MSG_UMQ_REGISTRATION_COMPLETE_EX_FLAG_ULB) != 0) ? "ULB" : "UMQ")
			    + " \"" + qregcomplete.queueName() + "\""
			    + "["
			    + Long.toHexString(qregcomplete.queueId())
			    + "] registration complete. AssignID "
			    + Long.toHexString(qregcomplete.assignmentId())
			    + ". Flags "
			    + Integer.toHexString(qregcomplete.flags()));
			if((qregcomplete.flags() & LBM.MSG_UMQ_REGISTRATION_COMPLETE_EX_FLAG_QUORUM) != 0)
				outString += (" QUORUM");
			rcv_logger.info(outString);
			break;
		case LBM.MSG_UMQ_DEREGISTRATION_COMPLETE_EX:
			UMQDeregistrationCompleteInfo qdregcomplete = msg.queueDeregistrationCompleteInfo();
			rcv_logger.info("["
			    + msg.topicName()
			    + "]["
			    + msg.source()
			    + "] "
				+ (((qdregcomplete.flags() & LBM.MSG_UMQ_DEREGISTRATION_COMPLETE_EX_FLAG_ULB) != 0) ? "ULB" : "UMQ")
			    + " \"" + qdregcomplete.queueName() + "\""
			    + "["
			    + Long.toHexString(qdregcomplete.queueId())
			    + "] deregistration complete. Flags "
			    + Integer.toHexString(qdregcomplete.flags()));
			rcv_done = true;
			break;
		case LBM.MSG_UMQ_INDEX_ASSIGNED_EX:
			UMQIndexAssignedInfo ia = msg.queueIndexAssignedInfo();
			try {
				rcv_logger.info("["
						+ msg.topicName()
						+ "]["
						+ msg.source()
						+ "] "
						+ (((ia.flags() & LBM.MSG_UMQ_INDEX_ASSIGNED_FLAG_ULB) != 0) ? "ULB" : "UMQ")
						+ " \"" + ia.queueName() + "\""
						+ "["
						+ Long.toHexString(ia.queueId())
						+ "] assigned index "
						+ (((ia.indexInfo().flags() & LBM.UMQ_INDEX_FLAG_NUMERIC) != 0) ? ia.indexInfo().numericIndex() : "\"" + new String(ia.indexInfo().index(),0,ia.indexInfo().indexLength()) + "\"")
						+ ". Flags "
						+ Integer.toHexString(ia.flags()));
			}
			catch (LBMEInvalException ex) {
				rcv_logger.warn("Error getting index info: " + ex.toString());
			}
			break;
		case LBM.MSG_UMQ_INDEX_RELEASED_EX:
			UMQIndexReleasedInfo ir = msg.queueIndexReleasedInfo();
			try {
				rcv_logger.info("["
						+ msg.topicName()
						+ "]["
						+ msg.source()
						+ "] "
						+ (((ir.flags() & LBM.MSG_UMQ_INDEX_RELEASED_FLAG_ULB) != 0) ? "ULB" : "UMQ")
						+ " \"" + ir.queueName() + "\""
						+ "["
						+ Long.toHexString(ir.queueId())
						+ "] released index "
						+ (((ir.indexInfo().flags() & LBM.UMQ_INDEX_FLAG_NUMERIC) != 0) ? ir.indexInfo().numericIndex() : "\"" + new String(ir.indexInfo().index()) + "\"")
						+ ". Flags "
						+ Integer.toHexString(ir.flags()));
			}
			catch (LBMEInvalException ex) {
				rcv_logger.warn("Error getting index info: " + ex.toString());
			}
			break;
		case LBM.MSG_UMQ_INDEX_ASSIGNMENT_ELIGIBILITY_ERROR:
			rcv_logger.warn("["
					+ msg.topicName()
					+ "]["
					+ msg.source()
					+ "] UMQ Index Assignment Eligibility error: "
					+ msg.dataString());
			break;
		case LBM.MSG_UMQ_INDEX_ASSIGNMENT_ELIGIBILITY_START_COMPLETE_EX:
			{
				UMQIndexAssignmentEligibilityStartCompleteInfo ias = msg.queueIndexAssignmentEligibilityStartCompleteInfo();
				rcv_logger.info("["
						+ msg.topicName()
						+ "]["
						+ msg.source()
						+ "] "
						+ (((ias.flags() & LBM.MSG_UMQ_INDEX_RELEASED_FLAG_ULB) != 0) ? "ULB" : "UMQ")
						+ " \"" + ias.queueName() + "\""
						+ "["
						+ Long.toHexString(ias.queueId())
						+ "] Index Assignment Eligibility start complete. Flags "
						+ Integer.toHexString(ias.flags()));
			}
			break;
		case LBM.MSG_UMQ_INDEX_ASSIGNMENT_ELIGIBILITY_STOP_COMPLETE_EX:
			{
				UMQIndexAssignmentEligibilityStopCompleteInfo ias = msg.queueIndexAssignmentEligibilityStopCompleteInfo();
				rcv_logger.info("["
						+ msg.topicName()
						+ "]["
						+ msg.source()
						+ "] "
						+ (((ias.flags() & LBM.MSG_UMQ_INDEX_RELEASED_FLAG_ULB) != 0) ? "ULB" : "UMQ")
						+ " \"" + ias.queueName() + "\""
						+ "["
						+ Long.toHexString(ias.queueId())
						+ "] Index Assignment Eligibility stop complete. Flags "
						+ Integer.toHexString(ias.flags()));
			}
			break;
		default:
			rcv_logger.warn("Unknown lbm_msg_t type " + msg.type() + " ["
					+ msg.topicName() + "][" + msg.source() + "]");
			break;
		}
		// TODO - [SPARK] - when should we ack the message?
		msg.dispose(); // Send ACK now
		return 0;
	}

	private void print_summary() {
		double total_time_sec, mps, bps;

		total_time_sec = 0.0;
		mps = 0.0;
		bps = 0.0;

		long bits_received = total_byte_count * 8;
		long total_time = data_end_time - data_start_time;

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);

		total_time_sec = total_time / 1000.0;

		if (total_time_sec > 0) {
			mps = stotal_msg_count / total_time_sec;
			bps = bits_received / total_time_sec;
		}
		String outString = "";

		outString += ("\nTotal time         : "
				+ nf.format(total_time_sec) + "  sec" + "\n");
		outString += ("Messages received  : " + stotal_msg_count + "\n");
		outString += ("Bytes received     : " + total_byte_count + "\n");
		outString += ("Avg. throughput    : " + nf.format(mps / 1000.0)
				+ " Kmsgs/sec, " + nf.format(bps / 1000000.0) + " Mbps\n\n");
		System.out.println(outString);

	}

	private void end() {
		System.out.println("UMQRcvReceiver Quitting.... received " + total_msg_count
				+ " messages");
		rcv_done = true;
	}
}
