package com.infa.vds.custom.sources;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;

public class Google_PubSub_source implements VDSSource, VDSPluginStatistics {

	// Note: Field name should be same as in vdsplugin.xml
	public static final String SUBSCRIPTION_ID_CONFIG = "subscriptionId";
	public static final String PROJECT_ID_CONFIG = "projectId";

	/** Statistic key used to store json */
	private static final String STATISTIC = "statistic";

	/** Statistic id used in json */
	private static final String ID = "id";

	/** Map used to store plugin stat keys and values */
	// protected Map<Short, Long> pluginStatsIdVsValue = new
	// ConcurrentHashMap<Short, Long>();

	/** Map used to store plugin stat keys and values */
	protected Map<Short, Long> pluginStatsIdVsValue = new ConcurrentHashMap<Short, Long>();
	protected IPluginRetryPolicy pluginRetryPolicyHandler;

	private Logger _logger = LoggerFactory.getLogger(Google_PubSub_source.class);
	Google_PubSub_Shared config;
	Google_PubSub_Thread google_PubSub_reader;
	Thread google_PubSub_reader_thread;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {

		config.closeSource = true;
		_logger.info("Closing Google Pub Sub Source");
		google_PubSub_reader_thread.interrupt();
		if (config.subscriber != null) {
			_logger.debug("calling stopAsync");
			config.subscriber.stopAsync();
			_logger.debug("returned from stopAsync");
			config.subscriber = null;
		}
		
		try {
			Thread.sleep(2000);
		} catch (Exception e) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.informatica.vds.api.VDSSource#open(com.informatica.vds.api.
	 * VDSConfiguration)
	 */
	public void open(VDSConfiguration vdsConfiguration) throws Exception {

		_logger.info("Opening Google PubSub Source");
		config = new Google_PubSub_Shared();

		// TODO: Update adjust and parse all the input configuration
		parseConfig(vdsConfiguration);

		config.eventQueue = new LinkedBlockingQueue<>(Google_PubSub_Shared.EVENT_QUEUE_SIZE);
		config.sourceObj = this;

		// populates stat keys and initialize the stat values
		populatePluginStatisticsKeys(vdsConfiguration);

		config.subscriber = null;
		try {

			google_PubSub_reader = new Google_PubSub_Thread(config);
			google_PubSub_reader_thread = new Thread(google_PubSub_reader);
			google_PubSub_reader_thread.start();
		} catch(Exception e) {
			_logger.error("Open error", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.informatica.vds.api.VDSSource#read(com.informatica.vds.api.
	 * VDSEventList)
	 */
	public void read(VDSEventList vdsEventList) throws Exception {

		_logger.debug("Reading from Google Pub/Sub Source");

		if (config.closeSource) {
			Thread.sleep(1000);
			close();
			return;
		}

		byte[] inputBytes = config.eventQueue.poll(1, TimeUnit.SECONDS);

		// No message in the event queue
		if (inputBytes == null) {
			return;
		}

		try {
			vdsEventList.addEvent(inputBytes, inputBytes.length);
		} catch (Exception e) {
			_logger.error("Exception ERROR:" + e.getMessage() + " adding message to the VDS event list failed", e);
		}

	}

	/**
	 * Parses the configurations defined in vdsplugin.xml and populates the
	 * values
	 * 
	 * @param vdsConfiguration
	 * @throws Exception
	 */
	public void parseConfig(VDSConfiguration vdsConfiguration) throws Exception {

		_logger.info("parseConfig");
		config.subscriptionId = vdsConfiguration.getString(SUBSCRIPTION_ID_CONFIG);
		config.projectId = vdsConfiguration.getString(PROJECT_ID_CONFIG);
/*
//isBlank(string) checks for empty string or null reference. If in UI projectId is made mandatory, no need to put below code.
        if(isBlank(config.projectId)) {
            config.projectId = ServiceOptions.getDefaultProjectId();
            if(isBlank(config.projectId)){
                _logger.error("Project ID can't be null or empty...");
                throw new Exception("Project ID can't be null or empty...");
            }
        }
*/
		String info = String.format(
				"Subscription ID = {%s}; " +
				"Project ID = {%s};\n", 
				config.subscriptionId,
				config.projectId);
		_logger.info(info);
	}

	/**
	 * Method returns stat values for statisticsKeys
	 * 
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
	 * Populates statistics keys defined in vdsplugin.xml and initializes stat
	 * values to zero.
	 * 
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

}
