package org.sunbird.progress.cassandrarepo;

import java.io.Serializable;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class MandatoryContentPrimaryKeyModel implements Serializable {

	private static final long serialVersionUID = 1L;
	@PrimaryKeyColumn(name = "root_org", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String rootOrg;

	@PrimaryKeyColumn(name = "org", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String org;

	@PrimaryKeyColumn(name = "content_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String contentId;

	public String getContentId() {
		return contentId;
	}

	public String getOrg() {
		return org;
	}

	public String getRootOrg() {
		return rootOrg;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public void setRootOrg(String rootOrg) {
		this.rootOrg = rootOrg;
	}

	@Override
	public String toString() {
		return "MandatoryContentPrimaryKeyModel [rootOrg=" + rootOrg + ", org=" + org + ", content_id=" + contentId
				+ "]";
	}
}