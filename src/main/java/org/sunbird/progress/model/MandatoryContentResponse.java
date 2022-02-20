package org.sunbird.progress.model;

import java.util.HashMap;
import java.util.Map;

public class MandatoryContentResponse {
	private boolean mandatoryCourseCompleted;
	private Map<String, MandatoryContentInfo> contentDetails;

	public void addContentInfo(String contentId, MandatoryContentInfo contentInfo) {
		if (contentDetails == null) {
			contentDetails = new HashMap<>();
		}
		contentDetails.put(contentId, contentInfo);
	}

	public Map<String, MandatoryContentInfo> getContentDetails() {
		return contentDetails;
	}

	public boolean isMandatoryCourseCompleted() {
		return mandatoryCourseCompleted;
	}

	public void setContentDetails(Map<String, MandatoryContentInfo> contentDetails) {
		this.contentDetails = contentDetails;
	}

	public void setMandatoryCourseCompleted(boolean mandatoryCourseCompleted) {
		this.mandatoryCourseCompleted = mandatoryCourseCompleted;
	}
}
