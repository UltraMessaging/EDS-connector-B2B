/**
 * 
 */
package com.infa.vds.custom.sources;

import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author pyoung & rclemente
 *
 */
class HttpX_Thread implements Runnable {

	static final short Number_Connections = 1;
	static final short Number_Disconnections = 2;

	Logger _logger = LoggerFactory.getLogger(HttpXSource.class);

	HttpXSource_Shared config;

	// TODO: Do we need to persist this across restarts some how? Hopefully
	// not...
	int ackSeqNum = 1;

	HttpX_Thread(HttpXSource_Shared sourceConfig) {
		config = sourceConfig;
	}

	public void run() {

		HttpXSource src = config.sourceObj;
		int failcount = 0;
		while (!config.closeSource) {
			try {
				if (config.connect == null) {
					config.connect = config.url.openConnection();
					config.numConnections++;
					src.pluginStatsIdVsValue.put(Number_Connections, config.numConnections);
				}
				if (config.bufRdr == null) {
					InputStream inStream = config.connect.getInputStream();
					InputStreamReader inStreamReader = new InputStreamReader(inStream);
					config.bufRdr = new BufferedReader(inStreamReader);
				}
			} catch (IOException ioe) {
				_logger.error(config.strURL, ioe);
				config.closeSource = true;
				try {
					config.bufRdr.close();
				} catch (Exception e) {

				}
				config.bufRdr = null;
				config.connect = null;
				if (failcount++ < 3) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					continue;
				} else {
					_logger.error("Too many connect failures - exiting");
					return;
				}
			}
			_logger.info("HTTPX Source connected to {}:{}", config.host, config.port);
			// Look for and pull HTTPX messages off the socket
			getMessages(config.bufRdr);
			config.connect = null;
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
	}

	public void getMessages(BufferedReader in) {
		HttpXSource src = config.sourceObj;
		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.length() > 0) {
					inputLine += "\n";
					config.eventQueue.add(inputLine.getBytes());
				}
			}
		} catch (IOException ioe) {
			_logger.error("getMessages:readLine", ioe);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				_logger.error("Buffered Reader Close", e);
			}
			config.bufRdr = null;
			config.numDisconnections++;
			src.pluginStatsIdVsValue.put(Number_Disconnections, config.numDisconnections);
		}
	}
}
