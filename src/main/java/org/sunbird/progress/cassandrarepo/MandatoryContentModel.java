package org.sunbird.progress.cassandrarepo;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("mandatory_user_content")
public class MandatoryContentModel {

	@PrimaryKey
	private MandatoryContentPrimaryKeyModel primaryKey;

	@Column("content_type")
	private String contentType;

	@Column("batch_id")
	private String batchId;

	@Column("minProgressForCompletion")
	private Float minProgressCheck;

	public String getBatchId() {
		return batchId;
	}

	public String getContentType() {
		return contentType;
	}

	public Float getMinProgressCheck() {
		return minProgressCheck;
	}

	public MandatoryContentPrimaryKeyModel getPrimaryKey() {
		return primaryKey;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setMinProgressCheck(Float minProgressCheck) {
		this.minProgressCheck = minProgressCheck;
	}

	public void setPrimaryKey(MandatoryContentPrimaryKeyModel primaryKey) {
		this.primaryKey = primaryKey;
	}
}