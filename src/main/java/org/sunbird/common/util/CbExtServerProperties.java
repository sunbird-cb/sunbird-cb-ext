package org.sunbird.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CbExtServerProperties {

	@Value("${wf.service.host}")
	private String wfServiceHost;

	@Value("${wf.service.transitionPath}")
	private String wfServiceTransitionPath;

	@Value("${wf.service.updateUserProfilePath}")
	private String wfServicePath;

	@Value("${lms.system.settings.path}")
	private String lmsSystemSettingsPath;

	@Value("${lms.user.update.path}")
	private String lmsUserUpdatePath;

	@Value("${lms.user.read.path}")
	private String lmsUserReadPath;

	@Value("${user.enable.multidept.mapping}")
	private boolean isUserMultiMapDeptEnabled;

	@Value("${sb.service.url}")
	private String sbUrl;

	@Value("${sb.service.user.create.path}")
	private String lmsUserCreatePath;

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

	@Value("${kafka.topics.userutility.telemetry.event}")
	private String userUtilityTopic;

	@Value("${sunbird.cb.ext.version}")
	private String sunbirdCbExtVersion;

	@Value("${userutility.telemetry.event.pdata.id}")
	private String firstLoginId;

	@Value("${userutility.telemetry.event.pdata.pid}")
	private String firstLoginPid;

	@Value("${assessment.host}")
	private String assessmentHost;

	@Value("${assessment.hierarchy.read.path}")
	private String assessmentHierarchyReadPath;

	@Value("${assessment.question.list.path}")
	private String assessmentQuestionListPath;

	@Value("${assessment.read.assessmentLevel.params}")
	private String assessmentLevelParams;

	@Value("${assessment.read.sectionLevel.params}")
	private String assessmentSectionParams;

	@Value("${assessment.read.questionLevel.params}")
	private String assessmentQuestionParams;

	@Value("${assessment.read.min.question.params}")
	private String assessmentMinQuestionParams;

	@Value("${user.assessment.submission.duration}")
	private String userAssessmentSubmissionDuration;

	@Value("${es.user.registration.index}")
	private String userRegistrationIndex;

	@Value("${es.org.onboarding.index}")
	private String orgOnboardingIndex;

	@Value("${user.registration.code.prefix}")
	private String userRegCodePrefix;

	@Value("${kafka.topics.user.registration.register.event}")
	private String userRegistrationTopic;

	@Value("${kafka.topics.user.registration.auto.createUser}")
	private String userRegistrationAutoCreateUserTopic;

	@Value("${user.registration.domain}")
	private String userRegistrationDomain;

	@Value("${user.registration.dept.exclude.list}")
	private String userRegistrationDeptExcludeList;

	@Value("${user.registration.workflow.service.name}")
	private String userRegistrationWorkFlowServiceName;

	@Value("${user.registration.subject}")
	private String userRegistrationSubject;

	@Value("${user.registration.title}")
	private String userRegistrationTitle;

	@Value("${user.registration.status}")
	private String userRegistrationStatus;

	@Value("${user.registration.thankyou.message}")
	private String userRegistrationThankyouMessage;

	@Value("${user.registration.initiated.message}")
	private String userRegistrationInitiatedMessage;

	@Value("${user.registration.approved.message}")
	private String userRegistrationApprovedMessage;

	@Value("${user.registration.failed.message}")
	private String userRegistrationFailedMessage;

	@Value("${user.registeration.route.button.name}")
	private String userRegisterationButtonName;

	@Value("${user.registration.domain.name}")
	private String userRegistrationDomainName;

	@Value("${user.registration.preApproved.domain}")
	private String userRegistrationPreApprovedDomainList;

	@Value("${sb.discussion.hub.host}")
	private String discussionHubHost;

	@Value("${sb.node.bb.user.create.path}")
	private String discussionHubCreateUserPath;

	@Value("${sb.service.reset.password.path}")
	private String sbResetPasswordPath;

	@Value("${sb.service.send.notify.email.path}")
	private String sbSendNotificationEmailPath;

	@Value("${sb.service.assign.role.path}")
	private String sbAssignRolePath;

	@Value("${user.registration.dept.master.list.file}")
	private String masterOrgListFileName;

	@Value("${user.registration.custodian.orgId}")
	private String custodianOrgId;

	@Value("${user.registration.custodian.orgName}")
	private String custodianOrgName;

	@Value("${user.position.master.list.file}")
	private String masterPositionListFileName;

	@Value("${user.registration.welcome.email.template}")
	private String welcomeEmailTemplate;

	@Value("${user.registration.welcome.email.subject}")
	private String welcomeEmailSubject;

	@Value("${sb.org.create.path}")
	private String lmsOrgCreatePath;
	
	@Value("${es.user.auto.complete.search.fields}")
	private String esAutoCompleteSearchFields;

	@Value("${es.user.auto.complete.include.fields}")
	private String esAutoCompleteIncludeFields;

    @Value("${sb.service.user.migrate.path}")
	private String lmsUserMigratePath;

    @Value("${km.base.composite.search.fields}")
	private String kmCompositeSearchFields;
	
	@Value("${km.base.composite.search.filters.primaryCategory}")
	private String kmCompositeSearchPrimaryCategoryFilters;

	@Value("${sb.data.sync.path}")
	private String lmsDataSyncPath;
	
	@Value("${sb.es.host}")
	private String sbEsHost;

	@Value("${sb.es.port}")
	private String sbEsPort;

	@Value("${sb.es.username}")
	private String sbEsUser;

	@Value("${sb.es.password}")
	private String sbEsPassword;
	
	@Value("${km.base.content.search}")
	private String kmBaseContentSearch;
	
	@Value("${sb.es.user.profile.index}")
	private String sbEsUserProfileIndex;
	
	public String getUserAssessmentSubmissionDuration() {
		return userAssessmentSubmissionDuration;
	}

	public void setUserAssessmentSubmissionDuration(String userAssessmentSubmissionDuration) {
		this.userAssessmentSubmissionDuration = userAssessmentSubmissionDuration;
	}

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

	public String getWfServiceTransitionPath() {
		return wfServiceTransitionPath;
	}

	public void setWfServiceTransitionPath(String wfServiceTransitionPath) {
		this.wfServiceTransitionPath = wfServiceTransitionPath;
	}

	public String getLmsUserUpdatePath() {
		return lmsUserUpdatePath;
	}

	public void setLmsUserUpdatePath(String lmsUserUpdatePath) {
		this.lmsUserUpdatePath = lmsUserUpdatePath;
	}

	public String getLmsSystemSettingsPath() {
		return lmsSystemSettingsPath;
	}

	public void setLmsSystemSettingsPath(String lmsSystemSettingsPath) {
		this.lmsSystemSettingsPath = lmsSystemSettingsPath;
	}

	public String getLmsUserReadPath() {
		return lmsUserReadPath;
	}

	public void setLmsUserReadPath(String lmsUserReadPath) {
		this.lmsUserReadPath = lmsUserReadPath;
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

	public String getLmsUserCreatePath() {
		return lmsUserCreatePath;
	}

	public void setLmsUserCreatePath(String lmsUserCreatePath) {
		this.lmsUserCreatePath = lmsUserCreatePath;
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

	public String getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(String redisPort) {
		this.redisPort = redisPort;
	}

	public String getRedisHostName() {
		return redisHostName;
	}

	public void setRedisHostName(String redisHostName) {
		this.redisHostName = redisHostName;
	}

	public String getRedisTimeout() {
		return redisTimeout;
	}

	public void setRedisTimeout(String redisTimeout) {
		this.redisTimeout = redisTimeout;
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

	public String getContentHierarchyDetailEndPoint() {
		return contentHierarchyDetailEndPoint;
	}

	public void setContentHierarchyDetailEndPoint(String contentHierarchyDetailEndPoint) {
		this.contentHierarchyDetailEndPoint = contentHierarchyDetailEndPoint;
	}

	public String getContentDefaultChannelId() {
		return contentDefaultChannelId;
	}

	public void setContentDefaultChannelId(String contentDefaultChannelId) {
		this.contentDefaultChannelId = contentDefaultChannelId;
	}

	public String getUserSearchEndPoint() {
		return userSearchEndPoint;
	}

	public void setUserSearchEndPoint(String userSearchEndPoint) {
		this.userSearchEndPoint = userSearchEndPoint;
	}

	public String getContentDefaultOrgId() {
		return contentDefaultOrgId;
	}

	public void setContentDefaultOrgId(String contentDefaultOrgId) {
		this.contentDefaultOrgId = contentDefaultOrgId;
	}

	public String getKafkaTopicWatEvent() {
		return kafkaTopicWatEvent;
	}

	public void setKafkaTopicWatEvent(String kafkaTopicWatEvent) {
		this.kafkaTopicWatEvent = kafkaTopicWatEvent;
	}

	public String getTelemetryBaseUrl() {
		return telemetryBaseUrl;
	}

	public void setTelemetryBaseUrl(String telemetryBaseUrl) {
		this.telemetryBaseUrl = telemetryBaseUrl;
	}

	public String getTelemetryEndpoint() {
		return telemetryEndpoint;
	}

	public void setTelemetryEndpoint(String telemetryEndpoint) {
		this.telemetryEndpoint = telemetryEndpoint;
	}

	public String getWatTelemetryEnv() {
		return watTelemetryEnv;
	}

	public void setWatTelemetryEnv(String watTelemetryEnv) {
		this.watTelemetryEnv = watTelemetryEnv;
	}

	public String getSbOrgSearchPath() {
		return sbOrgSearchPath;
	}

	public void setSbOrgSearchPath(String sbOrgSearchPath) {
		this.sbOrgSearchPath = sbOrgSearchPath;
	}

	public String getKmCompositeSearchPath() {
		return kmCompositeSearchPath;
	}

	public void setKmCompositeSearchPath(String kmCompositeSearchPath) {
		this.kmCompositeSearchPath = kmCompositeSearchPath;
	}

	public String getFracSearchPath() {
		return fracSearchPath;
	}

	public void setFracSearchPath(String fracSearchPath) {
		this.fracSearchPath = fracSearchPath;
	}

	public long getCacheMaxTTL() {
		return cacheMaxTTL;
	}

	public void setCacheMaxTTL(long cacheMaxTTL) {
		this.cacheMaxTTL = cacheMaxTTL;
	}

	public String getAzureContainerName() {
		return azureContainerName;
	}

	public void setAzureContainerName(String azureContainerName) {
		this.azureContainerName = azureContainerName;
	}

	public String getAzureTypeName() {
		return azureTypeName;
	}

	public void setAzureTypeName(String azureTypeName) {
		this.azureTypeName = azureTypeName;
	}

	public String getAzureIdentityName() {
		return azureIdentityName;
	}

	public void setAzureIdentityName(String azureIdentityName) {
		this.azureIdentityName = azureIdentityName;
	}

	public String getAzureStorageKey() {
		return azureStorageKey;
	}

	public void setAzureStorageKey(String azureStorageKey) {
		this.azureStorageKey = azureStorageKey;
	}

	public String getUserUtilityTopic() {
		return userUtilityTopic;
	}

	public void setUserUtilityTopic(String userUtilityTopic) {
		this.userUtilityTopic = userUtilityTopic;
	}

	public String getVersion() {
		return sunbirdCbExtVersion;
	}

	public void setVersion(String sunbirdCbExtVersion) {
		this.sunbirdCbExtVersion = sunbirdCbExtVersion;
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

	public String getAssessmentHost() {
		return assessmentHost;
	}

	public void setAssessmentHost(String assessmentHost) {
		this.assessmentHost = assessmentHost;
	}

	public String getAssessmentHierarchyReadPath() {
		return assessmentHierarchyReadPath;
	}

	public void setAssessmentHierarchyReadPath(String assessmentHierarchyReadPath) {
		this.assessmentHierarchyReadPath = assessmentHierarchyReadPath;
	}

	public String getAssessmentQuestionListPath() {
		return assessmentQuestionListPath;
	}

	public void setAssessmentQuestionListPath(String assessmentQuestionListPath) {
		this.assessmentQuestionListPath = assessmentQuestionListPath;
	}

	public String getSunbirdCbExtVersion() {
		return sunbirdCbExtVersion;
	}

	public void setSunbirdCbExtVersion(String sunbirdCbExtVersion) {
		this.sunbirdCbExtVersion = sunbirdCbExtVersion;
	}

	public List<String> getAssessmentLevelParams() {
		return Arrays.asList(assessmentLevelParams.split(",", -1));
	}

	public void setAssessmentLevelParams(String assessmentLevelParams) {
		this.assessmentLevelParams = assessmentLevelParams;
	}

	public List<String> getAssessmentSectionParams() {
		return Arrays.asList(assessmentSectionParams.split(",", -1));
	}

	public void setAssessmentSectionParams(String assessmentSectionParams) {
		this.assessmentSectionParams = assessmentSectionParams;
	}

	public List<String> getAssessmentQuestionParams() {
		return Arrays.asList(assessmentQuestionParams.split(",", -1));
	}

	public void setAssessmentQuestionParams(String assessmentQuestionParams) {
		this.assessmentQuestionParams = assessmentQuestionParams;
	}

	public List<String> getAssessmentMinQuestionParams() {
		return Arrays.asList(assessmentMinQuestionParams.split(",", -1));
	}

	public void setAssessmentMinQuestionParams(String assessmentMinQuestionParams) {
		this.assessmentMinQuestionParams = assessmentMinQuestionParams;
	}

	public String getUserRegistrationIndex() {
		return userRegistrationIndex;
	}

	public void setUserRegistrationIndex(String userRegistrationIndex) {
		this.userRegistrationIndex = userRegistrationIndex;
	}

	public String getUserRegCodePrefix() {
		return userRegCodePrefix;
	}

	public void setUserRegCodePrefix(String userRegCodePrefix) {
		this.userRegCodePrefix = userRegCodePrefix;
	}

	public String getUserRegistrationTopic() {
		return userRegistrationTopic;
	}

	public void setUserRegistrationTopic(String userRegistrationTopic) {
		this.userRegistrationTopic = userRegistrationTopic;
	}

	public String getUserRegistrationAutoCreateUserTopic() {
		return userRegistrationAutoCreateUserTopic;
	}

	public void setUserRegistrationAutoCreateUserTopic(String userRegistrationAutoCreateUserTopic) {
		this.userRegistrationAutoCreateUserTopic = userRegistrationAutoCreateUserTopic;
	}

	public List<String> getUserRegistrationDomain() {
		return Arrays.asList(userRegistrationDomain.split(",", -1));
	}

	public void setUserRegistrationDomain(String userRegistrationDomain) {
		this.userRegistrationDomain = userRegistrationDomain;
	}

	public void setUserRegistrationDeptExcludeList(String userRegistrationDeptExcludeList) {
		this.userRegistrationDeptExcludeList = userRegistrationDeptExcludeList;
	}

	public List<String> getUserRegistrationDeptExcludeList() {
		return Arrays.asList(userRegistrationDeptExcludeList.split(",", -1));
	}

	public String getUserRegistrationWorkFlowServiceName() {
		return userRegistrationWorkFlowServiceName;
	}

	public void setUserRegistrationWorkFlowServiceName(String userRegistrationWorkFlowServiceName) {
		this.userRegistrationWorkFlowServiceName = userRegistrationWorkFlowServiceName;
	}

	public String getUserRegistrationTitle() {
		return userRegistrationTitle;
	}

	public void setUserRegistrationTitle(String userRegistrationTitle) {
		this.userRegistrationTitle = userRegistrationTitle;
	}

	public String getUserRegistrationStatus() {
		return userRegistrationStatus;
	}

	public void setUserRegistrationStatus(String userRegistrationStatus) {
		this.userRegistrationStatus = userRegistrationStatus;
	}

	public String getUserRegistrationThankyouMessage() {
		return userRegistrationThankyouMessage;
	}

	public void setUserRegistrationThankyouMessage(String userRegistrationThankyouMessage) {
		this.userRegistrationThankyouMessage = userRegistrationThankyouMessage;
	}

	public String getUserRegistrationInitiatedMessage() {
		return userRegistrationInitiatedMessage;
	}

	public void setUserRegistrationInitiatedMessage(String userRegistrationInitiatedMessage) {
		this.userRegistrationInitiatedMessage = userRegistrationInitiatedMessage;
	}

	public String getUserRegistrationApprovedMessage() {
		return userRegistrationApprovedMessage;
	}

	public void setUserRegistrationApprovedMessage(String userRegistrationApprovedMessage) {
		this.userRegistrationApprovedMessage = userRegistrationApprovedMessage;
	}

	public String getUserRegistrationFailedMessage() {
		return userRegistrationFailedMessage;
	}

	public void setUserRegistrationFailedMessage(String userRegistrationFailedMessage) {
		this.userRegistrationFailedMessage = userRegistrationFailedMessage;
	}

	public String getUserRegisterationButtonName() {
		return userRegisterationButtonName;
	}

	public void setUserRegisterationButtonName(String userRegisterationButtonName) {
		this.userRegisterationButtonName = userRegisterationButtonName;
	}

	public String getUserRegistrationSubject() {
		return userRegistrationSubject;
	}

	public void setUserRegistrationSubject(String userRegistrationSubject) {
		this.userRegistrationSubject = userRegistrationSubject;
	}

	public String getUserRegistrationDomainName() {
		return userRegistrationDomainName;
	}

	public void setUserRegistrationDomainName(String userRegistrationDomainName) {
		this.userRegistrationDomainName = userRegistrationDomainName;
	}

	public List<String> getUserRegistrationPreApprovedDomainList() {
		return Arrays.asList(userRegistrationPreApprovedDomainList.split(",", -1));
	}

	public void setUserRegistrationPreApprovedDomainList(String userRegistrationPreApprovedDomainList) {
		this.userRegistrationPreApprovedDomainList = userRegistrationPreApprovedDomainList;
	}

	public String getDiscussionHubHost() {
		return discussionHubHost;
	}

	public void setDiscussionHubHost(String discussionHubHost) {
		this.discussionHubHost = discussionHubHost;
	}

	public String getDiscussionHubCreateUserPath() {
		return discussionHubCreateUserPath;
	}

	public void setDiscussionHubCreateUserPath(String discussionHubCreateUserPath) {
		this.discussionHubCreateUserPath = discussionHubCreateUserPath;
	}

	public String getSbResetPasswordPath() {
		return sbResetPasswordPath;
	}

	public void setSbResetPasswordPath(String sbResetPasswordPath) {
		this.sbResetPasswordPath = sbResetPasswordPath;
	}

	public String getSbSendNotificationEmailPath() {
		return sbSendNotificationEmailPath;
	}

	public void setSbSendNotificationEmailPath(String sbSendNotificationEmailPath) {
		this.sbSendNotificationEmailPath = sbSendNotificationEmailPath;
	}

	public String getSbAssignRolePath() {
		return sbAssignRolePath;
	}

	public void setSbAssignRolePath(String sbAssignRolePath) {
		this.sbAssignRolePath = sbAssignRolePath;
	}

	public String getMasterOrgListFileName() {
		return masterOrgListFileName;
	}

	public void setMasterOrgListFileName(String masterOrgListFileName) {
		this.masterOrgListFileName = masterOrgListFileName;
	}

	public String getCustodianOrgId() {
		return custodianOrgId;
	}

	public void setCustodianOrgId(String custodianOrgId) {
		this.custodianOrgId = custodianOrgId;
	}

	public String getCustodianOrgName() {
		return custodianOrgName;
	}

	public void setCustodianOrgName(String custodianOrgName) {
		this.custodianOrgName = custodianOrgName;
	}

	public String getMasterPositionListFileName() {
		return masterPositionListFileName;
	}

	public void setMasterPositionListFileName(String masterPositionListFileName) {
		this.masterPositionListFileName = masterPositionListFileName;
	}

	public String getWelcomeEmailTemplate() {
		return welcomeEmailTemplate;
	}

	public void setWelcomeEmailTemplate(String welcomeEmailTemplate) {
		this.welcomeEmailTemplate = welcomeEmailTemplate;
	}

	public String getWelcomeEmailSubject() {
		return welcomeEmailSubject;
	}

	public void setWelcomeEmailSubject(String welcomeEmailSubject) {
		this.welcomeEmailSubject = welcomeEmailSubject;
	}

	public String getOrgOnboardingIndex() {
		return orgOnboardingIndex;
	}

	public void setOrgOnboardingIndex(String orgOnboardingIndex) {
		this.orgOnboardingIndex = orgOnboardingIndex;
	}

	public String getLmsOrgCreatePath() {
		return lmsOrgCreatePath;
	}

	public void setLmsOrgCreatePath(String lmsOrgCreatePath) {
		this.lmsOrgCreatePath = lmsOrgCreatePath;
	}

	public String getLmsUserMigratePath() {
		return lmsUserMigratePath;
	}

	public void setLmsUserMigratePath(String lmsUserSelfMigratePath) {
		this.lmsUserMigratePath = lmsUserSelfMigratePath;
	}

	public String getLmsDataSyncPath() {
		return lmsDataSyncPath;
	}

	public void setLmsDataSyncPath(String lmsDataSyncPath) {
		this.lmsDataSyncPath = lmsDataSyncPath;
	}

	public List<String> getKmCompositeSearchFields() {
			return Arrays.asList(kmCompositeSearchFields.split(",", -1));
	}

	public void setKmCompositeSearchFields(String kmCompositeSearchFields) {
		this.kmCompositeSearchFields = kmCompositeSearchFields;
	}

	public List<String> getKmCompositeSearchPrimaryCategoryFilters() {
		return Arrays.asList(kmCompositeSearchPrimaryCategoryFilters.split(",", -1));
	}

	public void setKmCompositeSearchPrimaryCategoryFilters(String kmCompositeSearchPrimaryCategoryFilters) {
		this.kmCompositeSearchPrimaryCategoryFilters = kmCompositeSearchPrimaryCategoryFilters;
	}

	public List<String> getEsAutoCompleteSearchFields() {
		return Arrays.asList(esAutoCompleteSearchFields.split(",", -1));
	}

	public void setEsAutoCompleteSearchFields(String esAutoCompleteSearchFields) {
		this.esAutoCompleteSearchFields = esAutoCompleteSearchFields;
	}

	public String[] getEsAutoCompleteIncludeFields() {
		return esAutoCompleteIncludeFields.split(",", -1);
	}

	public void setEsAutoCompleteIncludeFields(String esAutoCompleteIncludeFields) {
		this.esAutoCompleteIncludeFields = esAutoCompleteIncludeFields;
	}

	public String getSbEsHost() {
		return sbEsHost;
	}

	public void setSbEsHost(String sbEsHost) {
		this.sbEsHost = sbEsHost;
	}

	public String getSbEsPort() {
		return sbEsPort;
	}

	public void setSbEsPort(String sbEsPort) {
		this.sbEsPort = sbEsPort;
	}

	public String getSbEsUser() {
		return sbEsUser;
	}

	public void setSbEsUser(String sbEsUser) {
		this.sbEsUser = sbEsUser;
	}

	public String getSbEsPassword() {
		return sbEsPassword;
	}

	public void setSbEsPassword(String sbEsPassword) {
		this.sbEsPassword = sbEsPassword;
	}

	public String getKmBaseContentSearch() {
		return kmBaseContentSearch;
	}

	public void setKmBaseContentSearch(String kmBaseContentSearch) {
		this.kmBaseContentSearch = kmBaseContentSearch;
	}

	public String getSbEsUserProfileIndex() {
		return sbEsUserProfileIndex;
	}

	public void setSbEsUserProfileIndex(String sbEsUserProfileIndex) {
		this.sbEsUserProfileIndex = sbEsUserProfileIndex;
	}
}