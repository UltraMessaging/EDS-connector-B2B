package com.infa.HL7_vds_source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;
import com.itemfield.contentmaster.CMException;
import com.itemfield.contentmaster.InputBuffer;
import com.itemfield.contentmaster.OutputBuffer;
import com.itemfield.contentmaster.ParserEngineSession;



public class HL7Source implements VDSSource, VDSPluginStatistics {

	//Note: Field name should be same as in vdsplugin.xml
	public static final String HOST_TEXT_FIELD_NAME = "host-text-field";
	public static final String PORT_DOUBLE_FIELD_NAME = "port-field";
	public static final String EVENT_SIZE = "eventSize";
	public static final String CONNECTION_TYPE = "clientserver-radio-group";
	public static final String TRANSFORM_TYPE = "transform-radio-group";
	public static final String MESSAGE_PROPERTY_KEY = "DESTINATION";
	public static final String MESSAGE_PROPERTY_ERROR_VALUE = "ERR";
	
    /** Statistic key used to store json */
    private static final String STATISTIC = "statistic";

    /** Statistic id used in json */
    private static final String ID = "id";

    /** Map used to store plugin stat keys and values  */
    protected Map<Short, Long> pluginStatsIdVsValue = new ConcurrentHashMap<Short, Long>();

    // Sample source statistic key.
    // Note: Sample source statistic keys should be in sync with vdsplugin.xml
    private static final short SAMPLE_SOURCE_STAT_KEY_1 = 1;
    private static final short SAMPLE_SOURCE_STAT_KEY_2 = 2;

    private static long sampleSourceStatVal_1;
    private static long sampleSourceStatVal_2;

    protected IPluginRetryPolicy pluginRetryPolicyHandler;

	private Logger _logger = LoggerFactory.getLogger(HL7Source.class); 
	HL7Source_Config config;
	HL7Reader_Thread hl7_reader;
	Thread hl7_reader_thread;
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		
		config.closeSource = true;
		_logger.info("Closing HL7 Source");
		hl7_reader_thread.interrupt();
		
        // TODO Implement the logic : close the HL7 Source
		if(config.listener != null)
			config.listener.close(); 
		
		if(config.client != null)
			config.client.close(); 
		
		//Example: close standard input stream
		if(config.bis != null)
			config.bis.close();
	}

	/* (non-Javadoc)
	 * @see com.informatica.vds.api.VDSSource#open(com.informatica.vds.api.VDSConfiguration)
	 */
	public void open(VDSConfiguration vdsConfiguration) throws Exception {

		_logger.info("Opening HL7 Source");
		config = new HL7Source_Config();
        
		// parse all the configuration
        parseConfig(vdsConfiguration);
        config.eventQueue = new LinkedBlockingQueue<>(HL7Source_Config.EVENT_QUEUE_SIZE);
        
        // populates stat keys and initialize the stat values
        populatePluginStatisticsKeys(vdsConfiguration);

        //Start HL7 socket handler thread
        hl7_reader = new HL7Reader_Thread(config);
        hl7_reader_thread = new Thread(hl7_reader);
        hl7_reader_thread.start();

        _logger.info("HL7 Source opened for reading");
	}

	/* (non-Javadoc)
	 * @see com.informatica.vds.api.VDSSource#read(com.informatica.vds.api.VDSEventList)
	 */
	public void read(VDSEventList vdsEventList) throws Exception {

		_logger.debug("Reading from HL7 Source");

		if (config.closeSource) {
			close();
			Exception e = new Exception("Source closed");
			throw e;
		}

		//Check Queue depth to see if we're backing up
		int queueDepth = config.eventQueue.size();
		if(config.checkHighWaterMark())
		{
			_logger.error("WARNING EVENT QUEUE IS BACKING UP, CURRENTLY AT: " + queueDepth);
		}else if(config.checkWarningMark())
		{
			_logger.warn("WARNING EVENT QUEUE IS BACKING UP, CURRENTLY AT: " + queueDepth);
		}
		
		// TODO Improve the logic, add batching
		HL7Wrapper wrapper = config.eventQueue.poll(1, TimeUnit.SECONDS);
		
		// No message in the event queue
		if (wrapper == null || wrapper.message == null) {
			return;
		}
		

		String parserType = "HL7VDS";
		Map<String, String> msgProperties = new HashMap<String, String>();
		byte[] incomingMsg = wrapper.message.array();
		boolean knownType = true;
		
		//OLD, can be removed when new version works
		/*switch(wrapper.msgType)
		{
			case "ADT":
				parserType = "MD_HL7_ADT_A08_Parser";
				break;
			case "ORU":
				parserType = "MD_HL7_ORU_R01_Parser";
				break;
			default:
				_logger.error("Unrecognized Message Type: " + wrapper.msgType);

				//If transform fails, send incoming raw message
				//forwardErrorMsg(msgProperties,incomingMsg,vdsEventList);
				knownType = false;
				//return;
				break;
		}*/
		//Previously we hard coded this, lets see if we can dynamically generate it :)
		
		//Parser naming convention: HL7_MsgType_MsgSubType_Version_parser
		//eg. HL7_ORU_R08_v23_parser
		
		String[] splitVersion = wrapper.msgVersion.split("\\.");
		String versionNoPeriod = "";
		for (int v = 0;v<splitVersion.length;v++)
		{
			versionNoPeriod += splitVersion[v];
		}
		
		parserType = "HL7_" + wrapper.msgType + "_" + wrapper.msgSubType + "_v" + versionNoPeriod + "_parser";


		if (config.useDT && knownType) {
			try{
			
				InputBuffer ib = new InputBuffer(incomingMsg);
				OutputBuffer ob = new OutputBuffer();
				ParserEngineSession sess = new ParserEngineSession(parserType, ib, ob);
	
				_logger.info("Created Parser Engine Session: " + parserType + " for: " + wrapper.msgType);
	
				sess.exec();
	
				_logger.info("Transformed!");
	
				byte[] transformedRecord = ob.toByteArray();
	
				if (transformedRecord != null) {
					//VDSEvent HL7event = vdsEventList.createEvent(transformedRecord.length);
					//HL7event.setByteArray(transformedRecord, transformedRecord.length);
					msgProperties.put(MESSAGE_PROPERTY_KEY, wrapper.msgType);
					//msgProperties.put(MESSAGE_PROPERTY_KEY, wrapper.msgSubType);
					//msgProperties.put(MESSAGE_PROPERTY_KEY, wrapper.msgVersion);
					vdsEventList.addEvent(transformedRecord, transformedRecord.length, msgProperties);
				} else {
					_logger.info("Transformed Record Null :(");
					//If transform fails, send incoming raw message
					forwardErrorMsg(msgProperties,incomingMsg,vdsEventList);
				}
			
			}catch(CMException ce){
				_logger.error("ERROR USING DT:" + ce.getAsXML() + " transform failed: "+ ce.getMessage());
				_logger.error("===== ENV VARIABLES ====="); 
				dumpVars(System.getenv(),_logger);
				ce.printStackTrace();
				//If transform fails, send incoming raw message
				forwardErrorMsg(msgProperties,incomingMsg,vdsEventList);
			}
			catch (Exception e) {
				_logger.error("Regular ERROR:" + e.getMessage() + " transform failed");
				e.printStackTrace();
				//If transform fails, send incoming raw message
				forwardErrorMsg(msgProperties,incomingMsg,vdsEventList);
			}
			

		} else {
			//Forward the raw bytes to the error queue
			forwardErrorMsg(msgProperties,incomingMsg,vdsEventList);
		}

		// TODO : Implement the logic to populate plugin stat values
		sampleSourceStatVal_1++;
		pluginStatsIdVsValue.put(SAMPLE_SOURCE_STAT_KEY_1, sampleSourceStatVal_1);

		sampleSourceStatVal_2++;
		pluginStatsIdVsValue.put(SAMPLE_SOURCE_STAT_KEY_2, sampleSourceStatVal_2);
	}
	
	/**
	 * Parses the configurations defined in vdsplugin.xml and populates the values
	 * @param vdsConfiguration
	 * @throws Exception
	 */
	public void parseConfig(VDSConfiguration vdsConfiguration) throws Exception {
		
		_logger.info("Parsing the fields defined in vdsplugin.xml");

		//parse the fields configured in vdsplugin.xml
		config.host = vdsConfiguration.getString(HOST_TEXT_FIELD_NAME);
		Double doublePort = vdsConfiguration.getDouble(PORT_DOUBLE_FIELD_NAME);
		config.port = doublePort.intValue();
		config.eventSize = vdsConfiguration.getInt(EVENT_SIZE);
		int connectionType = 99;
		int transformType = 99;

		//Server/Client Mode radio button
		connectionType = vdsConfiguration.getInt(CONNECTION_TYPE);
		if(connectionType==1){
			config.isListener = true;
		}else if(connectionType==2){
			config.isListener = false;
		}else{
			config.isListener = true;
			_logger.error("Error setting Connection Type: " + connectionType + ". Defaulting to Server Mode");
		}
		
		//Raw/XML Transform Mode radio button
		transformType = vdsConfiguration.getInt(TRANSFORM_TYPE);
		if(transformType==1){
			config.useDT = false;
		}else if(transformType==2){
			config.useDT = true;
		}else{
			config.useDT = true;
			_logger.error("Error setting Transform Type: " + transformType + ". Defaulting to XML Mode");
		}

		
		_logger.info("HostValue: " + config.host + " PortValue: " + config.port + " eventSize: "+ config.eventSize
					  + " Server Mode: " + config.isListener + " Use DT XML Transform: " + config.useDT);
	}

    /**
     * Method returns stat values for statisticsKeys
     * @param statisticsKeys
     * @return stat values 
     */
	public long[] getStatistics(short[] statisticsKeys) {

        long[] pluginStatistics = new long[statisticsKeys.length];

        for (int i = 0; i < statisticsKeys.length; i++) {
            pluginStatistics[i] = pluginStatsIdVsValue.get(statisticsKeys[i]);
        }
        return pluginStatistics;
	}

    /**
     * Populates statistics keys defined in vdsplugin.xml and initializes stat values to zero.
     * @param ctx
     * @throws Exception
     */
    public void populatePluginStatisticsKeys(VDSConfiguration ctx) throws Exception {

        String pluginStats = ctx.getString(STATISTIC);
        JSONArray pluginStatsJsonArray = new JSONObject(pluginStats).getJSONArray(STATISTIC);

        for (int i = 0; i < pluginStatsJsonArray.length(); i++) {
            JSONObject stat = pluginStatsJsonArray.getJSONObject(i);
            short pluginStatKey = Short.parseShort(stat.getString(ID));
            pluginStatsIdVsValue.put(new Short(pluginStatKey), (long) 0);

        }

    }

    public void setRetryPolicyHandler(IPluginRetryPolicy iPluginRetryPolicyHandler) {
        this.pluginRetryPolicyHandler = iPluginRetryPolicyHandler;
        this.pluginRetryPolicyHandler.setLogger(_logger);
    }
    
    //Helper method to see if our socket is connected
	public boolean isConnected() {
		return (config.client == null ? false : config.client.isConnected());
	}

	//Helper method to send raw messages to an error topic or dead letter queue
	public void forwardErrorMsg(Map<String, String> msgProperties, byte[] incomingMsg, VDSEventList vdsEventList)
	{
		if(config.sendRawToErrorQueue)
		{
			try{
				msgProperties.put(MESSAGE_PROPERTY_KEY, MESSAGE_PROPERTY_ERROR_VALUE);
				vdsEventList.addEvent(incomingMsg, incomingMsg.length, msgProperties);
			}catch (Exception e){
				_logger.error("ERROR SENDING ERROR MESSAGE FORWARD: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	//DT Helper Method
	private static void dumpVars(Map<String, ?> m, Logger _logger2) 
	{
		List<String> keys = new ArrayList<String>(m.keySet());
		Collections.sort(keys); 
		for (String k : keys) 
		{
			_logger2.error(k + " : " + m.get(k)); 
		}
	}   
	   
}
