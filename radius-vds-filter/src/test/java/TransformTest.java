
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.DateFormat; //import java.io.*;

import com.informatica.vds.api.*;

import java.util.Map;
import java.util.HashMap;
import java.lang.Integer;
import java.nio.ByteBuffer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.openmdx.uses.gnu.getopt.*;
import gnu.getopt.*;

import com.informatica.vds.custom.transforms.radius_vds_filter.RadiusFilter;

/**
 *
 * @author pyoung
 */
public class TransformTest {
	public static final int MAX_READS = 120;
	private static final Logger LOG = LoggerFactory.getLogger(TransformTest.class);
	public static final int PCAP_MAGIC_SAME = 0xa1b2c3d4;
	public static final int PCAP_MAGIC_SWAP = 0xd4c3b2a1;
	public static final int PCAP_FILE_HDR_SIZE = 24;
	public static final int PCAP_FILE_REC_SIZE = 16;
	public static String m_udpPorts = "1812,1813";
	public static String m_radiusCodes = "1,4";
	public static String m_nasIpAddrs = "0.0.0.0,172.18.209.6";
	static ArrayList<String> m_fileNames = new ArrayList<String>();

	static TestUtils.MyVDSConfig vdsconf; // = new MyVDSConfig();
	static TestUtils.MyVDSEventList myEventList; // = new MyVDSConfig();
	static TestUtils.MyVDSEvent myEvent;;
	
	static String m_outFileName;
	static String m_inFileName;
	
	public static void main(String args[]) {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		String _progname = Thread.currentThread().getStackTrace()[1].getClassName();

		DateFormat df;
		Calendar cal;
		cal = Calendar.getInstance();
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL);
		RadiusFilter testTransform;
		TestUtils tu = new TestUtils();
		myEvent = tu.new MyVDSEvent();
		myEventList = tu.new MyVDSEventList();

		try {
			vdsconf = new TestUtils.MyVDSConfig();
			process_cmdline(_progname, args);
			if (m_outFileName != null) {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(m_outFileName, true), "utf-8"));
			}
			writer.write(
					"<!-- ********************************************************************************* -->\n");
			writer.write(String.format("<!-- Start application <%s> at %s -->\n", _progname, df.format(cal.getTime())));
			writer.flush();
			testTransform = new RadiusFilter(true, writer);
			vdsconf.configValues.put("udp-port-filter", m_udpPorts);
			vdsconf.configValues.put("radius-code-filter", m_radiusCodes);
			vdsconf.configValues.put("nas-ip-addr-filter", m_nasIpAddrs);
			testTransform.open(vdsconf);
		} catch (Exception e) {
			LOG.error("open", e);
			return;
		}

		// Iterate through m_fileNames
		for (String inputName : m_fileNames) {
			LOG.info("Reading file {}", inputName);
			try {
				byte[] inBuf = readFile(inputName);
				myEvent.setByteArray(inBuf, 0);
				testTransform.apply(myEvent, myEventList);
			} catch (Exception e) {
				LOG.error("Read", e);
				break;
			}
		}
		try {
			testTransform.close();
		} catch (Exception e) {
			LOG.error("Close", e);
			return;
		}
	}

	private static void process_cmdline(String _progname, String[] args) {
		try {
			int argId;
			LongOpt[] longopts = { // Name, has_arg, flag, value
					new LongOpt("udpPorts", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
					new LongOpt("radiusCodes", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
					new LongOpt("nasIpAddrs", LongOpt.REQUIRED_ARGUMENT, null, 'n'), };
			// String optarg;

			Getopt g = new Getopt(_progname, args, "i:o:", longopts);
			g.setOpterr(true); // Let getopt handle errors

			while ((argId = g.getopt()) != -1) {
				switch (argId) {
				case 'u':
					m_udpPorts = g.getOptarg();
					break;
				case 'r':
					m_radiusCodes = g.getOptarg();
					break;
				case 'n':
					m_nasIpAddrs = g.getOptarg();
					break;
				case ':':
					// Missing option arg
					Usage.print_and_exit(_progname);
					throw new Exception("Missing argument");
					// break;
				case '?':
					// Unknown option
					Usage.print_and_exit(_progname);
					throw new Exception("Unknown option");
					// break;
				default:
					System.err.println("Unexpected getopt error: <" + argId + ">");
					System.exit(1);
					break;
				} // switch (argId)
			} // while (argId)
			int idx = g.getOptind();
			for (int i = idx; i < args.length; i++) {
				m_fileNames.add(args[i]);
			}
		} catch (Exception e) {
			LOG.error("GetOpt", e);
			System.exit(-1);
		} // try-catch
	} // process_cmdline

	static  byte[] readFile(String fileName) {

		byte[] bFile = null;
		File file = new File(fileName);
		FileInputStream inStream;

		bFile = new byte[(int) file.length()];
		try {
			// convert file into array of bytes
			bFile = new byte[(int) file.length()];
			inStream = new FileInputStream(file);
			inStream.read(bFile);
			inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bFile;
	}

} // Class Source Test


class Usage {
	final static String message = "Usage: %s [-i <file>] [-o <file>]\n" + "\t--inputFileName=<file> # Input PCAP file\n"
			+ "\t--outputFileName=<file> # output file\n" + "\n" + "";

	static void print_and_exit(String progname) {
		System.err.printf(String.format(Usage.message, progname));
		System.exit(1);
	}
} // Class Usage

class TestUtils {

	class MyVDSEvent implements VDSEvent {
		ByteBuffer m_ByteBuffer = null;

		public ByteBuffer getBuffer() {
			return m_ByteBuffer;
		}

		public int getBufferLen() {
			return m_ByteBuffer.remaining();
		}

		public boolean hasProperties() {
			return false;
		}

		public void setByteArray(byte[] paramArrayOfByte, int paramInt) {
			m_ByteBuffer = ByteBuffer.wrap(paramArrayOfByte);
		}

		public void setBuffer(ByteBuffer paramByteBuffer, int paramInt) {
			m_ByteBuffer = paramByteBuffer;
		}

		public EventType getEventType() {
			return EventType.BUFFER;
		}

		public boolean isStructured() {
			return false;
		}

		public boolean isIdleEvent() {
			return false;
		}

		// ******************************************************************************************
		// Unsupported operations below here
		public Map<String, String> getProperties() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String getProperty(String paramString) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int getMaxEventSize() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void setBufferLen(int paramInt) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void addProperties(Map<String, String> paramMap) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String addProperty(String paramString1, String paramString2) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String removeProperty(String paramString) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void associateRequestObject(Object paramObject) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object getRequestObject() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void reset() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Map<String, Object> getStructuredData() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void setStructuredData(Map<String, Object> paramMap) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void addStructuredData(String paramString, Object paramObject) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

	}

	class MyVDSEventList implements VDSEventList {
		ArrayList<ByteBuffer> m_eventList = new ArrayList<ByteBuffer>();

		// @Override
		public void addEvent(byte[] bytes, int i) throws Exception {
			m_eventList.add(ByteBuffer.wrap(bytes, 0, i));
		}

		// @Override
		public void addEvent(VDSEvent event) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public void addAll(java.util.List<VDSEvent> events) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public void addEvent(byte[] bytes, int i, Map<String, String> map) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public VDSEvent createEvent(int i) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public VDSEvent clone(VDSEvent vdse) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	static class MyVDSConfig implements VDSConfiguration {
		

		public Map<String, String> configValues = new HashMap<String, String>();

		// @Override
		public boolean contains(String string) {
			return configValues.containsKey(string);
		}

		// @Override
		public boolean getBoolean(String string) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public int getInt(String string) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public long getLong(String string) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public double getDouble(String string) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public String getString(String string) throws Exception {
			return configValues.get(string);
		}

		// @Override
		public boolean optBoolean(String string, boolean bln) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public int optInt(String string, int i) {
			// throw new UnsupportedOperationException("Not supported yet.");
			if (contains(string)) {
				return (new Integer(configValues.get(string)));
			} else {
				return new Integer(i);
			}
		}

		// @Override
		public long optLong(String string, long l) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public double optDouble(String string, double d) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public String optString(String string, String string1) {
			// throw new UnsupportedOperationException("Not supported yet.");
			if (contains(string)) {
				return configValues.get(string);
			} else {
				return new String(string1);
			}
		}

		// @Override
		public void addListener(VDSConfigurationListener arg0) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		// @Override
		public long getUMPSessionId(int arg0, int arg1, String arg2, String arg3) throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

}