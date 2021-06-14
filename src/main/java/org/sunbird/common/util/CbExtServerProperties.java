package org.sunbird.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CbExtServerProperties {

    @Value("${wf.service.host}")
    private String wfServiceHost;

    @Value("${wf.service.updateUserProfilePath}")
    private String wfServicePath;

    @Value("${user.enable.multidept.mapping}")
    private boolean isUserMultiMapDeptEnabled;

    @Value("${sb.service.url}")
    private String sbUrl;

    @Value("${sb.hub.graph.service.url}")
    private String sbHubGraphServiceUrl;

    @Value("${spring.data.cassandra.sb.username}")
    private String cassandraUserName;

    @Value("${spring.data.cassandra.sb.password}")
    private String cassandraPassword;

    @Value("${spring.data.cassandra.sb.keyspace-name}")
    private String sbExtKeyspace;

    @Value("${sunbird.course.service.host}")
    private String courseServiceHost;

    @Value("${progress.api.endpoint}")
    private String progressReadEndPoint;

    @Value("${sb.api.key}")
    private String sbApiKey;

    @Value("${es.host}")
    private String esHost;

    @Value("${es.port}")
    private String esPort;

    @Value("${es.username}")
    private String esUser;

    @Value("${es.password}")
    private String esPassword;

    @Value("${es.profile.index}")
    private String esProfileIndex;

    @Value("${es.profile.index.type}")
    private String esProfileIndexType;

    @Value("${es.profile.source.fields}")
    private String[] esProfileSourceFields;

    @Value("${org.create.endpoint}")
    private String orgCreateEndPoint;

    @Value("${kafka.topics.org.creation}")
    private String orgCreationKafkaTopic;
    
    @Value("${km.base.host}")
    private String kmBaseHost;
    
    @Value("${km.base.framework.path}")
    private String kmFrameWorkPath;
    
    @Value("${frac.host}")
    private String fracHost;
    
    @Value("${frac.node.path}")
    private String fracNodePath;
    
    @Value("${frac.activity.path}")
    private String fracActivityPath;
    
    @Value("${frac.node.source}")
    private String fracSource;
    
    @Value("${frac.node.source.useDeptName}")
    private boolean fracSourceUseDeptName;
    
    @Value("${igot.taxonomy.framework.name}")
    private String taxonomyFrameWorkName;
    
    @Value("${igot.taxonomy.category.name}")
    private String taxonomyCategoryName;

    @Value("${kafka.topics.userrole.audit}")
    private String userRoleAuditTopic;
    
    @Value("${content-service-host}")
	private String contentHost;

	@Value("${content-hierarchy-endpoint}")
	private String hierarchyEndPoint;
	
	@Value("${participants.api.endpoint}")
	private String participantsEndPoint;

    @Value("${course.batch.create.endpoint}")
    private String courseBatchCreateEndpoint;

    @Value("${user.course.list}")
    private String userCoursesList;


    @Value("${user.course.enroll}")
    private String userCourseEnroll;

    @Value("${content-create-endpoint}")
    private String contentCreateEndPoint;

    @Value("${content-upload-endpoint}")
    private String contentUploadEndPoint;
	
    public String getContentHost() {
		return contentHost;
	}

	public void setContentHost(String contentHost) {
		this.contentHost = contentHost;
	}

	public String getHierarchyEndPoint() {
		return hierarchyEndPoint;
	}

	public void setHierarchyEndPoint(String hierarchyEndPoint) {
		this.hierarchyEndPoint = hierarchyEndPoint;
	}

	public String getWfServiceHost() {
        return wfServiceHost;
    }

    public void setWfServiceHost(String wfServiceHost) {
        this.wfServiceHost = wfServiceHost;
    }

    public String getWfServicePath() {
        return wfServicePath;
    }

    public void setWfServicePath(String wfServicePath) {
        this.wfServicePath = wfServicePath;
    }

    public boolean isUserMultiMapDeptEnabled() {
        return isUserMultiMapDeptEnabled;
    }

    public void setUserMultiMapDeptEnabled(boolean isUserMultiMapDeptEnabled) {
        this.isUserMultiMapDeptEnabled = isUserMultiMapDeptEnabled;
    }

    public String getSbUrl() {
        return sbUrl;
    }

    public void setSbUrl(String sbUrl) {
        this.sbUrl = sbUrl;
    }

    public String getSbHubGraphServiceUrl() {
        return sbHubGraphServiceUrl;
    }

    public void setSbHubGraphServiceUrl(String sbHubGraphServiceUrl) {
        this.sbHubGraphServiceUrl = sbHubGraphServiceUrl;
    }

    public String getCassandraUserName() {
        return cassandraUserName;
    }

    public void setCassandraUserName(String cassandraUserName) {
        this.cassandraUserName = cassandraUserName;
    }

    public String getCassandraPassword() {
        return cassandraPassword;
    }

    public void setCassandraPassword(String cassandraPassword) {
        this.cassandraPassword = cassandraPassword;
    }

    public String getSbExtKeyspace() {
        return sbExtKeyspace;
    }

    public void setSbExtKeyspace(String sbExtKeyspace) {
        this.sbExtKeyspace = sbExtKeyspace;
    }

    public String getCourseServiceHost() {
        return courseServiceHost;
    }

    public void setCourseServiceHost(String courseServiceHost) {
        this.courseServiceHost = courseServiceHost;
    }

    public String getProgressReadEndPoint() {
        return progressReadEndPoint;
    }

    public void setProgressReadEndPoint(String progressReadEndPoint) {
        this.progressReadEndPoint = progressReadEndPoint;
    }

    public String getSbApiKey() {
        return sbApiKey;
    }

    public void setSbApiKey(String sbApiKey) {
        this.sbApiKey = sbApiKey;
    }

    public String getEsHost() {
        return esHost;
    }

    public void setEsHost(String esHost) {
        this.esHost = esHost;
    }

    public String getEsPort() {
        return esPort;
    }

    public void setEsPort(String esPort) {
        this.esPort = esPort;
    }

    public String getEsUser() {
        return esUser;
    }

    public void setEsUser(String esUser) {
        this.esUser = esUser;
    }

    public String getEsPassword() {
        return esPassword;
    }

    public void setEsPassword(String esPassword) {
        this.esPassword = esPassword;
    }

    public String getEsProfileIndex() {
        return esProfileIndex;
    }

    public void setEsProfileIndex(String esProfileIndex) {
        this.esProfileIndex = esProfileIndex;
    }

    public String getEsProfileIndexType() {
        return esProfileIndexType;
    }

    public void setEsProfileIndexType(String esProfileIndexType) {
        this.esProfileIndexType = esProfileIndexType;
    }

    public String[] getEsProfileSourceFields() {
        return esProfileSourceFields;
    }

    public void setEsProfileSourceFields(String[] esProfileSourceFields) {
        this.esProfileSourceFields = esProfileSourceFields;
    }

    public String getOrgCreateEndPoint() {
        return orgCreateEndPoint;
    }

    public void setOrgCreateEndPoint(String orgCreateEndPoint) {
        this.orgCreateEndPoint = orgCreateEndPoint;
    }

    public String getOrgCreationKafkaTopic() {
        return orgCreationKafkaTopic;
    }

    public void setOrgCreationKafkaTopic(String orgCreationKafkaTopic) {
        this.orgCreationKafkaTopic = orgCreationKafkaTopic;
    }
    
    public String getKmBaseHost() {
		return kmBaseHost;
	}

	public void setKmBaseHost(String kmBaseHost) {
		this.kmBaseHost = kmBaseHost;
	}

	public String getKmFrameWorkPath() {
		return kmFrameWorkPath;
	}

	public void setKmFrameWorkPath(String kmFrameWorkPath) {
		this.kmFrameWorkPath = kmFrameWorkPath;
	}
	
	public String getFracHost() {
		return fracHost;
	}

	public void setFracHost(String fracHost) {
		this.fracHost = fracHost;
	}

	public String getFracNodePath() {
		return fracNodePath;
	}

	public void setFracNodePath(String fracNodePath) {
		this.fracNodePath = fracNodePath;
	}
	
	public String getFracActivityPath() {
		return fracActivityPath;
	}

	public void setFracActivityPath(String fracActivityPath) {
		this.fracActivityPath = fracActivityPath;
	}

	public String getFracSource() {
		return fracSource;
	}

	public void setFracSource(String fracSource) {
		this.fracSource = fracSource;
	}

	public boolean isFracSourceUseDeptName() {
		return fracSourceUseDeptName;
	}

	public void setFracSourceUseDeptName(boolean fracSourceUseDeptName) {
		this.fracSourceUseDeptName = fracSourceUseDeptName;
	}
	
	public String getTaxonomyFrameWorkName() {
		return taxonomyFrameWorkName;
	}

	public void setTaxonomyFrameWorkName(String taxonomyFrameWorkName) {
		this.taxonomyFrameWorkName = taxonomyFrameWorkName;
	}

	public String getTaxonomyCategoryName() {
		return taxonomyCategoryName;
	}

	public void setTaxonomyCategoryName(String taxonomyCategoryName) {
		this.taxonomyCategoryName = taxonomyCategoryName;
	}

    public String getUserRoleAuditTopic() {
        return userRoleAuditTopic;
    }

    public void setUserRoleAuditTopic(String userRoleAuditTopic) {
        this.userRoleAuditTopic = userRoleAuditTopic;
    }
    
    public String getParticipantsEndPoint() {
		return participantsEndPoint;
	}

	public void setParticipantsEndPoint(String participantsEndPoint) {
		this.participantsEndPoint = participantsEndPoint;
	}

    public String getCourseBatchCreateEndpoint() {
        return courseBatchCreateEndpoint;
    }

    public void setCourseBatchCreateEndpoint(String courseBatchCreateEndpoint) {
        this.courseBatchCreateEndpoint = courseBatchCreateEndpoint;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("SB-CB-Ext Server Properties: ");
        str.append("[wfServiceHost=").append(wfServiceHost).append("],");
        str.append("[wfServicePath=").append(wfServicePath).append("],");
        str.append("[isUserMultiMapDeptEnabled=").append(isUserMultiMapDeptEnabled).append("],");
        str.append("[sbUrl=").append(sbUrl).append("],");
        str.append("[sbHubGraphServiceUrl=").append(sbHubGraphServiceUrl).append("]");
        return str.toString();
    }

    public String getUserCoursesList() {
        return userCoursesList;
    }

    public void setUserCoursesList(String userCoursesList) {
        this.userCoursesList = userCoursesList;
    }

    public String getUserCourseEnroll() {
        return userCourseEnroll;
    }

    public void setUserCourseEnroll(String userCourseEnroll) {
        this.userCourseEnroll = userCourseEnroll;
    }

    public String getContentCreateEndPoint() {
        return contentCreateEndPoint;
    }

    public void setContentCreateEndPoint(String contentCreateEndPoint) {
        this.contentCreateEndPoint = contentCreateEndPoint;
    }

    public String getContentUploadEndPoint() {
        return contentUploadEndPoint;
    }

    public void setContentUploadEndPoint(String contentUploadEndPoint) {
        this.contentUploadEndPoint = contentUploadEndPoint;
    }
}