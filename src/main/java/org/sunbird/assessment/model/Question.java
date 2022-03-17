
package org.sunbird.assessment.model;

import java.io.Serializable;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "parent", "name", "identifier", "primaryCategory", "versionKey", "mimeType", "code", "objectType",
		"status", "qType", "index" })

public class Question extends JdkSerializationRedisSerializer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("parent")
	private String parent;
	@JsonProperty("name")
	private String name;
	@JsonProperty("identifier")
	private String identifier;
	@JsonProperty("primaryCategory")
	private String primaryCategory;
	@JsonProperty("versionKey")
	private String versionKey;
	@JsonProperty("mimeType")
	private String mimeType;
	@JsonProperty("code")
	private String code;
	@JsonProperty("objectType")
	private String objectType;
	@JsonProperty("status")
	private String status;
	@JsonProperty("qType")
	private String qType;
	@JsonProperty("index")
	private Long index;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public Question() {
	}

	/**
	 * 
	 * @param parent
	 * @param identifier
	 * @param code
	 * @param primaryCategory
	 * @param name
	 * @param index
	 * @param mimeType
	 * @param qType
	 * @param versionKey
	 * @param objectType
	 * @param status
	 */
	public Question(String parent, String name, String identifier, String primaryCategory, String versionKey,
			String mimeType, String code, String objectType, String status, String qType, Long index) {
		super();
		this.parent = parent;
		this.name = name;
		this.identifier = identifier;
		this.primaryCategory = primaryCategory;
		this.versionKey = versionKey;
		this.mimeType = mimeType;
		this.code = code;
		this.objectType = objectType;
		this.status = status;
		this.qType = qType;
		this.index = index;
	}

	@JsonProperty("parent")
	public String getParent() {
		return parent;
	}

	@JsonProperty("parent")
	public void setParent(String parent) {
		this.parent = parent;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("identifier")
	public String getIdentifier() {
		return identifier;
	}

	@JsonProperty("identifier")
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@JsonProperty("primaryCategory")
	public String getPrimaryCategory() {
		return primaryCategory;
	}

	@JsonProperty("primaryCategory")
	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	@JsonProperty("versionKey")
	public String getVersionKey() {
		return versionKey;
	}

	@JsonProperty("versionKey")
	public void setVersionKey(String versionKey) {
		this.versionKey = versionKey;
	}

	@JsonProperty("mimeType")
	public String getMimeType() {
		return mimeType;
	}

	@JsonProperty("mimeType")
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	@JsonProperty("code")
	public void setCode(String code) {
		this.code = code;
	}

	@JsonProperty("objectType")
	public String getObjectType() {
		return objectType;
	}

	@JsonProperty("objectType")
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("qType")
	public String getqType() {
		return qType;
	}

	@JsonProperty("qType")
	public void setqType(String qType) {
		this.qType = qType;
	}

	@JsonProperty("index")
	public Long getIndex() {
		return index;
	}

	@JsonProperty("index")
	public void setIndex(Long index) {
		this.index = index;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Question.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("parent");
		sb.append('=');
		sb.append(((this.parent == null) ? "<null>" : this.parent));
		sb.append(',');
		sb.append("name");
		sb.append('=');
		sb.append(((this.name == null) ? "<null>" : this.name));
		sb.append(',');
		sb.append("identifier");
		sb.append('=');
		sb.append(((this.identifier == null) ? "<null>" : this.identifier));
		sb.append(',');
		sb.append("primaryCategory");
		sb.append('=');
		sb.append(((this.primaryCategory == null) ? "<null>" : this.primaryCategory));
		sb.append(',');
		sb.append("versionKey");
		sb.append('=');
		sb.append(((this.versionKey == null) ? "<null>" : this.versionKey));
		sb.append(',');
		sb.append("mimeType");
		sb.append('=');
		sb.append(((this.mimeType == null) ? "<null>" : this.mimeType));
		sb.append(',');
		sb.append("code");
		sb.append('=');
		sb.append(((this.code == null) ? "<null>" : this.code));
		sb.append(',');
		sb.append("objectType");
		sb.append('=');
		sb.append(((this.objectType == null) ? "<null>" : this.objectType));
		sb.append(',');
		sb.append("status");
		sb.append('=');
		sb.append(((this.status == null) ? "<null>" : this.status));
		sb.append(',');
		sb.append("qType");
		sb.append('=');
		sb.append(((this.qType == null) ? "<null>" : this.qType));
		sb.append(',');
		sb.append("index");
		sb.append('=');
		sb.append(((this.index == null) ? "<null>" : this.index));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}
