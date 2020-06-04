package com.informatica.vds.custom.transforms;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.HashMap;

import org.json.XML;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;

public class XLingoTransform implements VDSTransform, VDSPluginStatistics {

	// Note: Field name should be same as in vdsplugin.xml
	public static final String FORMAT_LIST_SELECTION = "format-list-selection";
	public static final String CSV_JSON_COLUMNS = "csv-json-columns";
	public static final String CSV_JSON_QUOTE = "csv-json-quote-char";
	public static final String CSV_JSON_SEPARATOR = "csv-json-separator-char";
	public static final String CSV_XML_COLUMNS = "csv-xml-columns";
	public static final String CSV_XML_QUOTE = "csv-xml-quote-char";
	public static final String CSV_XML_SEPARATOR = "csv-xml-separator-char";

	/** Statistic key used to store json */
	private static final String STATISTIC = "statistic";

	/** Statistic id used in json */
	private static final String ID = "id";

	/** Map used to store plugin stat keys and values */
	protected Map<Short, Long> pluginStatsIdVsValue = new ConcurrentHashMap<Short, Long>();

	// Note: Statistic keys should be in sync with vdsplugin.xml
	private static final short XLINGO_TRANSFORM_INBYTES_STAT = 1;
	private static final short XLINGO_TRANSFORM_INRECORDS_STAT = 2;
	private static final short XLINGO_TRANSFORM_OUTBYTES_STAT = 3;
	private static final short XLINGO_TRANSFORM_OUTRECORDS_STAT = 4;

	private static long inBytes;
	private static long inRecords;
	private static long outBytes;
	private static long outRecords;

	private Logger _logger = LoggerFactory.getLogger(XLingoTransform.class);

	int conversionType = 0;
	String[] columnNames = null;
	String quoteChar = "\"";
	String separatorChar = ",";

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {

		_logger.info("Closing XLingo Transform");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.informatica.vds.api.VDSTransform#apply(com.informatica.vds.api.
	 * VDSEvent, com.informatica.vds.api.VDSEventList)
	 */
	public void apply(VDSEvent vdsEvent, VDSEventList vdsEventList) throws Exception {

		_logger.info("Applying XLingo Transform");

		// TODO Implement logic : apply transformation Sample Transform
		// Example : apply transformation: here we are prefixing with host name

		ByteBuffer src = vdsEvent.getBuffer();
		int srcLength = vdsEvent.getBufferLen();

		inRecords++;
		pluginStatsIdVsValue.put(XLINGO_TRANSFORM_INRECORDS_STAT, inRecords);
		inBytes += srcLength;
		pluginStatsIdVsValue.put(XLINGO_TRANSFORM_INBYTES_STAT, inBytes);

		if (srcLength > 0) {
			byte[] transformedRecord = null;
			switch (conversionType) {
			default:
			case 0:
				transformedRecord = xmlToJSON(src.array(), srcLength);
				break;
			case 1:
				transformedRecord = jsonToXML(src.array(), srcLength);
				break;
			case 2:
				transformedRecord = csvToJSON(src.array(), srcLength);
				break;
			case 3:
				transformedRecord = csvToXML(src.array(), srcLength);
				break;
			}
			vdsEventList.addEvent(transformedRecord, transformedRecord.length);

			outBytes += transformedRecord.length;
			pluginStatsIdVsValue.put(XLINGO_TRANSFORM_OUTBYTES_STAT, outBytes);

			outRecords++;
			pluginStatsIdVsValue.put(XLINGO_TRANSFORM_OUTRECORDS_STAT, outRecords);
		}
	}

	/**
	 * @param srcBytes
	 * @param srcLength
	 * @return byte[] transformedRecord
	 * @throws Exception
	 */
	public byte[] jsonToXML(byte[] srcBytes, int srcLength) throws Exception {
		String str = new String(Arrays.copyOf(srcBytes, srcLength), "UTF-8");
		_logger.debug("Input String is <" + str + ">");
		JSONObject json = new JSONObject(str);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
		xml += "<jsonData>";
		xml += XML.toString(json);
		xml += "</jsonData>\n";
		byte[] transformedRecord = xml.getBytes();

		return transformedRecord;
	}

	/**
	 * @param srcBytes
	 * @param srcLength
	 * @return byte[] transformedRecord
	 * @throws Exception
	 */
	public byte[] xmlToJSON(byte[] srcBytes, int srcLength) throws Exception {
		String str = new String(Arrays.copyOf(srcBytes, srcLength), "UTF-8");
		_logger.debug("Input String is <" + str + ">");
		String json = XML.toJSONObject(str).toString() + "\n";
		byte[] transformedRecord = json.getBytes();

		return transformedRecord;
	}

	/**
	 * @param srcBytes
	 * @param srcLength
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject csvToJSONObject(byte[] srcBytes, int srcLength) throws Exception {
		String str = new String(Arrays.copyOf(srcBytes, srcLength), "UTF-8");
		str = str.trim();
		_logger.debug("Input String is <" + str + ">");

		HashMap<String,String> map = new HashMap<String,String>();
		String[] csvArray = str.split(separatorChar);
		for (int i = 0; i < csvArray.length; i++) {
			String data = csvArray[i];
			String name;

			if (i < columnNames.length) {
				name = columnNames[i];
			} else {
				name = "column" + i;
			}
			if (quoteChar.length() > 0) {
				// Trim opening, closing, and embedded quotes if necessary
				if (data.startsWith(quoteChar) && data.endsWith(quoteChar)) {
					data = data.substring(1, data.length() - 1);
				}

				// Replace any embedded quotes (doubled to escape)
				data.replace(quoteChar + quoteChar, quoteChar);
			}
			// Escape embedded quotes for JSON record
			//data.replace("\"", "\"\"");
			map.put(name, data);
		}

		return new JSONObject(map);
	}
	
	/**
	 * @param srcBytes
	 * @param srcLength
	 * @return byte[] transformedRecord
	 * @throws Exception
	 */
	public byte[] csvToJSON(byte[] srcBytes, int srcLength) throws Exception {
		JSONObject json = csvToJSONObject(srcBytes, srcLength);
		String str = json.toString() + "\n";
		byte[] transformedRecord = str.getBytes();
		return transformedRecord;
		
	}

	/**
	 * @param srcBytes
	 * @param srcLength
	 * @return byte[] transformedRecord
	 * @throws Exception
	 */
	public byte[] csvToXML(byte[] srcBytes, int srcLength) throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

		JSONObject json = csvToJSONObject(srcBytes, srcLength);

		xml += "<csvData>";
		xml += XML.toString(json);
		xml += "</csvData>\n";
		byte[] transformedRecord = xml.getBytes();

		return transformedRecord;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.informatica.vds.api.VDSTransform#open(com.informatica.vds.api.
	 * VDSConfiguration)
	 */
	public void open(VDSConfiguration vdsConfiguration) throws Exception {

		// parse all the configuration
		parseConfig(vdsConfiguration);

		// populates stat keys and initialize the stat values
		populatePluginStatisticsKeys(vdsConfiguration);

		_logger.info("Opened Lingo Transform");

	}

	/**
	 * Parses the configurations defined in vdsplugin.xml and populates the
	 * values
	 * 
	 * @param vdsConfiguration
	 * @throws Exception
	 */
	public void parseConfig(VDSConfiguration vdsConfiguration) throws Exception {

		String str;
		conversionType = vdsConfiguration.getInt(FORMAT_LIST_SELECTION);
		_logger.info("Selected Conversion = " + conversionType);
		switch (conversionType) {
		case 2: // CSV to JSON
			quoteChar = vdsConfiguration.getString(CSV_JSON_QUOTE);
			separatorChar = vdsConfiguration.getString(CSV_JSON_SEPARATOR);
			str = vdsConfiguration.getString(CSV_JSON_COLUMNS);
			columnNames = str.split(separatorChar);
			_logger.info("CSV separator <{}> and quote <{}>", separatorChar, quoteChar);
			break;
		case 3: // CSV to XML
			quoteChar = vdsConfiguration.getString(CSV_XML_QUOTE);
			separatorChar = vdsConfiguration.getString(CSV_XML_SEPARATOR);
			str = vdsConfiguration.getString(CSV_XML_COLUMNS);
			columnNames = str.split(separatorChar);
			_logger.info("CSV separator <{}> and quote <{}>", separatorChar, quoteChar);
			break;
		default:
			break;
		}
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
}
