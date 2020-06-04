package com.infa.vds.custom.sources;

import java.net.*;


public class TxDOT_API_Test {

	static boolean test = true;
	static String m_AuthToken = "8bt2Mo9NnnFMQDdmf0bKAcotvVkRh8kDV0YRrbkUYwQ|";
	static final String TokenXML =	"<Inrix docType=\"GetSecurityToken\" copyright=\"Copyright INRIX Inc.\" versionNumber=\"11.1\" createdDate=\"2017-12-04T11:55:27Z\" statusId=\"0\" statusText=\"\" responseId=\"286c0bf2-73c0-4829-834c-a22e87f71433\"><AuthResponse><AuthToken expiry=\"2017-12-04T12:54:00Z\">*1pKyYnzI3n*NYM6FAikNP8pOiF9EjUQ8CEMcvBDfY0</AuthToken><ServerPath>devzone.inrix.com/traffic/inrix.ashx</ServerPath><ServerPaths><ServerPath type=\"API\" region=\"NA\">http://na.api.inrix.com/Traffic/Inrix.ashx</ServerPath><ServerPath type=\"TTS\" region=\"NA\">http://na-rseg-tts.inrix.com/RsegTiles/tile.ashx</ServerPath></ServerPaths></AuthResponse></Inrix>";
	static final String TokenURL = "https://api.inrix.com" 
			+"/Traffic/Inrix.ashx"
			+"?action=getsecuritytoken"
			+"&vendorid=567583903"
			+"&consumerid=76ea2157-7b82-44c8-af81-cafa610c9e43";
	
	static final String MainURL = "https://api.inrix.com/Traffic/Inrix.ashx?Action=GetSegmentSpeedInRadius&CENTER=30.299529|-97.743469&Radius=9.0&RoadSegment&Type=string&Token="; 
	
	static final String tokenXpath = "//Inrix/AuthResponse/AuthToken";
	public static void main(String[] args) throws Exception {
		URL tokenURL = new URL(TokenURL);
		//int retries;
		//String strContents;
		//int statusCode;
		while (true) {
			long now = System.currentTimeMillis();
			long next = now + 300 * 1000;
			int statusCode = -1;
			String strContents = null;
			UrlUtils uu = new UrlUtils();
			int retries = 0;
			while (retries <= 3) {
				URL mainURL = new URL(MainURL + m_AuthToken);
				statusCode = uu.getURLContents(mainURL);
				strContents = uu.getUrlString();
				if (statusCode > 0 && statusCode != 200 &&
						(strContents.toLowerCase().contains("tokenexpired") ||
								strContents.toLowerCase().contains("badtoken"))) {
					m_AuthToken = UrlUtils.getUrlXpath(tokenURL, tokenXpath, true);
					retries ++;
				} else {
					break;
				}
			}
			int len = strContents.length();
			if (len > 255) {
				strContents = strContents.substring(0, 512);
			}
			System.out.print(String.format("length %d: {%s}\n", len, strContents));
			long sleepMillis = next - System.currentTimeMillis();
			if (sleepMillis <= 1000) {
				break;
			}
			try {
				Thread.sleep(sleepMillis);
			} catch (Exception e) {
				// Woke up for another reason
				//continue;
			}
		}
	}

}
