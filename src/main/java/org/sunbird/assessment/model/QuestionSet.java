
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
@JsonPropertyOrder({
    "children",
    "name",
    "identifier",
    "primaryCategory",
    "versionKey",
    "mimeType",
    "code",
    "version",
    "objectType",
    "status",
    "childNodes"
})

public class QuestionSet extends JdkSerializationRedisSerializer implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("children")
    private List<Section> children = null;
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
    @JsonProperty("version")
    private Long version;
    @JsonProperty("objectType")
    private String objectType;
    @JsonProperty("status")
    private String status;
    @JsonProperty("childNodes")
    private List<String> childNodes = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public QuestionSet() {
    }

    /**
     * 
     * @param identifier
     * @param code
     * @param children
     * @param primaryCategory
     * @param childNodes
     * @param name
     * @param mimeType
     * @param version
     * @param versionKey
     * @param objectType
     * @param status
     */
    public QuestionSet(List<Section> children, String name, String identifier, String primaryCategory, String versionKey, String mimeType, String code, Long version, String objectType, String status, List<String> childNodes) {
        super();
        this.children = children;
        this.name = name;
        this.identifier = identifier;
        this.primaryCategory = primaryCategory;
        this.versionKey = versionKey;
        this.mimeType = mimeType;
        this.code = code;
        this.version = version;
        this.objectType = objectType;
        this.status = status;
        this.childNodes = childNodes;
    }

    @JsonProperty("children")
    public List<Section> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<Section> children) {
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

    @JsonProperty("childNodes")
    public List<String> getChildNodes() {
        return childNodes;
    }

    @JsonProperty("childNodes")
    public void setChildNodes(List<String> childNodes) {
        this.childNodes = childNodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(QuestionSet.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("children");
        sb.append('=');
        sb.append(((this.children == null)?"<null>":this.children));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("identifier");
        sb.append('=');
        sb.append(((this.identifier == null)?"<null>":this.identifier));
        sb.append(',');
        sb.append("primaryCategory");
        sb.append('=');
        sb.append(((this.primaryCategory == null)?"<null>":this.primaryCategory));
        sb.append(',');
        sb.append("versionKey");
        sb.append('=');
        sb.append(((this.versionKey == null)?"<null>":this.versionKey));
        sb.append(',');
        sb.append("mimeType");
        sb.append('=');
        sb.append(((this.mimeType == null)?"<null>":this.mimeType));
        sb.append(',');
        sb.append("code");
        sb.append('=');
        sb.append(((this.code == null)?"<null>":this.code));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("objectType");
        sb.append('=');
        sb.append(((this.objectType == null)?"<null>":this.objectType));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("childNodes");
        sb.append('=');
        sb.append(((this.childNodes == null)?"<null>":this.childNodes));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
