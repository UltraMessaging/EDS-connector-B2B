package com.informatica.vds.custom.utils;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtils {
	public static final int PCAP_MAGIC_SAME = 0xa1b2c3d4;
	public static final int PCAP_MAGIC_SWAP = 0xd4c3b2a1;
	public static final int PCAP_FILE_HDR_SIZE = 24;
	public static final int PCAP_FILE_REC_SIZE = 16;
	private static final Logger LOG = LoggerFactory.getLogger(PacketUtils.class);

	public class PcapFileHeader {
		public ByteOrder endian = ByteOrder.BIG_ENDIAN;
		public int magic_number = -1;
		public int version_major = -1;
		public int version_minor = -1;
		public int thiszone = -1;
		public int sigfigs = -1;
		public int snaplen = -1;
		public int network = -1;
	}

	public class PcapRecord {
		public int ts_sec = -1;
		public int ts_usec = -1;
		public int incl_len = -1;
		public int orig_len = -1;
	}

	public class EthernetHeader {
		public byte[] srcAddr = new byte[6];
		public byte[] dstAddr = new byte[6];
		public short dblVlanId = -1;
		public short vlanId = -1;
		public short etherType = -1;
	}

	public class IpHeader {
		public int ipVersion = -1;
		public int ipHdrLen = -1;
		public int ipTos = -1;
		public short ipLen = -1;
		public short ipId = -1;
		public short ipFrag = -1;
		public byte ipTtl = -1;
		public byte ipProto = -1;
		public short ipCksum = -1;
		public int ipSrcAddr = -1;
		public int ipDstAddr = -1;
	}

	public class UdpHeader {
		public int udpSrcPort = -1;
		public int udpDestPort = -1;
		public int udpLength = -1;
		public short udpChksum = -1;
	}

	public class RadiusHeader {
		public byte radCode = -1;
		public byte radPktId = -1;
		public short radLength = -1;
		public byte[] radAuth = new byte[16];
	}

	public class RadiusAvp {
		public short type = -1; // byte in packet
		public short length = -1; // byte in packet
		public byte[] data = null;
	}

	public static long inetStr2Long(String inet) {
		long inetNumber = 0;
		String[] strs = inet.split("\\.");
		for (int i = 0; i < strs.length; i++) {
			int shift = (8 * (3 - i));
			inetNumber |= (Integer.parseInt(strs[i])&0xff)<< shift ;
		}
		return inetNumber & 0xffffffffL;
	}
	
	public static long inetBytes2Long(byte[] bytes) {
		// ByteBuffer is BIG_ENDIAN when created
		return (long) ByteBuffer.wrap(bytes).getInt() & 0xffffffffL;
	}
	
	public static String inetLong2Str(long inet) {
		String strInet = "";
		strInet += (inet >> 24 & 0xffL) + ".";
		strInet += (inet >> 16 & 0xffL) + ".";
		strInet += (inet >> 8 & 0xffL) + ".";
		strInet += (inet & 0xffL);
		return strInet;
	}

	public PcapFileHeader readPcap(ByteBuffer bb) {
		PcapFileHeader hdr = new PcapFileHeader();
		int magic = bb.getInt(0); // Read without incrementing position
		if (magic == PCAP_MAGIC_SAME) {
			hdr.endian = ByteOrder.BIG_ENDIAN;
		} else if (magic == PCAP_MAGIC_SWAP) {
			hdr.endian = ByteOrder.LITTLE_ENDIAN;
		}
		bb.order(hdr.endian);
		hdr.magic_number = bb.getInt();
		hdr.version_major = bb.getShort();
		hdr.version_minor = bb.getShort();
		hdr.thiszone = bb.getInt();
		hdr.sigfigs = bb.getInt();
		hdr.snaplen = bb.getInt();
		hdr.network = bb.getInt();
		return hdr;
	}

	/**
	 * getPcapLink - create a header structure, but only populate the endian and network fields
	 * Use this for quick filtering
	 * @param bb
	 * @return
	 */
	public PcapFileHeader getPcapLink(ByteBuffer bb) {
		PcapFileHeader hdr = new PcapFileHeader();
		int magic = bb.getInt(0); // Read without incrementing position
		if (magic == PCAP_MAGIC_SAME) {
			hdr.endian = ByteOrder.BIG_ENDIAN;
		} else if (magic == PCAP_MAGIC_SWAP) {
			hdr.endian = ByteOrder.LITTLE_ENDIAN;
		}
		bb.order(hdr.endian);
		int position = bb.position();
		// Skip to network field
		position += 20;	bb.position(position);
		hdr.network = bb.getInt();
		// Last field - position is set correctly
		return hdr;
	}

	/**
	 * Read a PCAP record
	 * @param bb - ByteBuffer with position pointing at start of PCAP record data
	 * @param endian - Endian determined by reading the PCAP header
	 * @return
	 */
	public PcapRecord readPcapRecord(ByteBuffer bb, ByteOrder endian) {
		PcapRecord record = new PcapRecord();
		bb.order(endian);
		record.ts_sec = bb.getInt();
		record.ts_usec = bb.getInt();
		record.incl_len = bb.getInt();
		record.orig_len = bb.getInt();
		return record;
	}

	/**
	 * Retrieve the incl_len field so we know the total data size of this pcap record;
	 * Used for quick filtering
	 * @param bb
	 * @param endian
	 * @return length of this PCAP record
	 */
	public int getPcapRecordLength(ByteBuffer bb, ByteOrder endian) {
		// PcapRecord record = new PcapRecord();
		bb.order(endian);
		int position = bb.position();
		// Skip to incl_len field
		position += 8; bb.position(position);
		// Get the packet included length
		int incl_len = bb.getInt(); 
		// Skip to end
		position += 8; bb.position(position);
		return incl_len;
	}

	public EthernetHeader readEthernetHeader(ByteBuffer bb) {
		EthernetHeader hdr = new EthernetHeader();
		int position = bb.position();
		// Ether header is always Big Endian
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.position(position);
		bb.get(hdr.dstAddr);
		bb.get(hdr.srcAddr);
		int tmpShort = bb.getShort();
		if (tmpShort == 0x9100) {
			hdr.dblVlanId = bb.getShort();
			tmpShort = bb.getShort();
		}
		if (tmpShort == 0x8100) {
			hdr.vlanId = bb.getShort();
			tmpShort = bb.getShort();
		}
		
		// At this point tmpShort will have the ether type
		hdr.etherType = (short) tmpShort;

		return hdr;
	}

	/**
	 * Retrieves ethertype from packet and sets position to the start of the IP header
	 * @param bb
	 * @return
	 */
	public int getEtherType(ByteBuffer bb) {
		int position = bb.position();
		// Ether header is always Big Endian
		bb.order(ByteOrder.BIG_ENDIAN);
		// Skip src and dst addresses
		position += 12;
		bb.position(position);
		int tmpShort = bb.getShort();
		if (tmpShort == 0x9100) {
			// Skip nested vlan id
			position += 2; bb.position(position);
			tmpShort = bb.getShort();
		}
		if (tmpShort == 0x8100) {
			// Skip vlan id
			position += 2; bb.position(position);
			tmpShort = bb.getShort();
		}

		// At this point tmpShort will have the ether type
		int etherType = (short) tmpShort;

		return etherType;
	}

	public IpHeader readIpHeader(ByteBuffer bb) {
		IpHeader hdr = new IpHeader();
		bb.order(ByteOrder.BIG_ENDIAN);
		short tmpByte = (short) (bb.get() & 0xff);
		hdr.ipVersion = tmpByte >> 4;
		hdr.ipHdrLen = tmpByte & 0x0f;
		hdr.ipTos = bb.get();
		hdr.ipLen = bb.getShort();
		hdr.ipId = bb.getShort();
		hdr.ipFrag = bb.getShort();
		hdr.ipTtl = bb.get();
		hdr.ipProto = bb.get();
		hdr.ipCksum = bb.getShort();
		hdr.ipSrcAddr = bb.getInt();
		hdr.ipDstAddr = bb.getInt();
		return hdr;
	}

	/**
	 * Get IP Proto field from IP header. Set position to point at next header
	 * @param bb
	 * @return
	 */
	public int getIpProto(ByteBuffer bb) {
		//IpHeader hdr = new IpHeader();
		int position = bb.position();
		bb.order(ByteOrder.BIG_ENDIAN);
		// Skip to ipProto
		position += 9; bb.position(position);
		int ipProto = bb.get();
		// Skip to the end
		position += 11; bb.position(position);
		return ipProto;
	}

	public UdpHeader readUdpHeader(ByteBuffer bb) {
		UdpHeader hdr = new UdpHeader();
		
		bb.order(ByteOrder.BIG_ENDIAN);
		hdr.udpSrcPort = bb.getShort() & 0xffff;
		hdr.udpDestPort = bb.getShort() & 0xffff;
		hdr.udpLength = bb.getShort() & 0xffff;
		hdr.udpChksum = bb.getShort();
		return hdr;
	}

	public int getUdpDestPort(ByteBuffer bb) {
		int position = bb.position();
		bb.order(ByteOrder.BIG_ENDIAN);
		position += 2; bb.position(position);
		int udpDestPort = bb.getShort() & 0xffff;
		// Skip to the end
		position += 6; bb.position(position);
		return udpDestPort;
	}

	public RadiusHeader readRadHeader(ByteBuffer bb) {
		RadiusHeader hdr = new RadiusHeader();
		bb.position();
		bb.order(ByteOrder.BIG_ENDIAN);
		hdr.radCode = bb.get();
		hdr.radPktId = bb.get();
		hdr.radLength = bb.getShort();
		bb.get(hdr.radAuth, 0, hdr.radAuth.length);
		return hdr;
	}

	/**
	 * 
	 */
	public RadiusAvp[] readRadAvps(ByteBuffer bb, int avpLength) {
		// avpLength is the Radius packet length - 20 (length of Radius header)
		if (avpLength < 2) {
			return null; // No AVPs
		}
		int radRemain = bb.remaining();
		if (avpLength > radRemain) {
			LOG.warn("readRadAvps - expecting {} of AVP data, only have {}", avpLength, radRemain);
		} else {
			radRemain = avpLength;
		}
		ArrayList<RadiusAvp> radiusList = new ArrayList<RadiusAvp>();
		while (radRemain >= 2) {
			bb.position();
			RadiusAvp working = new RadiusAvp();
			working.type = (short) (bb.get() & 0xff);
			working.length = (short) (bb.get() & 0xff);
			radRemain -= 2;
			if (radRemain < (working.length - 2)) {
				LOG.warn(String.format("AVP field[%d] type (%d), size (%d) - missing data!!!",
						radiusList.size(), working.type, working.length));
				break;
			} else {
				working.data = new byte[working.length - 2];
				bb.get(working.data);
				radRemain -= working.data.length;
			}
			radiusList.add(working);
		}
		RadiusAvp[] avpArray = new RadiusAvp[radiusList.size()];
		return radiusList.toArray(avpArray);
	}

	String hexprint(PrintStream out, ByteBuffer bb, int incl_len, boolean printOffset) {
		StringBuilder sb = new StringBuilder();
		if (printOffset) {
			sb.append("0000\t");
		}
		if (bb.remaining() < incl_len) {
			LOG.warn(String.format("Missing packet data - expected %d bytes, only %d bytes left", incl_len,
					bb.remaining()));
		}
		for (int i = 0; i < incl_len && bb.remaining() > 0; i++) {
			sb.append(String.format("%02X ", bb.get()));
			if (printOffset) {
				if ((i + 1) % 16 == 0) {
					sb.append("\n");
					sb.append(String.format("%04x\t", i + 1));
				}
			}
		}
		if (out != null) {
			out.println(sb);
		}
		return sb.toString();
	}
}
