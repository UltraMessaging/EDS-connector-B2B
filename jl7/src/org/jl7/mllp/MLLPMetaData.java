package org.jl7.mllp;

public class MLLPMetaData {
	public MLLPMetaData() {
		startByte = 0x0B;
		endByte = 0x1C;
	}
	
	public MLLPMetaData(String host, int port) {
		this.host = host;
		this.port = port;
		startByte = 0x0B;
		endByte = 0x1C;
	}

	public String host;
	public int port;
	public byte startByte;
	public byte endByte;
}
