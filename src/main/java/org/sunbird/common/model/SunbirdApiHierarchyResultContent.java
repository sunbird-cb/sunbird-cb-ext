package org.sunbird.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SunbirdApiHierarchyResultContent {
	private String parent;
	private String identifier;
	private String downloadUrl;
	private String channel;
	private String source;
	private String mimeType;
	private String objectType;
	private String primaryCategory;
	private String artifactUrl;
	private String contentType;
	private String status;
	private String name;
	private String code;
	private String streamingUrl;
	private List<SunbirdApiHierarchyResultContent> children;
	private List<SunbirdApiBatchResp> batches;
	private int leafNodesCount;

	public String getArtifactUrl() {
		return artifactUrl;
	}

	public List<SunbirdApiBatchResp> getBatches() {
		return batches;
	}

	public String getChannel() {
		return channel;
	}

	public List<SunbirdApiHierarchyResultContent> getChildren() {
		return children;
	}

	public String getCode() {
		return code;
	}

	public String getContentType() {
		return contentType;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getName() {
		return name;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getParent() {
		return parent;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public String getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public String getStreamingUrl() {
		return streamingUrl;
	}

	public void setArtifactUrl(String artifactUrl) {
		this.artifactUrl = artifactUrl;
	}

	public void setBatches(List<SunbirdApiBatchResp> batches) {
		this.batches = batches;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setChildren(List<SunbirdApiHierarchyResultContent> children) {
		this.children = children;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStreamingUrl(String streamingUrl) {
		this.streamingUrl = streamingUrl;
	}

	public int getLeafNodesCount() {
		return leafNodesCount;
	}

	public void setLeafNodesCount(int leafNodesCount) {
		this.leafNodesCount = leafNodesCount;
	}
}
