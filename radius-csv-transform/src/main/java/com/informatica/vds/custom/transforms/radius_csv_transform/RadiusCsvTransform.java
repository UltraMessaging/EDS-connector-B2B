package com.informatica.vds.custom.transforms.radius_csv_transform;

import java.io.BufferedWriter;
import java.io.IOException;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.informatica.vds.api.*;
import com.informatica.vds.custom.utils.PacketUtils;
import com.informatica.vds.custom.utils.PacketUtils.*;

public class RadiusCsvTransform implements VDSTransform, VDSPluginStatistics {

	// Note: Field name should be same as in vdsplugin.xml
	public static final String UDP_PORT_LIST_NAME = "udp-port-filter";
	public static final String RADIUS_CODE_LIST_NAME = "radius-code-filter";
	public static final String NAS_IP_ADDR_LIST_NAME = "nas-ip-addr-filter";

	int[] m_udpPorts = null;
	int[] m_radiusCodes = null;
	long[] m_nasAddrs = null;

	long m_stat_passed = 0;
	long m_stat_rejected = 0;
	long m_stat_total = 0;

	/** Statistic key used to store json */
	private static final String STATISTIC = "statistic";
	//private static final short FILTER_EVENTS_REJECTED = 1;

	/** Statistic id used in json */
	private static final String ID = "id";

	/** Map used to store plugin stat keys and values */
	protected Map<Short, Long> pluginStatsIdVsValue = new ConcurrentHashMap<Short, Long>();

	private Logger _logger = LoggerFactory.getLogger(RadiusCsvTransform.class);

	public RadiusCsvTransform(boolean b, BufferedWriter writer) {
		// Entry point for test mode
		// TODO Auto-generated constructor stub
	}

	public RadiusCsvTransform() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {

		_logger.info("Closing RadiusCsvTransform Transform");

		// TODO Implement the logic : close Sample Transform
		// Example: close the streams if any
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.informatica.vds.api.VDSTransform#apply(com.informatica.vds.api.
	 * VDSEvent, com.informatica.vds.api.VDSEventList)
	 */
	public void apply(VDSEvent vdsEvent, VDSEventList vdsEventList) throws Exception {

		_logger.debug("Applying Radius Packet Filter Transform");

		ByteBuffer src = vdsEvent.getBuffer();
		int saveSrcPosition = src.position();
		int srcLength = vdsEvent.getBufferLen();

		if (srcLength > 0) {
			// both input and transform specified are valid, the transform would
			// be applied on input data.

			// append host name
			String output = processFile3(src);
			src.position(saveSrcPosition);
			if (output != null) {
				_logger.info("Decoded RADIUS packet of size {} to CSV record of length {}", src.remaining(),
						output.length());
				vdsEventList.addEvent(output.getBytes(), output.length());
			} else {
				_logger.info("Invalid Packet of {} bytes", src.remaining());
			}
		}
	}

	/**
	 * 
	 * @param bb
	 */
	boolean processFile(ByteBuffer bb) {
		PcapFileHeader pcapHdr = null;
		PcapRecord pcapRec = null;
		EthernetHeader ethHdr = null;
		IpHeader ipHdr = null;
		UdpHeader udpHdr = null;
		RadiusHeader radHdr = null;
		RadiusAvp[] radAvps = null;
		PacketUtils pu = new PacketUtils();

		pcapHdr = pu.readPcap(bb);
		if (pcapHdr == null) {
			_logger.warn("Error reading PCAP file header");
			return false;
		}
		pcapRec = pu.readPcapRecord(bb, pcapHdr.endian);
		if (pcapRec == null) {
			_logger.warn("Error reading PCAP record");
			return false;
		}
		if (pcapHdr.network == 1) {
			ethHdr = pu.readEthernetHeader(bb);
		} else {
			_logger.info("Skip packet because PCAP link type is {}", pcapHdr.network);
			return false;
		}
		if (ethHdr.etherType == 0x0800) {
			ipHdr = pu.readIpHeader(bb);
		} else {
			_logger.info("Skip packet because etherType is {}", ethHdr.etherType);
			return false;
		}

		if (ipHdr.ipProto == 17) {
			udpHdr = pu.readUdpHeader(bb);
		} else {
			_logger.info("Skip packet because ipProto is {}", ipHdr.ipProto);
			return false;
		}

		if (!udpPortMatch(udpHdr.udpDestPort)) {
			_logger.info("Skip packet - UDP port {} not matched", udpHdr.udpDestPort);
			return false;
		}

		radHdr = pu.readRadHeader(bb);
		if (!radCodeMatch(radHdr.radCode)) {
			_logger.info("Skip packet - RADIUS code {} not matched", radHdr.radCode);
			return false;
		}
		radAvps = pu.readRadAvps(bb, radHdr.radLength - 20);
		if (radNasAddrsMatch(radAvps)) {
			return true;
		} else {
			_logger.info("Skip packet - RADIUS NAS addr not matched");
			return false;
		}
	}

	boolean processFile2(ByteBuffer bb) {
		PcapFileHeader pcapHdr = null;
		RadiusHeader radHdr = null;
		RadiusAvp[] radAvps = null;
		PacketUtils pu = new PacketUtils();

		pcapHdr = pu.getPcapLink(bb);
		if (pcapHdr == null) {
			_logger.warn("Error reading PCAP file header");
			return false;
		}
		int pcapRecLength = pu.getPcapRecordLength(bb, pcapHdr.endian);
		if (pcapRecLength == -1) {
			_logger.warn("Error reading PCAP record");
			return false;
		}
		int etherType;
		if (pcapHdr.network == 1) {
			etherType = pu.getEtherType(bb);
		} else {
			_logger.info("Skip packet because PCAP link type is {}", pcapHdr.network);
			return false;
		}
		int ipProto;
		if (etherType == 0x0800) {
			ipProto = pu.getIpProto(bb);
		} else {
			_logger.info(String.format("Skip packet - etherType is %d/0x%04x", etherType, etherType));
			return false;
		}

		int udpDestPort;
		if (ipProto == 17) {
			udpDestPort = pu.getUdpDestPort(bb);
		} else {
			_logger.info("Skip packet - ipProto is {}", ipProto);
			return false;
		}

		if (!udpPortMatch(udpDestPort)) {
			_logger.info("Skip packet - UDP port {} not matched", udpDestPort);
			return false;
		}

		radHdr = pu.readRadHeader(bb);
		if (!radCodeMatch(radHdr.radCode)) {
			_logger.info("Skip packet - RADIUS code {} not matched", radHdr.radCode);
			return false;
		}
		radAvps = pu.readRadAvps(bb, radHdr.radLength - 20);
		if (radNasAddrsMatch(radAvps)) {
			return true;
		} else {
			_logger.info("Skip packet - RADIUS NAS addr not matched");
			return false;
		}
	}

	String processFile3(ByteBuffer bb) {
		PcapFileHeader pcapHdr = null;
		RadiusHeader radHdr = null;
		RadiusAvp[] radAvps = null;
		PacketUtils pu = new PacketUtils();
		String output = "";

		pcapHdr = pu.getPcapLink(bb);
		if (pcapHdr == null) {
			_logger.warn("Error reading PCAP file header");
			return null;
		}
		int pcapRecLength = pu.getPcapRecordLength(bb, pcapHdr.endian);
		if (pcapRecLength == -1) {
			_logger.warn("Error reading PCAP record");
			return null;
		}
		int etherType;
		if (pcapHdr.network == 1) {
			etherType = pu.getEtherType(bb);
		} else {
			_logger.info("Skip packet because PCAP link type is {}", pcapHdr.network);
			return null;
		}
		int ipProto;
		if (etherType == 0x0800) {
			ipProto = pu.getIpProto(bb);
		} else {
			_logger.info(String.format("Skip packet - etherType is %d/0x%04x", etherType, etherType));
			return null;
		}

		int udpDestPort;
		if (ipProto == 17) {
			udpDestPort = pu.getUdpDestPort(bb);
		} else {
			_logger.info("Skip packet - ipProto is {}", ipProto);
			return null;
		}

		radHdr = pu.readRadHeader(bb);
		if (radHdr == null) {
			_logger.warn("Error parsing RADIUS header on UDP port {}", udpDestPort);
			return null;
		}

		radAvps = pu.readRadAvps(bb, radHdr.radLength - 20);
		if (radAvps == null) {
			_logger.warn("Error parsing RADIUS AVP fields");
			return null;
		}
		output = radius2csv(radHdr, radAvps);
		return output;
	}

	private String radius2csv(RadiusHeader radHdr, RadiusAvp[] radAvps) {
		// Code,PacketId,Length,Authenticator,<AVPS>
		// <AVPs> are AVPType,AVPLength,AVPValue
		// Value is hex digits except
		// Convert User-Name (1) and NAS-IP-Address (4) to ascii
		String output = "";
		// Add header fields
		output += radHdr.radCode;
		output += "," + radHdr.radPktId;
		output += "," + radHdr.radLength;
		output += "," + "\"" + PacketUtils.hexprint(null, ByteBuffer.wrap(radHdr.radAuth), radHdr.radAuth.length, false)
				+ "\"";
		// Add AVPs
		for (RadiusAvp avp : radAvps) {
			output += "," + avp.type + "," + avp.length + ",\"";
			if (avp.type == 1) {
				output += PacketUtils.bytes2ascii(avp.data);
			} else if (avp.type == 4) {
				output += PacketUtils.inetBytes2Str(avp.data);
			} else {
				output += PacketUtils.hexprint(null, ByteBuffer.wrap(avp.data), avp.data.length, false);
			}
			output += "\"";
		}
		return output;
	}

	boolean udpPortMatch(int udpDestPort) {
		for (int port : m_udpPorts) {
			if (udpDestPort == port) {
				_logger.info("Matched udp port {}", udpDestPort);
				return true;
			}
		}
		return false;
	}

	boolean radCodeMatch(int radCode) {

		for (int code : m_radiusCodes) {
			if (radCode == code) {
				_logger.info("Matched RADIUS code {}", radCode);
				return true;
			}
		}

		return false;
	}

	/**
	 * Match avp NAS-IP addr to list of addresses from configuration
	 * 
	 * @param avps
	 * @return true if a NAS-IP is found and it matches; false if there is no
	 *         NAS-IP or the NAS-IP does not match
	 */
	boolean radNasAddrsMatch(RadiusAvp[] avps) {
		if (m_nasAddrs == null || m_nasAddrs.length == 0) {
			return true;
		}
		for (RadiusAvp avp : avps) {
			// TODO - what happens if there is no type 4 field?
			if (avp.type == 4) { // Find NAS IP Address
				long avpData = PacketUtils.inetBytes2Long(avp.data);
				for (long addr : m_nasAddrs) {
					if (avpData == addr) {
						_logger.info("Matched NAS IP {}", PacketUtils.inetLong2Str(addr));
						return true;
					}
				}
			}
		}
		return false;
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
		// populatePluginStatisticsKeys(vdsConfiguration);

		_logger.info("Opened Radius Filter Transform");

		// TODO Implement the logic : open Sample Transform
		// open streams if any
	}

	/**
	 * Parses the configurations defined in vdsplugin.xml and populates the
	 * values
	 * 
	 * @param vdsConfiguration
	 * @throws Exception
	 */
	public void parseConfig(VDSConfiguration vdsConfiguration) throws Exception {

		_logger.info("Parsing the fields defined in vdsplugin.xml");

		// String[] tmpStringArray;
		// tmpStringArray =
		// vdsConfiguration.getString(UDP_PORT_LIST_NAME).split(",");
		// m_udpPorts = new int[tmpStringArray.length];
		// for (int i = 0; i < tmpStringArray.length; i++) {
		// m_udpPorts[i] = Integer.parseInt(tmpStringArray[i]);
		// }
		// tmpStringArray =
		// vdsConfiguration.getString(RADIUS_CODE_LIST_NAME).split(",");
		// m_radiusCodes = new int[tmpStringArray.length];
		// for (int i = 0; i < tmpStringArray.length; i++) {
		// m_radiusCodes[i] = Integer.parseInt(tmpStringArray[i]);
		// }
		// tmpStringArray =
		// vdsConfiguration.getString(NAS_IP_ADDR_LIST_NAME).split(",");
		// m_nasAddrs = new long[tmpStringArray.length];
		// for (int i = 0; i < tmpStringArray.length; i++) {
		// m_nasAddrs[i] = PacketUtils.inetStr2Long(tmpStringArray[i]);
		// }
	}

	/**
	 * Method returns stat values for statisticsKeys
	 * 
	 * @param statisticsKeys
	 * @return stat values
	 */
	public long[] getStatistics(short[] statisticsKeys) {

		long[] pluginStatistics = new long[statisticsKeys.length];

		// for (int i = 0; i < statisticsKeys.length; i++) {
		// pluginStatistics[i] = pluginStatsIdVsValue.get(statisticsKeys[i]);
		// }
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
