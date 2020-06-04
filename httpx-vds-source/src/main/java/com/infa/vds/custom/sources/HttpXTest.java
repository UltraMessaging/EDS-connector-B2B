package com.infa.vds.custom.sources;

import java.net.*;
import java.io.*;

public class HttpXTest {

	static final String TheURL = "https://api.talkwalker.com"
			+ "/api/v2/stream/s/teststream/p/4eb2e988-6037-4aa4-b9e4-a998016d345d/results"
			+ "?access_token=a4940232-9caf-48d3-b423-274504f28da0_Vpgj.xH-Wx-v48cGsEYD.JHiD-woLmkuBoRKX.cQjxEWWAPkp04rlzQhF4fUR31CMEZCkpt-m2RnT0wi7.lzyrHeVtyt11VhIcs6L0jld8KvUJLZmWwxQl2UZa7TdHnsAgMUjQwSQQjCqCTpi3OiqaUiHspUz7gW7sJxk-bAZPg";

	public static void main(String[] args) throws Exception {
		URL myURL = new URL(TheURL);
		String host = myURL.getHost();
		String path = myURL.getPath();
		String query = myURL.getQuery();
		String file = myURL.getFile();

		System.out.print(String.format("Host = %s\nPath = %s\nQuery = %s\n File = %s\n", host, path, query, file));
		if (myURL.getProtocol().compareTo("http") == 0) {
			System.out.println("Plain HTTP");
		} else if (myURL.getProtocol().compareTo("https") == 0) {
			System.out.println("Secure HTTP");
		}
		URLConnection connect = myURL.openConnection();
		InputStream inStream = connect.getInputStream();

		String inputLine;
		InputStreamReader inStreamReader = new InputStreamReader(inStream);
		BufferedReader in = new BufferedReader(inStreamReader);
		while ((inputLine = in.readLine()) != null) {
			int len = inputLine.length();
			if (len > 0) {
				System.out.println(inputLine);
			}
		}
		in.close();
		System.out.println("Closed");
	}

}
