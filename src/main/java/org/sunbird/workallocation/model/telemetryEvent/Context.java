package org.sunbird.workallocation.model.telemetryEvent;

public class Context {
    private String channel;
    private Pdata pdata;
    private String env;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Pdata getPdata() {
        return pdata;
    }

    public void setPdata(Pdata pdata) {
        this.pdata = pdata;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
