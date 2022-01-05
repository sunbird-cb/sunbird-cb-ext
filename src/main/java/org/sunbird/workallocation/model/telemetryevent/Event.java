package org.sunbird.workallocation.model.telemetryevent;

import java.util.Map;

public class Event {
	private Actor actor;
	private String eid;
	private Map<String, Object> edata;
	private String ver;
	private long ets;
	private Context context;
	private String mid;
	private ObjectData object;

	public Actor getActor() {
		return actor;
	}

	public Context getContext() {
		return context;
	}

	public Map<String, Object> getEdata() {
		return edata;
	}

	public String getEid() {
		return eid;
	}

	public long getEts() {
		return ets;
	}

	public String getMid() {
		return mid;
	}

	public ObjectData getObject() {
		return object;
	}

	public String getVer() {
		return ver;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setEdata(Map<String, Object> edata) {
		this.edata = edata;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public void setEts(long ets) {
		this.ets = ets;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public void setObject(ObjectData object) {
		this.object = object;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}
}
