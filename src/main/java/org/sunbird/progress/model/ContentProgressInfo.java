package org.sunbird.progress.model;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 */
public class ContentProgressInfo {
    private List<String> userId;
    @NotNull
    private List<String> contentId;
    @NotNull
    private String courseId;
    @NotNull
    private String batchId;

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public List<String> getContentId() {
        return contentId;
    }

    public void setContentId(List<String> contentId) {
        this.contentId = contentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}