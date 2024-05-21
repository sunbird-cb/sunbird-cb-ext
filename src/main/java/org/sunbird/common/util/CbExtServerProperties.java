package org.sunbird.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sunbird.common.model.SBApiResponse;

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

	@Value("${lms.user.update.private.path}")
	private String lmsUserUpdatePrivatePath;

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

	@Value("${es.host.list}")
	private String esHostList;

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

	@Value("${cloud.container.name}")
	private String cloudContainerName;

	@Value("${cloud.storage.type.name}")
	private String cloudStorageTypeName;

	@Value("${cloud.storage.key}")
	private String cloudStorageKey;

	@Value("${cloud.storage.secret}")
	private String cloudStorageSecret;

	@Value("${cloud.storage.cephs3.endpoint}")
	private String cloudStorageCephs3Endpoint;

	@Value("${redis.host.name}")
	private String redisHostName;

	@Value("${redis.port}")
	private String redisPort;

	@Value("${cb-plan.update.publish.authorized.roles}")
	private String cbPlanUpdatePublishAuthorizedRoles;

	@Value("${kafka.topic.cbplan.content.request}")
	private String cbplanContentRequestKafkaTopic;

	public String getRedisDataHostName() {
		return redisDataHostName;
	}

	public void setRedisDataHostName(String redisDataHostName) {
		this.redisDataHostName = redisDataHostName;
	}

	public String getRedisDataPort() {
		return redisDataPort;
	}

	public void setRedisDataPort(String redisDataPort) {
		this.redisDataPort = redisDataPort;
	}

	@Value("${redis.data.host.name}")
	private String redisDataHostName;

	@Value("${redis.data.port}")
	private String redisDataPort;

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

	@Value("${sb.es.host.list}")
	private String sbEsHostList;

	@Value("${sb.es.username}")
	private String sbEsUser;

	@Value("${sb.es.password}")
	private String sbEsPassword;

	@Value("${km.base.content.search}")
	private String kmBaseContentSearch;

	@Value("${sb.es.user.profile.index}")
	private String sbEsUserProfileIndex;

	@Value("${sb.service.signup.user}")
	private String lmsUserSignUpPath;

	@Value("${user.bulk.upload.status.fields}")
	private String bulkUploadStatusFields;

	@Value("${user.bulk.upload.container.name}")
	private String bulkUploadContainerName;

	@Value("${user.bulk.upload.email.template}")
	private String bulkUploadEmailTemplate;

	@Value("${user.bulk.upload.email.notification.list}")
	private String bulkUploadEmailNotificationList;

	@Value("${user.bulk.upload.email.notification.subject}")
	private String bulkUploadEmailNotificationSubject;

	@Value("${incomplete.course.alert.message.key}")
	private String incompleteCourseAlertMessageKey;

	@Value("${latest.course.alert.message.key}")
	private String latestCourseAlertMessageKey;

	@Value("${incomplete.courses.alert.enabled}")
	private boolean incompleteCoursesAlertEnabled;

	@Value("${incomplete.courses.alert.last.access.time.mills}")
	private long incompleteCoursesLastAccessTime;

	@Value("${latest.courses.alert.enabled}")
	private boolean latestCoursesAlertEnabled;

	@Value("${latest.courses.alert.user.email.list}")
	private String latestCoursesAlertUserEmailList;

	@Value("${latest.courses.alert.email.subject}")
	private String latestCoursesAlertEmailSubject;

	@Value("${latest.courses.alert.scheduler.time.gap}")
	private long latestCoursesAlertSchedulerTimeGap;

	@Value("${latest.courses.alert.content.limit}")
	private int latestCoursesAlertContentLimit;

	@Value("${latest.courses.alert.search.user.fields}")
	private String latestCoursesAlertSearchUserFields;

	@Value("${latest.courses.alert.search.content.fields}")
	private String latestCoursesAlertSearchContentFields;

	@Value("${latest.courses.alert.send.to.all.user}")
	private boolean latestCoursesAlertSendToAllUser;

	@Value("${latest.courses.alert.content.min.limit}")
	private int latestCoursesAlertContentMinLimit;

	@Value("${sender.mail}")
	private String senderEmailAddress;

	@Value("${notification.service.host}")
	private String notificationServiceHost;

	@Value("${notification.event.endpoint}")
	private String notificationEventEndpoint;

	@Value("${course.url}")
	private String courseLinkUrl;

	@Value("${es.user.report.include.fields}")
	private String esUserReportIncludeFields;

	@Value("${kafka.topics.user.assessment.submit}")
	private String assessmentSubmitTopic;

	@Value("${metaData.contextTypes}")
	private String[] ContextTypes;

	@Value("${kafka.topics.user.bulk.upload}")
	private String userBulkUploadTopic;

	@Value("${org.channel.delimitter}")
	private String orgChannelDelimitter;

	@Value("${org.search.response.default.limit}")
	private Integer orgSearchResponseDefaultLimit;

	@Value("${lms.system.settings.verified.profile.fields.path}")
	private String verifiedProfileFieldsPath;

	@Value("${user.bulk.upload.group.value}")
	private String bulkUploadGroupValue;

	@Value("${content.search.primary.category.filter}")
	private String contentSearchPrimaryCategoryFilter;

	@Value("${assessment.hierarchy.namespace}")
	private String assessmentHierarchyNameSpace;

	@Value("${assessment.hierarchy.table}")
	private String assessmentHierarchyTable;

	@Value("${assessment.user.submit.data.table}")
	private String assessmentUserSubmitDataTable;

	@Value("${kafka.topics.user.assessment.async.submit.handler}")
	private String assessmentAsyncSubmitHandlerTopic;

	@Value("${assessment.ratake.count.verification.enabled}")
	private boolean assessmentRetakeCountVerificationEnabled;

	@Value("${bulk.upload.tag.verification.regex}")
	private String bulkUploadTagVerificationRegex;

	@Value("${report.download.container.name}")
	private String reportDownloadContainerName;

	@Value("${report.download.folder.name}")
	private String reportDownloadFolderName;

	@Value("${progress.api.update.endpoint}")
	private String progressUpdateEndPoint;

	@Value("${kafka.topic.update.content.progress}")
	private String updateContentProgressKafkaTopic;

	@Value("${sb.es.user.profile.index.type}")
	private String sbEsProfileIndexType;

	@Value("${content.type.auto.enroll.accepted}")
	private String contentTypeAutoEnrollAccepted;

	@Value("${content-read-endpoint-fields}")
	private String contentReadEndPointFields;

	@Value("${content-read-endpoint}")
	private String contentReadEndPoint;


	@Value("${redis.questions.read.timeout}")
	private Integer redisQuestionsReadTimeOut;

	@Value("${read.assess.questions.from.redis}")
	private boolean qListFromCacheEnabled;

	@Value("${mdo.admin.update.users}")
	private String mdoAdminUpdateUsers;

	@Value("${learning_service_base_url}")
	private String learningServiceBaseUrl;

	@Value("${system.content.update.url}")
	private String systemUpdateAPI;

	@Value(("${limit_for_search}"))
	private String limitForSearch;

	@Value("${user.registration.welcome.email.signinLink}")
	private String welcomeEmailSigninLink;

	@Value("${user.registration.welcome.email.discoverLink}")
	private String welcomeEmailDiscoverLink;

	@Value("${user.registration.welcome.email.meetingLink}")
	private String welcomeEmailMeetingLink;

	@Value("${report.type.get.file.info}")
	private String reportTypeGetFileInfo;

	@Value("${redis.insights.index}")
	private int redisInsightIndex;

	@Value("${insights.label.certificates.across}")
	private String insightsLabelCertificatesAcross;

	@Value("${insights.label.learning.hours.across}")
	private String insightsLabelLearningHoursAcross;

	@Value("${insights.label.certificates.your.department}")
	private String insightsLabelCertificatesYourDepartment;

	@Value("${insights.label.learning.hours.your.department}")
	private String insightsLabelLearningHoursYourDepartment;

	@Value("${sb.otp.generate.path}")
	private String sbOTPGeneratePath;

	@Value("${lms.user.update.path}")
	private String lmsUserUpdatePath;

	@Value("${cloud.profile.image.container.name}")
	private String cloudProfileImageContainerName;

	@Value("${sb.service.user.lookup.path}")
	private String lmsUserLookupPath;

	@Value("${wf.service.update.pendingRequestsToNewMDO}")
	private String pendingRequestsToNewMDOPath;

	@Value("${batch.enrolment.return.size}")
	private Integer batchEnrolmentReturnSize;

	@Value("${default.content.properties}")
	private String defaultContentProperties;

	@Value("${cbplan.manual.end.date}")
	private String cbPlanEndDate;

	@Value("${karma.points.limit}")
	private int KarmaPointsLimit;
	@Value("${kafka.topics.claim.acbp.karma.points}")
	private String claimKarmaPointsTopic;

	@Value("#{${report.property.map}}")
	private Map<String, String> reportMap;

	@Value("${spv_admin_channel_name}")
	private String spvChannelName;

	@Value("#{${spv.report.property.map}}")
	private Map<String, String> spvReportMap;

	@Value("#{${spv.report.property.subFolder.map}}")
	private Map<String, String> spvReportSubFolderTypeMap;

	@Value("${user.registration.welcome.email.profileUpdateLink}")
	private String welcomeEmailProfileUpdateLink;
	@Value("${ehrms.detail.url}")
	private String ehrmsDetailUrl;

	@Value("${ehrms.auth.url}")
	private String ehrmsAuthUrl;

	@Value("${ehrms.auth.username}")
	private String ehrmsAuthUserName;

	@Value("${ehrms.auth.password}")
	private String ehrmsAuthPassword;

	@Value("${redis.ehrms.token.timeout}")
	private Integer redisEhrmsTokenTimeOut;

	@Value("${redis.wheebox.key}")
	private String redisWheeboxKey;

	@Value("${sector.category.fields}")
	private String sectorFields;

	@Value("${sub.sector.category.fields}")
	private String subSectorFields;

	public String getRedisWheeboxKey() {
		return redisWheeboxKey;
	}

	public void setRedisWheeboxKey(String redisWheeboxKey) {
		this.redisWheeboxKey = redisWheeboxKey;
	}

	@Value("${notify.service.host}")
	private String notifyServiceHost;

	@Value("${recommend.content.notification.template}")
	private String recommendContentTemplate;

	@Value("${notification.support.mail}")
	private String supportEmail;

	@Value("${notify.service.path.async}")
	private String notifyServicePathAsync;

	@Value("${sb.role.read}")
	private String sbRoleRead;

	@Value("${cert-registry-service-host}")
	private String certRegistryServiceBaseUrl;

	@Value("${cert-registry-certificate-download-url}")
	private String certRegistryCertificateDownloadUrl;

	@Value("${digilocker-issuer-id}")
	private String digiLockerIssuerId;

	@Value("${report.property.file.allMdo}")
	private String reportPropertyFileAllMdo;

	@Value("${operational.reports.passwordlength}")
	private int zipFilePasswordLength;

	@Value("${operational.reports.unzip.password}")
	private String unZipFilePassword;

	@Value("${es.default.result.limit}")
	private int esDefaultResultLimit;

	@Value("${secret.key.token.validation}")
	private String secretKeyTokenValidation;

	@Value("${operational.report.folder.name}")
	private String operationalReportFolderName;

	@Value("${operational.report.file.name}")
	private String operationReportFileName;

	@Value("${digilocker-api-key}")
	private String digiLockerAPIKey;

	@Value("${pdf-generator-service-host}")
	private String pdfGeneratorServiceBaseUrl;

	@Value("${pdf-generator-svg-to-pdf-url}")
	private String pdfGeneratorSvgToPdfUrl;

	@Value("${km.framework.term.search.path}")
	private String kmFrameworkTermSearchPath;

	@Value("${sector.category.name}")
	private String sectorCategoryName;

	@Value("${km.framework.term.read.path}")
	private String kmFrameworkTermReadPath;

	@Value("${km.framework.term.create.path}")
	private String kmFrameworkTermCreatePath;

	@Value("${sector.framework.name}")
	private String sectorFrameworkName;

	@Value("${domain.url}")
	private String domainUrl;

	@Value("${kafka.topics.calendar.bulk.upload.event}")
	private String calendarEventBulkUploadTopic;

	@Value("${event-create-api}")
	private String eventCreateAPI;

	@Value("${event-publish-api}")
	private String eventPublishAPI;

	@Value("${calendar.bulk.upload.container.name}")
	private String calendarEventBulkUploadContainerName;

	@Value("${event-update-api}")
	private String eventUpdateAPI;

	@Value("#{${payload.to.redis.key.mapping}}")
	private String payloadToRedisKeyMapping;

	@Value("${cloud.org.store.folder.name}")
	private String orgStoreFolderName;

	@Value("${cloud.public.store.container.name}")
	private String cloudPublicContainerName;

	@Value("#{${organisation.insights.fields}}")
	private Map<String, String> organisationInsightFields;

	@Value("#{${organisation.insights.property.fields}}")
	private Map<String, String> organisationInsightPropertyFields;

	@Value("#{${organisation.insights.redis.key.mapping}}")
	private Map<String, String> organisationInsightRedisKeyMapping;

	@Value("${bulk.upload.allowed.roles.creation}")
	private String bulkUploadAllowedRolesCreation;

	public boolean qListFromCacheEnabled() {
		return qListFromCacheEnabled;
	}

	public void setqListFromCacheEnabled(boolean qListFromCacheEnabled) {
		this.qListFromCacheEnabled = qListFromCacheEnabled;
	}

	public Integer getRedisQuestionsReadTimeOut() {
		return redisQuestionsReadTimeOut;
	}

	public void setRedisQuestionsReadTimeOut(Integer redisQuestionsReadTimeOut) {
		this.redisQuestionsReadTimeOut = redisQuestionsReadTimeOut;
	}

	public Integer getBatchEnrolmentReturnSize() {
		return batchEnrolmentReturnSize;
	}

	public void setBatchEnrolmentReturnSize(Integer batchEnrolmentReturnSize) {
		this.batchEnrolmentReturnSize = batchEnrolmentReturnSize;
	}

	public String getSbOTPGeneratePath() {
		return sbOTPGeneratePath;
	}

	public void setSbOTPGeneratePath(String sbOTPGeneratePath) {
		this.sbOTPGeneratePath = sbOTPGeneratePath;
	}

	public String getInsightsLabelCertificatesAcross() {
		return insightsLabelCertificatesAcross;
	}

	public void setInsightsLabelCertificatesAcross(String insightsLabelCertificatesAcross) {
		this.insightsLabelCertificatesAcross = insightsLabelCertificatesAcross;
	}

	public String getInsightsLabelLearningHoursAcross() {
		return insightsLabelLearningHoursAcross;
	}

	public void setInsightsLabelLearningHoursAcross(String insightsLabelLearningHoursAcross) {
		this.insightsLabelLearningHoursAcross = insightsLabelLearningHoursAcross;
	}

	public String getInsightsLabelCertificatesYourDepartment() {
		return insightsLabelCertificatesYourDepartment;
	}

	public void setInsightsLabelCertificatesYourDepartment(String insightsLabelCertificatesYourDepartment) {
		this.insightsLabelCertificatesYourDepartment = insightsLabelCertificatesYourDepartment;
	}

	public String getInsightsLabelLearningHoursYourDepartment() {
		return insightsLabelLearningHoursYourDepartment;
	}

	public void setInsightsLabelLearningHoursYourDepartment(String insightsLabelLearningHoursYourDepartment) {
		this.insightsLabelLearningHoursYourDepartment = insightsLabelLearningHoursYourDepartment;
	}

	public int getRedisInsightIndex() {
		return redisInsightIndex;
	}

	public void setRedisInsightIndex(int redisInsightIndex) {
		this.redisInsightIndex = redisInsightIndex;
	}

	public String getAssessmentSubmitTopic() {
		return assessmentSubmitTopic;
	}

	public void setAssessmentSubmitTopic(String assessmentSubmitTopic) {
		this.assessmentSubmitTopic = assessmentSubmitTopic;
	}

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

	public String getLmsUserUpdatePrivatePath() {
		return lmsUserUpdatePrivatePath;
	}

	public void setLmsUserUpdatePrivatePath(String lmsUserUpdatePrivatePath) {
		this.lmsUserUpdatePrivatePath = lmsUserUpdatePrivatePath;
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

	public String[] getEsHostList() {
		return esHostList.split(",", -1);
	}

	public void setEsHostList(String esHostList) {
		this.esHostList = esHostList;
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

	public String getCloudContainerName() {
		return cloudContainerName;
	}

	public void setCloudContainerName(String cloudContainerName) {
		this.cloudContainerName = cloudContainerName;
	}

	public String getCloudStorageTypeName() {
		return cloudStorageTypeName;
	}

	public void setCloudStorageTypeName(String cloudStorageTypeName) {
		this.cloudStorageTypeName = cloudStorageTypeName;
	}

	public String getCloudStorageKey() {
		return cloudStorageKey;
	}

	public void setCloudStorageKey(String cloudStorageKey) {
		this.cloudStorageKey = cloudStorageKey;
	}

	public String getCloudStorageSecret() {
		return cloudStorageSecret;
	}

	public void setCloudStorageSecret(String cloudStorageSecret) {
		this.cloudStorageSecret = cloudStorageSecret;
	}

	public String getCloudStorageCephs3Endpoint() {
		return cloudStorageCephs3Endpoint;
	}

	public void setCloudStorageCephs3Endpoint(String cloudStorageCephs3Endpoint) {
		this.cloudStorageCephs3Endpoint = cloudStorageCephs3Endpoint;
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

	public String[] getSbEsHostList() {
		return sbEsHostList.split(",", -1);
	}

	public void setSbEsHostList(String sbEsHost) {
		this.sbEsHostList = sbEsHostList;
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

	public String getLmsUserSignUpPath() {
		return lmsUserSignUpPath;
	}

	public void setLmsUserSignUpPath(String lmsUserSignUpPath) {
		this.lmsUserSignUpPath = lmsUserSignUpPath;
	}

	public List<String> getBulkUploadStatusFields() {
		return Arrays.asList(bulkUploadStatusFields.split(",", -1));
	}

	public void setBulkUploadStatusFields(String bulkUploadStatusFields) {
		this.bulkUploadStatusFields = bulkUploadStatusFields;
	}

	public String getBulkUploadContainerName() {
		return bulkUploadContainerName;
	}

	public void setBulkUploadContainerName(String bulkUploadContainerName) {
		this.bulkUploadContainerName = bulkUploadContainerName;
	}

	public String getBulkUploadEmailTemplate() {
		return bulkUploadEmailTemplate;
	}

	public void setBulkUploadEmailTemplate(String bulkUploadEmailTemplate) {
		this.bulkUploadEmailTemplate = bulkUploadEmailTemplate;
	}

	public List<String> getBulkUploadEmailNotificationList() {
		return Arrays.asList(bulkUploadEmailNotificationList.split(",", -1));
	}

	public void setBulkUploadEmailNotificationList(String bulkUploadEmailNotificationList) {
		this.bulkUploadEmailNotificationList = bulkUploadEmailNotificationList;
	}

	public String getBulkUploadEmailNotificationSubject() {
		return bulkUploadEmailNotificationSubject;
	}

	public void setBulkUploadEmailNotificationSubject(String bulkUploadEmailNotificationSubject) {
		this.bulkUploadEmailNotificationSubject = bulkUploadEmailNotificationSubject;
	}

	public String getIncompleteCourseAlertMessageKey() {
		return incompleteCourseAlertMessageKey;
	}

	public void setIncompleteCourseAlertMessageKey(String incompleteCourseAlertMessageKey) {
		this.incompleteCourseAlertMessageKey = incompleteCourseAlertMessageKey;
	}

	public String getLatestCourseAlertMessageKey() {
		return latestCourseAlertMessageKey;
	}

	public void setLatestCourseAlertMessageKey(String latestCourseAlertMessageKey) {
		this.latestCourseAlertMessageKey = latestCourseAlertMessageKey;
	}

	public boolean isIncompleteCoursesAlertEnabled() {
		return incompleteCoursesAlertEnabled;
	}

	public void setIncompleteCoursesAlertEnabled(boolean incompleteCoursesAlertEnabled) {
		this.incompleteCoursesAlertEnabled = incompleteCoursesAlertEnabled;
	}

	public long getIncompleteCoursesLastAccessTime() {
		return incompleteCoursesLastAccessTime;
	}

	public void setIncompleteCoursesLastAccessTime(long incompleteCoursesLastAccessTime) {
		this.incompleteCoursesLastAccessTime = incompleteCoursesLastAccessTime;
	}

	public boolean isLatestCoursesAlertEnabled() {
		return latestCoursesAlertEnabled;
	}

	public void setLatestCoursesAlertEnabled(boolean latestCoursesAlertEnabled) {
		this.latestCoursesAlertEnabled = latestCoursesAlertEnabled;
	}

	public List<String> getLatestCoursesAlertUserEmailList() {
		if (StringUtils.isNotBlank(latestCoursesAlertUserEmailList)) {
			return Arrays.asList(latestCoursesAlertUserEmailList.split(",", -1));
		} else {
			return Collections.emptyList();
		}
	}

	public void setLatestCoursesAlertUserEmailList(String latestCoursesAlertUserEmailList) {
		this.latestCoursesAlertUserEmailList = latestCoursesAlertUserEmailList;
	}

	public String getLatestCoursesAlertEmailSubject() {
		return latestCoursesAlertEmailSubject;
	}

	public void setLatestCoursesAlertEmailSubject(String latestCoursesAlertEmailSubject) {
		this.latestCoursesAlertEmailSubject = latestCoursesAlertEmailSubject;
	}

	public long getLatestCoursesAlertSchedulerTimeGap() {
		return latestCoursesAlertSchedulerTimeGap;
	}

	public void setLatestCoursesAlertSchedulerTimeGap(long latestCoursesAlertSchedulerTimeGap) {
		this.latestCoursesAlertSchedulerTimeGap = latestCoursesAlertSchedulerTimeGap;
	}

	public int getLatestCoursesAlertContentLimit() {
		return latestCoursesAlertContentLimit;
	}

	public void setLatestCoursesAlertContentLimit(int latestCoursesAlertContentLimit) {
		this.latestCoursesAlertContentLimit = latestCoursesAlertContentLimit;
	}

	public List<String> getLatestCoursesAlertSearchUserFields() {
		return Arrays.asList(latestCoursesAlertSearchUserFields.split(",", -1));
	}

	public void setLatestCoursesAlertSearchUserFields(String latestCoursesAlertSearchUserFields) {
		this.latestCoursesAlertSearchUserFields = latestCoursesAlertSearchUserFields;
	}

	public List<String> getLatestCoursesAlertSearchContentFields() {
		return Arrays.asList(latestCoursesAlertSearchContentFields.split(",", -1));
	}

	public void setLatestCoursesAlertSearchContentFields(String latestCoursesAlertSearchContentFields) {
		this.latestCoursesAlertSearchContentFields = latestCoursesAlertSearchContentFields;
	}

	public boolean isLatestCoursesAlertSendToAllUser() {
		return latestCoursesAlertSendToAllUser;
	}

	public void setLatestCoursesAlertSendToAllUser(boolean latestCoursesAlertSendToAllUser) {
		this.latestCoursesAlertSendToAllUser = latestCoursesAlertSendToAllUser;
	}

	public int getLatestCoursesAlertContentMinLimit() {
		return latestCoursesAlertContentMinLimit;
	}

	public void setLatestCoursesAlertContentMinLimit(int latestCoursesAlertContentMinLimit) {
		this.latestCoursesAlertContentMinLimit = latestCoursesAlertContentMinLimit;
	}

	public String getSenderEmailAddress() {
		return senderEmailAddress;
	}

	public void setSenderEmailAddress(String senderEmailAddress) {
		this.senderEmailAddress = senderEmailAddress;
	}

	public String getNotificationServiceHost() {
		return notificationServiceHost;
	}

	public void setNotificationServiceHost(String notificationServiceHost) {
		this.notificationServiceHost = notificationServiceHost;
	}

	public String getNotificationEventEndpoint() {
		return notificationEventEndpoint;
	}

	public void setNotificationEventEndpoint(String notificationEventEndpoint) {
		this.notificationEventEndpoint = notificationEventEndpoint;
	}

	public String getCourseLinkUrl() {
		return courseLinkUrl;
	}

	public void setCourseLinkUrl(String courseLinkUrl) {
		this.courseLinkUrl = courseLinkUrl;
	}

	public String[] getEsUserReportIncludeFields() {
		return esUserReportIncludeFields.split(",", -1);
	}

	public void setEsUserReportIncludeFields(String esUserReportIncludeFields) {
		this.esUserReportIncludeFields = esUserReportIncludeFields;
	}

	public String[] getContextTypes() {
		return ContextTypes;
	}

	public void setContextTypes(String[] contextTypes) {
		ContextTypes = contextTypes;
	}

	public String getUserBulkUploadTopic() {
		return userBulkUploadTopic;
	}

	public void setUserBulkUploadTopic(String userBulkUploadTopic) {
		this.userBulkUploadTopic = userBulkUploadTopic;
	}

	public String getOrgChannelDelimitter() {
		return orgChannelDelimitter;
	}

	public void setOrgChannelDelimitter(String orgChannelDelimitter) {
		this.orgChannelDelimitter = orgChannelDelimitter;
	}

	public Integer getOrgSearchResponseDefaultLimit() {
		return orgSearchResponseDefaultLimit;
	}

	public void setOrgSearchResponseDefaultLimit(Integer orgSearchResponseDefaultLimit) {
		this.orgSearchResponseDefaultLimit = orgSearchResponseDefaultLimit;
	}

	public String getVerifiedProfileFieldsPath() {
		return verifiedProfileFieldsPath;
	}

	public void setVerifiedProfileFieldsPath(String verifiedProfileFieldsPath) {
		this.verifiedProfileFieldsPath = verifiedProfileFieldsPath;
	}

	public List<String> getBulkUploadGroupValue() {
		return Arrays.asList(bulkUploadGroupValue.split(",", -1));
	}

	public void setBulkUploadGroupValue(String bulkUploadGroupValue) {
		this.bulkUploadGroupValue = bulkUploadGroupValue;
	}

	public List<String> getContentSearchPrimaryCategoryFilter() {
		return Arrays.asList(contentSearchPrimaryCategoryFilter.split(",", -1));
	}

	public void setContentSearchPrimaryCategoryFilter(String contentSearchPrimaryCategoryFilter) {
		this.contentSearchPrimaryCategoryFilter = contentSearchPrimaryCategoryFilter;
	}

	public String getAssessmentHierarchyNameSpace() {
		return assessmentHierarchyNameSpace;
	}

	public void setAssessmentHierarchyNameSpace(String assessmentHierarchyNameSpace) {
		this.assessmentHierarchyNameSpace = assessmentHierarchyNameSpace;
	}

	public String getAssessmentHierarchyTable() {
		return assessmentHierarchyTable;
	}

	public void setAssessmentHierarchyTable(String assessmentHierarchyTable) {
		this.assessmentHierarchyTable = assessmentHierarchyTable;
	}

	public String getAssessmentUserSubmitDataTable() {
		return assessmentUserSubmitDataTable;
	}

	public void setAssessmentUserSubmitDataTable(String assessmentUserSubmitDataTable) {
		this.assessmentUserSubmitDataTable = assessmentUserSubmitDataTable;
	}

	public String getAssessmentAsyncSubmitHandlerTopic() {
		return assessmentAsyncSubmitHandlerTopic;
	}

	public void setAssessmentAsyncSubmitHandlerTopic(String assessmentAsyncSubmitHandlerTopic) {
		this.assessmentAsyncSubmitHandlerTopic = assessmentAsyncSubmitHandlerTopic;
	}

	public boolean isAssessmentRetakeCountVerificationEnabled() {
		return assessmentRetakeCountVerificationEnabled;
	}

	public void setAssessmentRetakeCountVerificationEnabled(boolean assessmentRetakeCountVerificationEnabled) {
		this.assessmentRetakeCountVerificationEnabled = assessmentRetakeCountVerificationEnabled;
	}

	public String getBulkUploadTagVerificationRegex() {
		return bulkUploadTagVerificationRegex;
	}

	public void setBulkUploadTagVerificationRegex(String bulkUploadTagVerificationRegex) {
		this.bulkUploadTagVerificationRegex = bulkUploadTagVerificationRegex;
	}

	public String getReportDownloadContainerName() {
		return reportDownloadContainerName;
	}

	public void setReportDownloadContainerName(String reportDownloadContainerName) {
		this.reportDownloadContainerName = reportDownloadContainerName;
	}

	public String getReportDownloadFolderName() {
		return reportDownloadFolderName;
	}

	public void setReportDownloadFolderName(String reportDownloadFolderName) {
		this.reportDownloadFolderName = reportDownloadFolderName;
	}

	public String getProgressUpdateEndPoint() {
		return progressUpdateEndPoint;
	}

	public void setProgressUpdateEndPoint(String progressUpdateEndPoint) {
		this.progressUpdateEndPoint = progressUpdateEndPoint;
	}

	public String getUpdateContentProgressKafkaTopic() {
		return updateContentProgressKafkaTopic;
	}

	public void setUpdateContentProgressKafkaTopic(String updateContentProgressKafkaTopic) {
		this.updateContentProgressKafkaTopic = updateContentProgressKafkaTopic;
	}

	public String getSbEsProfileIndexType() {
		return sbEsProfileIndexType;
	}

	public void setSbEsProfileIndexType(String sbEsProfileIndexType) {
		this.sbEsProfileIndexType = sbEsProfileIndexType;
	}

	public List<String> getContentTypeAutoEnrollAccepted() {
		return Arrays.asList(contentTypeAutoEnrollAccepted.split(",", -1));
	}

	public void setContentTypeAutoEnrollAccepted(String contentTypeAutoEnrollAccepted) {
		this.contentTypeAutoEnrollAccepted = contentTypeAutoEnrollAccepted;
	}

	public String getContentReadEndPointFields() {
		return contentReadEndPointFields;
	}

	public void setContentReadEndPointFields(String contentReadEndPointFields) {
		this.contentReadEndPointFields = contentReadEndPointFields;
	}

	public String getContentReadEndPoint() {
		return contentReadEndPoint;
	}

	public void setContentReadEndPoint(String contentReadEndPoint) {
		this.contentReadEndPoint = contentReadEndPoint;
	}

	public String getWelcomeEmailSigninLink() {
		return welcomeEmailSigninLink;
	}

	public void setWelcomeEmailSigninLink(String welcomeEmailSigninLink) {
		this.welcomeEmailSigninLink = welcomeEmailSigninLink;
	}

	public String getWelcomeEmailDiscoverLink() {
		return welcomeEmailDiscoverLink;
	}

	public void setWelcomeEmailDiscoverLink(String welcomeEmailDiscoverLink) {
		this.welcomeEmailDiscoverLink = welcomeEmailDiscoverLink;
	}

	public String getWelcomeEmailMeetingLink() {
		return welcomeEmailMeetingLink;
	}

	public void setWelcomeEmailMeetingLink(String welcomeEmailMeetingLink) {
		this.welcomeEmailMeetingLink = welcomeEmailMeetingLink;
	}

	public List<String> getReportTypeGetFileInfo() {
		return Arrays.asList(reportTypeGetFileInfo.split(",", -1));
	}

	public void setReportTypeGetFileInfo(String reportTypeGetFileInfo) {
		this.reportTypeGetFileInfo = reportTypeGetFileInfo;
	}

	public String getMdoAdminUpdateUsers() {
		return mdoAdminUpdateUsers;
	}

	public void setMdoAdminUpdateUsers(String mdoAdminUpdateUsers) {
		this.mdoAdminUpdateUsers = mdoAdminUpdateUsers;
	}

	public String getLearningServiceBaseUrl() {
		return learningServiceBaseUrl;
	}

	public void setLearningServiceBaseUrl(String learningServiceBaseUrl) {
		this.learningServiceBaseUrl = learningServiceBaseUrl;
	}

	public String getSystemUpdateAPI() {
		return systemUpdateAPI;
	}

	public void setSystemUpdateAPI(String systemUpdateAPI) {
		this.systemUpdateAPI = systemUpdateAPI;
	}

	public String getLimitForSearch() {
		return limitForSearch;
	}

	public void setLimitForSearch(String limitForSearch) {
		this.limitForSearch = limitForSearch;
	}

	public String getLmsUserUpdatePath() {
		return lmsUserUpdatePath;
	}

	public void setLmsUserUpdatePath(String lmsUserUpdatePath) {
		this.lmsUserUpdatePath = lmsUserUpdatePath;
	}

	public String getCloudProfileImageContainerName() {
		return cloudProfileImageContainerName;
	}

	public void setCloudProfileImageContainerName(String cloudProfileImageContainerName) {
		this.cloudProfileImageContainerName = cloudProfileImageContainerName;
	}

	public List<String> getDefaultContentProperties() {
		return Arrays.asList(defaultContentProperties.split(",", -1));
	}

	public void setDefaultContentProperties(String defaultContentProperties) {
		this.defaultContentProperties = defaultContentProperties;
	}

	public String getLmsUserLookupPath() {
		return lmsUserLookupPath;
	}

	public void setLmsUserLookupPath(String lmsUserLookupPath) {
		this.lmsUserLookupPath = lmsUserLookupPath;
	}

  public String getCbPlanEndDate() {
		return cbPlanEndDate;
	}

	public void setCbPlanEndDate(String cbPlanEndDate) {
		this.cbPlanEndDate = cbPlanEndDate;
  	}

	public String getPendingRequestsToNewMDOPath() {
		return pendingRequestsToNewMDOPath;
	}

	public void setPendingRequestsToNewMDOPath(String pendingRequestsToNewMDOPath) {
		this.pendingRequestsToNewMDOPath = pendingRequestsToNewMDOPath;
	}

	public List<String> getCbPlanUpdatePublishAuthorizedRoles() {
		return Arrays.asList(cbPlanUpdatePublishAuthorizedRoles.split(",", -1));
	}

	public void setCbPlanUpdatePublishAuthorizedRoles(String cbPlanUpdatePublishAuthorizedRoles) {
		this.cbPlanUpdatePublishAuthorizedRoles = cbPlanUpdatePublishAuthorizedRoles;
	}

	public int getKarmaPointsLimit() {
		return KarmaPointsLimit;
	}

	public void setKarmaPointsLimit(int karmaPointsLimit) {
		KarmaPointsLimit = karmaPointsLimit;
	}

	public String getClaimKarmaPointsTopic() {
		return claimKarmaPointsTopic;
	}

	public void setClaimKarmaPointsTopic(String claimKarmaPointsTopic) {
		this.claimKarmaPointsTopic = claimKarmaPointsTopic;
	}

	public Map<String, String> getReportMap() {
		return reportMap;
	}

	public void setReportMap(Map<String, String> reportMap) {
		this.reportMap = reportMap;
	}

	public String getSpvChannelName() {
		return spvChannelName;
	}

	public void setSpvChannelName(String spvChannelName) {
		this.spvChannelName = spvChannelName;
	}

	public Map<String, String> getSpvReportMap() {
		return spvReportMap;
	}

	public void setSpvReportMap(Map<String, String> spvReportMap) {
		this.spvReportMap = spvReportMap;
	}

	public Map<String, String> getSpvReportSubFolderTypeMap() {
		return spvReportSubFolderTypeMap;
	}

	public void setSpvReportSubFolderTypeMap(Map<String, String> spvReportSubFolderTypeMap) {
		this.spvReportSubFolderTypeMap = spvReportSubFolderTypeMap;
	}

	public String getCbplanContentRequestKafkaTopic() {
		return cbplanContentRequestKafkaTopic;
	}

	public void setCbplanContentRequestKafkaTopic(String cbplanContentRequestKafkaTopic) {
		this.cbplanContentRequestKafkaTopic = cbplanContentRequestKafkaTopic;
	}

	public String getWelcomeEmailProfileUpdateLink() {
		return welcomeEmailProfileUpdateLink;
	}

	public void setWelcomeEmailProfileUpdateLink(String welcomeEmailProfileUpdateLink) {
		this.welcomeEmailProfileUpdateLink = welcomeEmailProfileUpdateLink;
	}

	public String getNotifyServiceHost() {
		return notifyServiceHost;
	}

	public void setNotifyServiceHost(String notifyServiceHost) {
		this.notifyServiceHost = notifyServiceHost;
	}

	public String getRecommendContentTemplate() {
		return recommendContentTemplate;
	}

	public void setRecommendContentTemplate(String recommendContentTemplate) {
		this.recommendContentTemplate = recommendContentTemplate;
	}

	public String getSupportEmail() {
		return supportEmail;
	}

	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}

	public String getNotifyServicePathAsync() {
		return notifyServicePathAsync;
	}

	public void setNotifyServicePathAsync(String notifyServicePathAsync) {
		this.notifyServicePathAsync = notifyServicePathAsync;
	}

	public String getEhrmsDetailUrl() {
		return ehrmsDetailUrl;
	}

	public void setEhrmsDetailUrl(SBApiResponse response) {
		this.ehrmsDetailUrl = ehrmsDetailUrl;
	}

	public void setEhrmsDetailUrl(String ehrmsDetailUrl) {
		this.ehrmsDetailUrl = ehrmsDetailUrl;
	}

	public String getEhrmsAuthUrl() {
		return ehrmsAuthUrl;
	}

	public void setEhrmsAuthUrl(String ehrmsAuthUrl) {
		this.ehrmsAuthUrl = ehrmsAuthUrl;
	}

	public String getEhrmsAuthUserName() {
		return ehrmsAuthUserName;
	}

	public void setEhrmsAuthUserName(String ehrmsAuthUserName) {
		this.ehrmsAuthUserName = ehrmsAuthUserName;
	}

	public String getEhrmsAuthPassword() {
		return ehrmsAuthPassword;
	}

	public void setEhrmsAuthPassword(String ehrmsAuthPassword) {
		this.ehrmsAuthPassword = ehrmsAuthPassword;
	}

	public Integer getRedisEhrmsTokenTimeOut() {
		return redisEhrmsTokenTimeOut;
	}

	public void setRedisEhrmsTokenTimeOut(Integer redisEhrmsTokenTimeOut) {
		this.redisEhrmsTokenTimeOut = redisEhrmsTokenTimeOut;
	}

	public String getCertRegistryServiceBaseUrl() {
		return certRegistryServiceBaseUrl;
	}

	public void setCertRegistryServiceBaseUrl(String certRegistryServiceBaseUrl) {
		this.certRegistryServiceBaseUrl = certRegistryServiceBaseUrl;
	}

	public String getCertRegistryCertificateDownloadUrl() {
		return certRegistryCertificateDownloadUrl;
	}

	public void setCertRegistryCertificateDownloadUrl(String certRegistryCertificateDownloadUrl) {
		this.certRegistryCertificateDownloadUrl = certRegistryCertificateDownloadUrl;
	}

	public String getDigiLockerIssuerId() {
		return digiLockerIssuerId;
	}

	public void setDigiLockerIssuerId(String digiLockerIssuerId) {
		this.digiLockerIssuerId = digiLockerIssuerId;
	}

	public List<String> getReportPropertyFileAllMdo() {
		return Arrays.asList(reportPropertyFileAllMdo.split(",", -1));
	}

	public void setReportPropertyFileAllMdo(String reportPropertyFileAllMdo) {
		this.reportPropertyFileAllMdo = reportPropertyFileAllMdo;
	}

	public int getZipFilePasswordLength() {
		return zipFilePasswordLength;
	}

	public void setZipFilePasswordLength(int zipFilePasswordLength) {
		this.zipFilePasswordLength = zipFilePasswordLength;
	}

	public String getUnZipFilePassword() {
		return unZipFilePassword;
	}

	public void setUnZipFilePassword(String unZipFilePassword) {
		this.unZipFilePassword = unZipFilePassword;
	}
	public int getEsDefaultResultLimit() {
		return esDefaultResultLimit;
	}

	public void setEsDefaultResultLimit(int esDefaultResultLimit) {
		this.esDefaultResultLimit = esDefaultResultLimit;
	}

	public String getSbRoleRead() {
		return sbRoleRead;
	}

	public void setSbRoleRead(String sbRoleRead) {
		this.sbRoleRead = sbRoleRead;
	}

	public String getSecretKeyTokenValidation() {
		return secretKeyTokenValidation;
	}

	public void setSecretKeyTokenValidation(String secretKeyTokenValidation) {
		this.secretKeyTokenValidation = secretKeyTokenValidation;
	}

	public String getOperationalReportFolderName() {
		return operationalReportFolderName;
	}

	public void setOperationalReportFolderName(String operationalReportFolderName) {
		this.operationalReportFolderName = operationalReportFolderName;
	}

	public String getOperationReportFileName() {
		return operationReportFileName;
	}

	public void setOperationReportFileName(String operationReportFileName) {
		this.operationReportFileName = operationReportFileName;
	}

	public String getDigiLockerAPIKey() {
		return digiLockerAPIKey;
	}

	public void setDigiLockerAPIKey(String digiLockerAPIKey) {
		this.digiLockerAPIKey = digiLockerAPIKey;
	}

	public String getPdfGeneratorServiceBaseUrl() {
		return pdfGeneratorServiceBaseUrl;
	}

	public void setPdfGeneratorServiceBaseUrl(String pdfGeneratorServiceBaseUrl) {
		this.pdfGeneratorServiceBaseUrl = pdfGeneratorServiceBaseUrl;
	}

	public String getPdfGeneratorSvgToPdfUrl() {
		return pdfGeneratorSvgToPdfUrl;
	}

	public void setPdfGeneratorSvgToPdfUrl(String pdfGeneratorSvgToPdfUrl) {
		this.pdfGeneratorSvgToPdfUrl = pdfGeneratorSvgToPdfUrl;
	}

	public String getKmFrameworkTermSearchPath() {
		return kmFrameworkTermSearchPath;
	}

	public void setKmFrameworkTermSearchPath(String kmFrameworkTermSearchPath) {
		this.kmFrameworkTermSearchPath = kmFrameworkTermSearchPath;
	}

	public String getSectorCategoryName() {
		return sectorCategoryName;
	}

	public void setSectorCategoryName(String sectorCategoryName) {
		this.sectorCategoryName = sectorCategoryName;
	}

	public List<String> getSectorFields() {
		return Arrays.asList(sectorFields.split(",", -1));
	}

	public void setSectorFields(String sectorFields) {
		this.sectorFields = sectorFields;
	}

	public List<String> getSubSectorFields() {
		return Arrays.asList(subSectorFields.split(",", -1));
	}

	public void setSubSectorFields(String subSectorFields) {
		this.subSectorFields = subSectorFields;
	}

	public String getKmFrameworkTermReadPath() {
		return kmFrameworkTermReadPath;
	}

	public void setKmFrameworkTermReadPath(String kmFrameworkTermReadPath) {
		this.kmFrameworkTermReadPath = kmFrameworkTermReadPath;
	}

	public String getKmFrameworkTermCreatePath() {
		return kmFrameworkTermCreatePath;
	}

	public void setKmFrameworkTermCreatePath(String kmFrameworkTermCreatePath) {
		this.kmFrameworkTermCreatePath = kmFrameworkTermCreatePath;
	}

	public String getSectorFrameworkName() {
		return sectorFrameworkName;
	}

	public void setSectorFrameworkName(String sectorFrameworkName) {
		this.sectorFrameworkName = sectorFrameworkName;
	}

	public void setDomainUrl(String domainUrl) {this.domainUrl = domainUrl;}

	public String getDomainUrl() {return domainUrl;}


	public String getCalendarEventBulkUploadTopic() {
		return calendarEventBulkUploadTopic;
	}

	public void setCalendarEventBulkUploadTopic(String calendarEventBulkUploadTopic) {
		this.calendarEventBulkUploadTopic = calendarEventBulkUploadTopic;
	}

	public String getEventCreateAPI() {
		return eventCreateAPI;
	}

	public void setEventCreateAPI(String eventCreateAPI) {
		this.eventCreateAPI = eventCreateAPI;
	}

	public String getEventPublishAPI() {
		return eventPublishAPI;
	}

	public void setEventPublishAPI(String eventPublishAPI) {
		this.eventPublishAPI = eventPublishAPI;
	}

	public String getCalendarEventBulkUploadContainerName() {
		return calendarEventBulkUploadContainerName;
	}

	public void setCalendarEventBulkUploadContainerName(String calendarEventBulkUploadContainerName) {
		this.calendarEventBulkUploadContainerName = calendarEventBulkUploadContainerName;
	}

	public String getEventUpdateAPI() {
		return eventUpdateAPI;
	}

	public void setEventUpdateAPI(String eventUpdateAPI) {
		this.eventUpdateAPI = eventUpdateAPI;
	}

	public String getOrgStoreFolderName() {
		return orgStoreFolderName;
	}

	public void setOrgStoreFolderName(String orgStoreFolderName) {
		this.orgStoreFolderName = orgStoreFolderName;
	}

	public String getPayloadToRedisKeyMapping() {
		return payloadToRedisKeyMapping;
	}

	public void setPayloadToRedisKeyMapping(String payloadToRedisKeyMapping) {
		this.payloadToRedisKeyMapping = payloadToRedisKeyMapping;
	}

	public String getCloudPublicContainerName() {
		return cloudPublicContainerName;
	}

	public void setCloudPublicContainerName(String cloudPublicContainerName) {
		this.cloudPublicContainerName = cloudPublicContainerName;
	}

	public Map<String, String> getOrganisationInsightFields() {
		return organisationInsightFields;
	}

	public void setOrganisationInsightFields(Map<String, String> organisationInsightFields) {
		this.organisationInsightFields = organisationInsightFields;
	}

	public Map<String, String> getOrganisationInsightPropertyFields() {
		return organisationInsightPropertyFields;
	}

	public void setOrganisationInsightPropertyFields(Map<String, String> organisationInsightPropertyFields) {
		this.organisationInsightPropertyFields = organisationInsightPropertyFields;
	}

	public Map<String, String> getOrganisationInsightRedisKeyMapping() {
		return organisationInsightRedisKeyMapping;
	}

	public void setOrganisationInsightRedisKeyMapping(Map<String, String> organisationInsightRedisKeyMapping) {
		this.organisationInsightRedisKeyMapping = organisationInsightRedisKeyMapping;
	}

	public List<String> getBulkUploadAllowedRolesCreation() {
		return Arrays.asList(bulkUploadAllowedRolesCreation.split(",", -1));
	}

	public void setBulkUploadAllowedRolesCreation(String bulkUploadAllowedRolesCreation) {
		this.bulkUploadAllowedRolesCreation = bulkUploadAllowedRolesCreation;
	}
}