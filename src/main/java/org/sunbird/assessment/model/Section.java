
package org.sunbird.assessment.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "parent", "children", "name", "identifier", "description", "trackable", "primaryCategory",
		"versionKey", "mimeType", "code", "version", "objectType", "status", "index", "maxQuestions" })

public class Section extends JdkSerializationRedisSerializer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("parent")
	private String parent;
	@JsonProperty("children")
	private List<Question> children = null;
	@JsonProperty("name")
	private String name;
	@JsonProperty("identifier")
	private String identifier;
	@JsonProperty("description")
	private String description;
	@JsonProperty("trackable")
	private Trackable trackable;
	@JsonProperty("primaryCategory")
	private String primaryCategory;
	@JsonProperty("versionKey")
	private String versionKey;
	@JsonProperty("mimeType")
	private String mimeType;
	@JsonProperty("code")
	private String code;
	@JsonProperty("version")
	private Long version;
	@JsonProperty("objectType")
	private String objectType;
	@JsonProperty("status")
	private String status;
	@JsonProperty("index")
	private Long index;
	@JsonProperty("maxQuestions")
	private Long maxQuestions;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public Section() {
	}

	/**
	 * 
	 * @param trackable
	 * @param parent
	 * @param identifier
	 * @param code
	 * @param description
	 * @param index
	 * @param mimeType
	 * @param version
	 * @param versionKey
	 * @param objectType
	 * @param children
	 * @param primaryCategory
	 * @param name
	 * @param status
	 * @param maxQuestions
	 */
	public Section(String parent, List<Question> children, String name, String identifier, String description,
			Trackable trackable, String primaryCategory, String versionKey, String mimeType, String code, Long version,
			String objectType, String status, Long index, Long maxQuestions) {
		super();
		this.parent = parent;
		this.children = children;
		this.name = name;
		this.identifier = identifier;
		this.description = description;
		this.trackable = trackable;
		this.primaryCategory = primaryCategory;
		this.versionKey = versionKey;
		this.mimeType = mimeType;
		this.code = code;
		this.version = version;
		this.objectType = objectType;
		this.status = status;
		this.index = index;
		this.maxQuestions = maxQuestions;
	}

	@JsonProperty("parent")
	public String getParent() {
		return parent;
	}

	@JsonProperty("parent")
	public void setParent(String parent) {
		this.parent = parent;
	}

	@JsonProperty("children")
	public List<Question> getChildren() {
		return children;
	}

	@JsonProperty("children")
	public void setChildren(List<Question> children) {
		this.children = children;
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

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("trackable")
	public Trackable getTrackable() {
		return trackable;
	}

	@JsonProperty("trackable")
	public void setTrackable(Trackable trackable) {
		this.trackable = trackable;
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

	@JsonProperty("version")
	public Long getVersion() {
		return version;
	}

	@JsonProperty("version")
	public void setVersion(Long version) {
		this.version = version;
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
		return "Child [parent=" + parent + ", children=" + children + ", name=" + name + ", identifier=" + identifier
				+ ", description=" + description + ", trackable=" + trackable + ", primaryCategory=" + primaryCategory
				+ ", versionKey=" + versionKey + ", mimeType=" + mimeType + ", code=" + code + ", version=" + version
				+ ", objectType=" + objectType + ", status=" + status + ", index=" + index + ", maxQuestions="
				+ maxQuestions + ", getParent()=" + getParent() + ", getChildren()=" + getChildren() + ", getName()="
				+ getName() + ", getIdentifier()=" + getIdentifier() + ", getDescription()=" + getDescription()
				+ ", getTrackable()=" + getTrackable() + ", getPrimaryCategory()=" + getPrimaryCategory()
				+ ", getVersionKey()=" + getVersionKey() + ", getMimeType()=" + getMimeType() + ", getCode()="
				+ getCode() + ", getVersion()=" + getVersion() + ", getObjectType()=" + getObjectType()
				+ ", getStatus()=" + getStatus() + ", getIndex()=" + getIndex() + ", getMaxQuestions()="
				+ getMaxQuestions() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

	@JsonProperty("maxQuestions")
	public Long getMaxQuestions() {
		return maxQuestions;
	}

	@JsonProperty("maxQuestions")
	public void setMaxQuestions(Long maxQuestions) {
		this.maxQuestions = maxQuestions;
	}

}
