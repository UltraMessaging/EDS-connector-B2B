package com.infa.vds.custom.sources;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;

public class TxDOT_API_source implements VDSSource, VDSPluginStatistics {

	// Note: Field name should be same as in vdsplugin.xml
	public static final String POLLING_PERIOD_NAME = "polling_period_sec";
	public static final String MAX_ATTEMPTS_NAME = "max_attempts";
	public static final String MAIN_URL_NAME = "main_url";
	public static final String MAIN_URL_QUERY_NAME = "main_url_query";
	public static final String AUTH_URL_NAME = "auth_url";
	public static final String AUTH_URL_QUERY_NAME = "auth_url_query";

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

	private Logger _logger = LoggerFactory.getLogger(TxDOT_API_source.class);
	TxDOT_API_Shared config;
	TxDOT_API_Thread httpx_reader;
	Thread httpx_reader_thread;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {

		config.closeSource = true;
		_logger.info("Closing HTTPX Source");
		httpx_reader_thread.interrupt();
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

		_logger.info("Opening HTTPX Source");
		config = new TxDOT_API_Shared();

		// parse all the configuration
		parseConfig(vdsConfiguration);
		config.eventQueue = new LinkedBlockingQueue<>(TxDOT_API_Shared.EVENT_QUEUE_SIZE);
		config.sourceObj = this;

		// populates stat keys and initialize the stat values
		populatePluginStatisticsKeys(vdsConfiguration);

		// Start HTTPX socket handler thread
		httpx_reader = new TxDOT_API_Thread(config);
		httpx_reader_thread = new Thread(httpx_reader);
		httpx_reader_thread.start();

		_logger.info("HTTPX Source opened for reading");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.informatica.vds.api.VDSSource#read(com.informatica.vds.api.
	 * VDSEventList)
	 */
	public void read(VDSEventList vdsEventList) throws Exception {

		_logger.debug("Reading from httpx Source");

		if (config.closeSource) {
			close();
			Exception e = new Exception("Source closed");
			throw e;
		}

		byte[] inputBytes = config.eventQueue.poll(1, TimeUnit.SECONDS);

		// No message in the event queue
		if (inputBytes == null) {
			return;
		}

		try {
			vdsEventList.addEvent(inputBytes, inputBytes.length);
		} catch (Exception e) {
			_logger.error("Exception ERROR:" + e.getMessage() + " transform failed");
			e.printStackTrace();
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

		config.polling_period_sec = vdsConfiguration.getInt(POLLING_PERIOD_NAME);
		config.max_attempts = vdsConfiguration.getInt(MAX_ATTEMPTS_NAME);
		config.main_url_string = vdsConfiguration.getString(MAIN_URL_NAME);
		config.main_url_query = vdsConfiguration.getString(MAIN_URL_QUERY_NAME);
		config.auth_url_string = vdsConfiguration.getString(AUTH_URL_NAME);
		config.auth_url_query = vdsConfiguration.getString(AUTH_URL_QUERY_NAME);
		config.auth_url_obj = new URL(config.auth_url_string+config.auth_url_query);
		String info = String.format(
				"Main URL = {%s};\n" +
				"Auth URL = {%s};\n" +
				"Polling Period = %d seconds; Max Attempts = %d;\n", 
				config.main_url_string + config.main_url_query,
				config.auth_url_string + config.auth_url_query,
				config.polling_period_sec, config.max_attempts);
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
