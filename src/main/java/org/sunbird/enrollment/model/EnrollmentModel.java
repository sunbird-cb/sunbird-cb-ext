package org.sunbird.enrollment.model;

public class EnrollmentModel {
    private String eid;
    private Long ets;
    private String mid;
    private String actorType;
    private String actorId ;
    private String contextVer;
    private String contextId;

    public String getEid() {
        return "BE_JOB_REQUEST";
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public Long getEts() {
        return ets= Long.valueOf("1663569863271");
    }

    public void setEts(String ets) {
        this.ets = Long.valueOf(ets);
    }

    public String getMid() {
        return "LMS.1563788371969.590c5fa0-0ce8-46ed-bf6c-681c0a1fdac8";
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getActorType() {
        return "System";
    }

    public void setActorType(Float actorType) {
        this.actorType = String.valueOf(actorType);
    }

    public String getActorId() {
        return "Course Batch Updater";
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getContextVer() {
        return "1.0";
    }

    public void setContextVer(String contextVer) {
        this.contextVer = contextVer;
    }

    public String getContextId() {
        return "org.sunbird.platform";
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }


}
