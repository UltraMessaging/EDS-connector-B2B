/**
 * 
 */
package com.infa.vds.custom.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;

/**
 * @author pyoung & rclemente
 *
 */
class WS_API_Thread implements Runnable {

	static final short Number_Connections = 1;
	static final short Number_Disconnections = 2;

	Logger _logger = LoggerFactory.getLogger(WS_API_source.class);

	WS_API_Shared config;

	// TODO: Do we need to persist this across restarts some how? Hopefully
	// not...
	int ackSeqNum = 1;

	WS_API_Thread(WS_API_Shared sourceConfig) {
		config = sourceConfig;
	}

	public void run() {

		WS_API_source src = config.sourceObj;
		
		try {
			Thread.sleep(2000); // Pause
		} catch (Exception e) {
			_logger.error("Initial sleep", e);
		}
		while (!config.closeSource) {
			UrlUtils uu = new UrlUtils();
			long now = System.currentTimeMillis();
			long next = now + config.polling_period_sec * 1000;
			int retries = 0;
			String strContents = null;
			int statusCode = -1;
			while (retries < config.max_attempts) {
				URL mainURL = null;
				strContents = null;
				statusCode = -1;
				try {
					String mainURLString;
					mainURLString = config.main_url_string + config.main_url_query + "&Token="
							+ config.auth_token_value;
					_logger.info("Retrieving from {" + mainURLString + "}");
					mainURL = new URL(mainURLString);
					statusCode = uu.getURLContents(mainURL);
				} catch (Exception e) {
					_logger.error("Getting main url", e);
				}
				strContents = uu.getUrlString();
				int strLen;
				if (strContents != null) {
					strLen = strContents.length();
				} else {
					strLen = 0;
				}
				_logger.info("getUrl status " + statusCode + " length " + strLen);
				if (statusCode > 0 && statusCode != 200
						&& (strContents.toLowerCase().contains("tokenexpired")
								|| strContents.toLowerCase().contains("badtoken")
								|| strContents.toLowerCase().contains("notoken"))) {
					try {
						_logger.info(String.format("UrlUtils.getUrlXpath(%s, %s)", config.auth_url_obj.toString(),
								config.auth_token_xpath));
						config.auth_token_value = UrlUtils.getUrlXpath(config.auth_url_obj, config.auth_token_xpath);
					} catch (Exception e) {
						_logger.error("Getting authentication token", e);
					}
				} else {
					break;
				}
				_logger.info("Retrying");
				retries++;
			}
			if (statusCode > 0) {
				if (statusCode == 200) {
					config.numConnections++;
				}
				if (strContents != null) {
					_logger.info("Adding event of " + strContents.length() + " bytes");
					config.eventQueue.add(strContents.getBytes());
				}
			}
			src.pluginStatsIdVsValue.put(Number_Connections, config.numConnections);
			// Sleep until next iteration
			while (!config.closeSource) {
				long sleepMillis = next - System.currentTimeMillis();
				if (sleepMillis <= 1000) {
					break;
				}
				try {
					Thread.sleep(sleepMillis);
				} catch (Exception e) {
					// Woke up for another reason
					continue;
				}
				break;
			}
		}
		_logger.info("Exiting WS_API_Thread.run()");
	}
}
