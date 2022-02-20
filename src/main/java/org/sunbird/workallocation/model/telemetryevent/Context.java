package org.sunbird.workallocation.model.telemetryevent;

public class Context {
	private String channel;
	private Pdata pdata;
	private String env;

	public String getChannel() {
		return channel;
	}

	public String getEnv() {
		return env;
	}

	public Pdata getPdata() {
		return pdata;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public void setPdata(Pdata pdata) {
		this.pdata = pdata;
	}
}
