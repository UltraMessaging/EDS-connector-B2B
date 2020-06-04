package org.jl7.mllp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jl7.hl7.HL7Message;
import org.jl7.hl7.HL7Parser;

public class MLLPTransport {

	private Socket client = null;
	private String host = null;
	private int port = -1;
	ServerSocket listener;

	public void connect(MLLPMetaData metaData) throws UnknownHostException,
			IOException {
		if (metaData != null) {
			connect(metaData.host, metaData.port);
		} else {
			connect();
		}
	}

	public void connect(String host, int port) throws UnknownHostException,
			IOException {
		if (!host.equals(this.host)) {
			this.host = host;
		}
		if (port != this.port) {
			this.port = port;
		}
		connect();
	}

	private void connect() throws UnknownHostException, IOException {
		if (!isConnected()) {
			if ((this.host != null) && (port != -1)) {
				client = new Socket(this.host, this.port);
			}
		}
	}

	public boolean isConnected() {
		return (client == null ? false : client.isConnected());
	}

	public void disconnect() throws IOException {
		try {
			if (client != null) {
				client.close();
			}
		} finally {
			client = null;
		}
	}

	public void listen(MLLPMetaData metaData) throws IOException {
		listener = new ServerSocket(metaData.port);
		client = listener.accept();
	}
	
	public void close()
	{
		try {
			listener.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MLLPTransportable receiveMessage(MLLPMetaData metaData)
			throws IOException {
		listen(metaData);
		InputStream networkStream = client.getInputStream();
		int i;
		StringBuilder builder = new StringBuilder();
		boolean started = false;
		while ((i = networkStream.read()) != -1) {
			if (!started) {
				if (i == metaData.startByte) {
					started = true;
				}
			} else if (i == metaData.endByte) {
				MLLPTransportable transportable = new MLLPTransportable();
				transportable.MetaData = metaData;
				transportable.Message = HL7Parser.parseMessage(builder
						.toString(), true);
				return transportable;
			} else {
				builder.append((char) i);
			}
		}
		return null;
	}

	public void sendMessage(MLLPTransportable transportable) throws IOException {
		connect(transportable.MetaData);
		OutputStream networkStream = client.getOutputStream();
		try {
			MLLPMetaData metaData = transportable.MetaData;
			networkStream.write(metaData.startByte);
			OutputStreamWriter streamWriter = new OutputStreamWriter(
					networkStream);
			HL7Message message = transportable.Message;
			// String characterSet = Hl7MessageExtractor.ExtractString(message,
			// "MSH|18");
			// streamWriter.Encoding = ... characterSet
			streamWriter.write(message.getValue());
			streamWriter.flush();
			networkStream.write(metaData.endByte);
			networkStream.write((byte) HL7Message.segmentTerminator);
			networkStream.flush();
		} finally {
			networkStream.close();
		}
	}

	public MLLPTransportable receiveResponse() {
		throw new RuntimeException("Not yet implemented");
	}
}
