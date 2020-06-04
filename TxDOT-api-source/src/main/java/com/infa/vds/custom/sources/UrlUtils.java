package com.infa.vds.custom.sources;
import java.net.*;
import java.io.*;

public class UrlUtils {
	
	String m_UrlContents = null;
	String getUrlString() {
		return m_UrlContents;
	}
	UrlUtils() {
		super();
	}
	
	static void checkURL(URL theURL) {
		System.out.println("checkURL <" + theURL.toString() + ">");
		String host = theURL.getHost();
		String path = theURL.getPath();
		String query = theURL.getQuery();
		String file = theURL.getFile();
		host = theURL.getHost();
		path = theURL.getPath();
		query = theURL.getQuery();
		file = theURL.getFile();

		System.out.print(String.format("Host = %s\nPath = %s\nQuery = %s\n File = %s\n", host, path, query, file));
		if (theURL.getProtocol().compareTo("http") == 0) {
			System.out.println("Plain HTTP");
		} else if (theURL.getProtocol().compareTo("https") == 0) {
			System.out.println("Secure HTTP");
		}
	}
	
	int getURLContents(URL theURL) throws Exception {
		URLConnection connect = theURL.openConnection();
		InputStream inStream = null;
		int statusCode = -1;
		try {
			inStream = connect.getInputStream();
		} catch (IOException ioe) {
			if (connect instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection) connect;
				statusCode = httpConn.getResponseCode();
				if (statusCode != 200) {
					inStream = httpConn.getErrorStream();
				} else {
					// Successful HTTP request, but threw an IOException
					// No contents, return -1
					ioe.printStackTrace();
					return -1;
				}
			} else {
				ioe.printStackTrace();
				return -1;
			}
		}
		statusCode = ((HttpURLConnection) connect).getResponseCode();
		String inputLine;
		m_UrlContents = "";
		InputStreamReader inStreamReader = new InputStreamReader(inStream);
		BufferedReader in = new BufferedReader(inStreamReader);
		while ((inputLine = in.readLine()) != null) {
			int len = inputLine.length();
			if (len > 0) {
				m_UrlContents += inputLine;
			}
		}
		in.close();
		return statusCode;
	}

	static String getUrlXpath(URL theURL, String xpath) throws Exception {
		return getUrlXpath(theURL, xpath, false);
	}

	static String getUrlXpath(URL theURL, String xpath, boolean bPrint) throws Exception {
		URLConnection connect;
		InputStream inStream;
		XmlToDocument xDoc;

		try {
			connect = theURL.openConnection();
			inStream = connect.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		xDoc = new XmlToDocument(inStream);

		xDoc.initialize();
		if (bPrint) {
			xDoc.print();
		}
		return xDoc.getXpathString(xpath);
		
	}
	

}
