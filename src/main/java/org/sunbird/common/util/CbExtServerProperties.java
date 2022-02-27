package org.sunbird.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CbExtServerProperties {

	@Value("${wf.service.host}")
	private String wfServiceHost;

	@Value("${kafka.topics.userutility.telemetry.event}")
	private String userUtilityTopic;

	@Value("${version}")
	private String version;

	@Value("${userutility.telemetry.event.pdata.id}")
	private String firstLoginId;

	@Value("${userutility.telemetry.event.pdata.pid}")
	private String firstLoginPid;

	@Value("${wf.service.updateUserProfilePath}")
	private String wfServicePath;

	@Value("${user.enable.multidept.mapping}")
	private boolean isUserMultiMapDeptEnabled;

	@Value("${sb.service.url}")
	private String sbUrl;

	@Value("${sunbird.user.search.endpoint}")
	private String userSearchEndPoint;

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

	@Value("${content-hierarchy-detail}")
	private String contentHierarchyDetailEndPoint;

	@Value("${content.default.channelId}")
	private String contentDefaultChannelId;

	@Value("${content.default.orgId}")
	private String contentDefaultOrgId;

	@Value("${kafka.topics.wat.telemetry.event}")
	private String kafkaTopicWatEvent;

	@Value("${sunbird.telemetry.base.url}")
	private String telemetryBaseUrl;

	@Value("${sunbird.telemetry.endpoint}")
	private String telemetryEndpoint;

	@Value("${wat.telemetry.env}")
	private String watTelemetryEnv;

	@Value("${sb.org.search.path}")
	private String sbOrgSearchPath;

	@Value("${km.base.composite.search.path}")
	private String kmCompositeSearchPath;

	@Value("${frac.search.path}")
	private String fracSearchPath;

	@Value("${cache.max.ttl}")
	private long cacheMaxTTL;

	@Value("${azure.container.name}")
	private String azureContainerName;

	@Value("${azure.type.name}")
	private String azureTypeName;

	@Value("${azure.identity.name}")
	private String azureIdentityName;

	@Value("${azure.storage.key}")
	private String azureStorageKey;

	@Value("${redis.host.name}")
	private String redisHostName;

	@Value("${redis.port}")
	private String redisPort;

	@Value("${redis.timeout}")
	private String redisTimeout;

	public String getAzureContainerName() {
		return azureContainerName;
	}

	public String getAzureIdentityName() {
		return azureIdentityName;
	}

	public String getAzureStorageKey() {
		return azureStorageKey;
	}

	public String getAzureTypeName() {
		return azureTypeName;
	}

	public long getCacheMaxTTL() {
		return cacheMaxTTL;
	}

	public String getCassandraPassword() {
		return cassandraPassword;
	}

	public String getCassandraUserName() {
		return cassandraUserName;
	}

	public String getContentCreateEndPoint() {
		return contentCreateEndPoint;
	}

	public String getContentDefaultChannelId() {
		return contentDefaultChannelId;
	}

	public String getContentDefaultOrgId() {
		return contentDefaultOrgId;
	}

	public String getContentHierarchyDetailEndPoint() {
		return contentHierarchyDetailEndPoint;
	}

	public String getContentHost() {
		return contentHost;
	}

	public String getContentUploadEndPoint() {
		return contentUploadEndPoint;
	}

	public String getCourseBatchCreateEndpoint() {
		return courseBatchCreateEndpoint;
	}

	public String getCourseServiceHost() {
		return courseServiceHost;
	}

	public String getEsHost() {
		return esHost;
	}

	public String getEsPassword() {
		return esPassword;
	}

	public String getEsPort() {
		return esPort;
	}

	public String getEsProfileIndex() {
		return esProfileIndex;
	}

	public String getEsProfileIndexType() {
		return esProfileIndexType;
	}

	public String[] getEsProfileSourceFields() {
		return esProfileSourceFields;
	}

	public String getEsUser() {
		return esUser;
	}

	public String getFracActivityPath() {
		return fracActivityPath;
	}

	public String getFracHost() {
		return fracHost;
	}

	public String getFracNodePath() {
		return fracNodePath;
	}

	public String getFracSearchPath() {
		return fracSearchPath;
	}

	public String getFracSource() {
		return fracSource;
	}

	public String getHierarchyEndPoint() {
		return hierarchyEndPoint;
	}

	public String getKafkaTopicWatEvent() {
		return kafkaTopicWatEvent;
	}

	public String getKmBaseHost() {
		return kmBaseHost;
	}

	public String getKmCompositeSearchPath() {
		return kmCompositeSearchPath;
	}

	public String getKmFrameWorkPath() {
		return kmFrameWorkPath;
	}

	public String getOrgCreateEndPoint() {
		return orgCreateEndPoint;
	}

	public String getOrgCreationKafkaTopic() {
		return orgCreationKafkaTopic;
	}

	public String getParticipantsEndPoint() {
		return participantsEndPoint;
	}

	public String getProgressReadEndPoint() {
		return progressReadEndPoint;
	}

	public String getRedisHostName() {
		return redisHostName;
	}

	public String getRedisPort() {
		return redisPort;
	}

	public String getRedisTimeout() {
		return redisTimeout;
	}

	public String getSbApiKey() {
		return sbApiKey;
	}

	public String getSbExtKeyspace() {
		return sbExtKeyspace;
	}

	public String getSbHubGraphServiceUrl() {
		return sbHubGraphServiceUrl;
	}

	public String getSbOrgSearchPath() {
		return sbOrgSearchPath;
	}

	public String getSbUrl() {
		return sbUrl;
	}

	public String getTaxonomyCategoryName() {
		return taxonomyCategoryName;
	}

	public String getTaxonomyFrameWorkName() {
		return taxonomyFrameWorkName;
	}

	public String getTelemetryBaseUrl() {
		return telemetryBaseUrl;
	}

	public String getTelemetryEndpoint() {
		return telemetryEndpoint;
	}

	public String getUserCourseEnroll() {
		return userCourseEnroll;
	}

	public String getUserCoursesList() {
		return userCoursesList;
	}

	public String getUserRoleAuditTopic() {
		return userRoleAuditTopic;
	}

	public String getUserSearchEndPoint() {
		return userSearchEndPoint;
	}

	public String getWatTelemetryEnv() {
		return watTelemetryEnv;
	}

	public String getWfServiceHost() {
		return wfServiceHost;
	}

	public String getWfServicePath() {
		return wfServicePath;
	}

	public boolean isFracSourceUseDeptName() {
		return fracSourceUseDeptName;
	}

	public boolean isUserMultiMapDeptEnabled() {
		return isUserMultiMapDeptEnabled;
	}

	public void setAzureContainerName(String azureContainerName) {
		this.azureContainerName = azureContainerName;
	}

	public void setAzureIdentityName(String azureIdentityName) {
		this.azureIdentityName = azureIdentityName;
	}

	public void setAzureStorageKey(String azureStorageKey) {
		this.azureStorageKey = azureStorageKey;
	}

	public void setAzureTypeName(String azureTypeName) {
		this.azureTypeName = azureTypeName;
	}

	public void setCacheMaxTTL(long cacheMaxTTL) {
		this.cacheMaxTTL = cacheMaxTTL;
	}

	public void setCassandraPassword(String cassandraPassword) {
		this.cassandraPassword = cassandraPassword;
	}

	public void setCassandraUserName(String cassandraUserName) {
		this.cassandraUserName = cassandraUserName;
	}

	public void setContentCreateEndPoint(String contentCreateEndPoint) {
		this.contentCreateEndPoint = contentCreateEndPoint;
	}

	public void setContentDefaultChannelId(String contentDefaultChannelId) {
		this.contentDefaultChannelId = contentDefaultChannelId;
	}

	public void setContentDefaultOrgId(String contentDefaultOrgId) {
		this.contentDefaultOrgId = contentDefaultOrgId;
	}

	public void setContentHierarchyDetailEndPoint(String contentHierarchyDetailEndPoint) {
		this.contentHierarchyDetailEndPoint = contentHierarchyDetailEndPoint;
	}

	public void setContentHost(String contentHost) {
		this.contentHost = contentHost;
	}

	public void setContentUploadEndPoint(String contentUploadEndPoint) {
		this.contentUploadEndPoint = contentUploadEndPoint;
	}

	public void setCourseBatchCreateEndpoint(String courseBatchCreateEndpoint) {
		this.courseBatchCreateEndpoint = courseBatchCreateEndpoint;
	}

	public void setCourseServiceHost(String courseServiceHost) {
		this.courseServiceHost = courseServiceHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public void setEsPassword(String esPassword) {
		this.esPassword = esPassword;
	}

	public void setEsPort(String esPort) {
		this.esPort = esPort;
	}

	public void setEsProfileIndex(String esProfileIndex) {
		this.esProfileIndex = esProfileIndex;
	}

	public void setEsProfileIndexType(String esProfileIndexType) {
		this.esProfileIndexType = esProfileIndexType;
	}

	public void setEsProfileSourceFields(String[] esProfileSourceFields) {
		this.esProfileSourceFields = esProfileSourceFields;
	}

	public void setEsUser(String esUser) {
		this.esUser = esUser;
	}

	public void setFracActivityPath(String fracActivityPath) {
		this.fracActivityPath = fracActivityPath;
	}

	public void setFracHost(String fracHost) {
		this.fracHost = fracHost;
	}

	public void setFracNodePath(String fracNodePath) {
		this.fracNodePath = fracNodePath;
	}

	public void setFracSearchPath(String fracSearchPath) {
		this.fracSearchPath = fracSearchPath;
	}

	public void setFracSource(String fracSource) {
		this.fracSource = fracSource;
	}

	public void setFracSourceUseDeptName(boolean fracSourceUseDeptName) {
		this.fracSourceUseDeptName = fracSourceUseDeptName;
	}

	public void setHierarchyEndPoint(String hierarchyEndPoint) {
		this.hierarchyEndPoint = hierarchyEndPoint;
	}

	public void setKafkaTopicWatEvent(String kafkaTopicWatEvent) {
		this.kafkaTopicWatEvent = kafkaTopicWatEvent;
	}

	public void setKmBaseHost(String kmBaseHost) {
		this.kmBaseHost = kmBaseHost;
	}

	public void setKmCompositeSearchPath(String kmCompositeSearchPath) {
		this.kmCompositeSearchPath = kmCompositeSearchPath;
	}

	public void setKmFrameWorkPath(String kmFrameWorkPath) {
		this.kmFrameWorkPath = kmFrameWorkPath;
	}

	public void setOrgCreateEndPoint(String orgCreateEndPoint) {
		this.orgCreateEndPoint = orgCreateEndPoint;
	}

	public void setOrgCreationKafkaTopic(String orgCreationKafkaTopic) {
		this.orgCreationKafkaTopic = orgCreationKafkaTopic;
	}

	public void setParticipantsEndPoint(String participantsEndPoint) {
		this.participantsEndPoint = participantsEndPoint;
	}

	public void setProgressReadEndPoint(String progressReadEndPoint) {
		this.progressReadEndPoint = progressReadEndPoint;
	}

	public void setRedisHostName(String redisHostName) {
		this.redisHostName = redisHostName;
	}

	public void setRedisPort(String redisPort) {
		this.redisPort = redisPort;
	}

	public void setRedisTimeout(String redisTimeout) {
		this.redisTimeout = redisTimeout;
	}

	public void setSbApiKey(String sbApiKey) {
		this.sbApiKey = sbApiKey;
	}

	public void setSbExtKeyspace(String sbExtKeyspace) {
		this.sbExtKeyspace = sbExtKeyspace;
	}

	public void setSbHubGraphServiceUrl(String sbHubGraphServiceUrl) {
		this.sbHubGraphServiceUrl = sbHubGraphServiceUrl;
	}

	public void setSbOrgSearchPath(String sbOrgSearchPath) {
		this.sbOrgSearchPath = sbOrgSearchPath;
	}

	public void setSbUrl(String sbUrl) {
		this.sbUrl = sbUrl;
	}

	public void setTaxonomyCategoryName(String taxonomyCategoryName) {
		this.taxonomyCategoryName = taxonomyCategoryName;
	}

	public void setTaxonomyFrameWorkName(String taxonomyFrameWorkName) {
		this.taxonomyFrameWorkName = taxonomyFrameWorkName;
	}

	public void setTelemetryBaseUrl(String telemetryBaseUrl) {
		this.telemetryBaseUrl = telemetryBaseUrl;
	}

	public void setTelemetryEndpoint(String telemetryEndpoint) {
		this.telemetryEndpoint = telemetryEndpoint;
	}

	public void setUserCourseEnroll(String userCourseEnroll) {
		this.userCourseEnroll = userCourseEnroll;
	}

	public void setUserCoursesList(String userCoursesList) {
		this.userCoursesList = userCoursesList;
	}

	public void setUserMultiMapDeptEnabled(boolean isUserMultiMapDeptEnabled) {
		this.isUserMultiMapDeptEnabled = isUserMultiMapDeptEnabled;
	}

	public void setUserRoleAuditTopic(String userRoleAuditTopic) {
		this.userRoleAuditTopic = userRoleAuditTopic;
	}

	public void setUserSearchEndPoint(String userSearchEndPoint) {
		this.userSearchEndPoint = userSearchEndPoint;
	}

	public void setWatTelemetryEnv(String watTelemetryEnv) {
		this.watTelemetryEnv = watTelemetryEnv;
	}

	public void setWfServiceHost(String wfServiceHost) {
		this.wfServiceHost = wfServiceHost;
	}

	public void setWfServicePath(String wfServicePath) {
		this.wfServicePath = wfServicePath;
	}

	public String getUserUtilityTopic() {
		return userUtilityTopic;
	}

	public void setUserUtilityTopic(String userUtilityTopic) {
		this.userUtilityTopic = userUtilityTopic;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFirstLoginId() {
		return firstLoginId;
	}

	public void setFirstLoginId(String firstLoginId) {
		this.firstLoginId = firstLoginId;
	}

	public String getFirstLoginPid() {
		return firstLoginPid;
	}

	public void setFirstLoginPid(String firstLoginPid) {
		this.firstLoginPid = firstLoginPid;
	}

}