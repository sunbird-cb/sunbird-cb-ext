package org.sunbird.course.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "trackable", "instructions", "identifier", "purpose", "channel", "organisation", "description",
		"creatorLogo", "mimeType", "posterImage", "idealScreenSize", "version", "pkgVersion", "objectType",
		"learningMode", "duration", "license", "appIcon", "primaryCategory", "name", "lastUpdatedOn", "contentType" })
public class Content {

	@JsonProperty("trackable")
	private Trackable trackable;
	@JsonProperty("instructions")
	private String instructions;
	@JsonProperty("identifier")
	private String identifier;
	@JsonProperty("purpose")
	private String purpose;
	@JsonProperty("channel")
	private String channel;
	@JsonProperty("organisation")
	private List<String> organisation = null;
	@JsonProperty("description")
	private String description;
	@JsonProperty("creatorLogo")
	private String creatorLogo;
	@JsonProperty("mimeType")
	private String mimeType;
	@JsonProperty("posterImage")
	private String posterImage;
	@JsonProperty("idealScreenSize")
	private String idealScreenSize;
	@JsonProperty("version")
	private Integer version;
	@JsonProperty("pkgVersion")
	private Integer pkgVersion;
	@JsonProperty("objectType")
	private String objectType;
	@JsonProperty("learningMode")
	private String learningMode;
	@JsonProperty("duration")
	private String duration;
	@JsonProperty("license")
	private String license;
	@JsonProperty("appIcon")
	private String appIcon;
	@JsonProperty("primaryCategory")
	private String primaryCategory;
	@JsonProperty("name")
	private String name;
	@JsonProperty("lastUpdatedOn")
	private String lastUpdatedOn;
	@JsonProperty("contentType")
	private String contentType;

	@JsonProperty("trackable")
	public Trackable getTrackable() {
		return trackable;
	}

	@JsonProperty("trackable")
	public void setTrackable(Trackable trackable) {
		this.trackable = trackable;
	}

	@JsonProperty("instructions")
	public String getInstructions() {
		return instructions;
	}

	@JsonProperty("instructions")
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	@JsonProperty("identifier")
	public String getIdentifier() {
		return identifier;
	}

	@JsonProperty("identifier")
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@JsonProperty("purpose")
	public String getPurpose() {
		return purpose;
	}

	@JsonProperty("purpose")
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@JsonProperty("channel")
	public String getChannel() {
		return channel;
	}

	@JsonProperty("channel")
	public void setChannel(String channel) {
		this.channel = channel;
	}

	@JsonProperty("organisation")
	public List<String> getOrganisation() {
		return organisation;
	}

	@JsonProperty("organisation")
	public void setOrganisation(List<String> organisation) {
		this.organisation = organisation;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("creatorLogo")
	public String getCreatorLogo() {
		return creatorLogo;
	}

	@JsonProperty("creatorLogo")
	public void setCreatorLogo(String creatorLogo) {
		this.creatorLogo = creatorLogo;
	}

	@JsonProperty("mimeType")
	public String getMimeType() {
		return mimeType;
	}

	@JsonProperty("mimeType")
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@JsonProperty("posterImage")
	public String getPosterImage() {
		return posterImage;
	}

	@JsonProperty("posterImage")
	public void setPosterImage(String posterImage) {
		this.posterImage = posterImage;
	}

	@JsonProperty("idealScreenSize")
	public String getIdealScreenSize() {
		return idealScreenSize;
	}

	@JsonProperty("idealScreenSize")
	public void setIdealScreenSize(String idealScreenSize) {
		this.idealScreenSize = idealScreenSize;
	}

	@JsonProperty("version")
	public Integer getVersion() {
		return version;
	}

	@JsonProperty("version")
	public void setVersion(Integer version) {
		this.version = version;
	}

	@JsonProperty("pkgVersion")
	public Integer getPkgVersion() {
		return pkgVersion;
	}

	@JsonProperty("pkgVersion")
	public void setPkgVersion(Integer pkgVersion) {
		this.pkgVersion = pkgVersion;
	}

	@JsonProperty("objectType")
	public String getObjectType() {
		return objectType;
	}

	@JsonProperty("objectType")
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@JsonProperty("learningMode")
	public String getLearningMode() {
		return learningMode;
	}

	@JsonProperty("learningMode")
	public void setLearningMode(String learningMode) {
		this.learningMode = learningMode;
	}

	@JsonProperty("duration")
	public String getDuration() {
		return duration;
	}

	@JsonProperty("duration")
	public void setDuration(String duration) {
		this.duration = duration;
	}

	@JsonProperty("license")
	public String getLicense() {
		return license;
	}

	@JsonProperty("license")
	public void setLicense(String license) {
		this.license = license;
	}

	@JsonProperty("appIcon")
	public String getAppIcon() {
		return appIcon;
	}

	@JsonProperty("appIcon")
	public void setAppIcon(String appIcon) {
		this.appIcon = appIcon;
	}

	@JsonProperty("primaryCategory")
	public String getPrimaryCategory() {
		return primaryCategory;
	}

	@JsonProperty("primaryCategory")
	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("lastUpdatedOn")
	public String getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	@JsonProperty("lastUpdatedOn")
	public void setLastUpdatedOn(String lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	@JsonProperty("contentType")
	public String getContentType() {
		return contentType;
	}

	@JsonProperty("contentType")
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Content.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)))
				.append('[');
		sb.append("trackable");
		sb.append('=');
		sb.append(((this.trackable == null) ? "<null>" : this.trackable));
		sb.append(',');
		sb.append("instructions");
		sb.append('=');
		sb.append(((this.instructions == null) ? "<null>" : this.instructions));
		sb.append(',');
		sb.append("identifier");
		sb.append('=');
		sb.append(((this.identifier == null) ? "<null>" : this.identifier));
		sb.append(',');
		sb.append("purpose");
		sb.append('=');
		sb.append(((this.purpose == null) ? "<null>" : this.purpose));
		sb.append(',');
		sb.append("channel");
		sb.append('=');
		sb.append(((this.channel == null) ? "<null>" : this.channel));
		sb.append(',');
		sb.append("organisation");
		sb.append('=');
		sb.append(((this.organisation == null) ? "<null>" : this.organisation));
		sb.append(',');
		sb.append("description");
		sb.append('=');
		sb.append(((this.description == null) ? "<null>" : this.description));
		sb.append(',');
		sb.append("creatorLogo");
		sb.append('=');
		sb.append(((this.creatorLogo == null) ? "<null>" : this.creatorLogo));
		sb.append(',');
		sb.append("mimeType");
		sb.append('=');
		sb.append(((this.mimeType == null) ? "<null>" : this.mimeType));
		sb.append(',');
		sb.append("posterImage");
		sb.append('=');
		sb.append(((this.posterImage == null) ? "<null>" : this.posterImage));
		sb.append(',');
		sb.append("idealScreenSize");
		sb.append('=');
		sb.append(((this.idealScreenSize == null) ? "<null>" : this.idealScreenSize));
		sb.append(',');
		sb.append("version");
		sb.append('=');
		sb.append(((this.version == null) ? "<null>" : this.version));
		sb.append(',');
		sb.append("pkgVersion");
		sb.append('=');
		sb.append(((this.pkgVersion == null) ? "<null>" : this.pkgVersion));
		sb.append(',');
		sb.append("objectType");
		sb.append('=');
		sb.append(((this.objectType == null) ? "<null>" : this.objectType));
		sb.append(',');
		sb.append("learningMode");
		sb.append('=');
		sb.append(((this.learningMode == null) ? "<null>" : this.learningMode));
		sb.append(',');
		sb.append("duration");
		sb.append('=');
		sb.append(((this.duration == null) ? "<null>" : this.duration));
		sb.append(',');
		sb.append("license");
		sb.append('=');
		sb.append(((this.license == null) ? "<null>" : this.license));
		sb.append(',');
		sb.append("appIcon");
		sb.append('=');
		sb.append(((this.appIcon == null) ? "<null>" : this.appIcon));
		sb.append(',');
		sb.append("primaryCategory");
		sb.append('=');
		sb.append(((this.primaryCategory == null) ? "<null>" : this.primaryCategory));
		sb.append(',');
		sb.append("name");
		sb.append('=');
		sb.append(((this.name == null) ? "<null>" : this.name));
		sb.append(',');
		sb.append("lastUpdatedOn");
		sb.append('=');
		sb.append(((this.lastUpdatedOn == null) ? "<null>" : this.lastUpdatedOn));
		sb.append(',');
		sb.append("contentType");
		sb.append('=');
		sb.append(((this.contentType == null) ? "<null>" : this.contentType));
		sb.append(',');
		if (sb.charAt((sb.length() - 1)) == ',') {
			sb.setCharAt((sb.length() - 1), ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}

}