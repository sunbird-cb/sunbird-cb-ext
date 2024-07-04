package org.sunbird.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {
	public static final String UUID = "wid";
	public static final String KID = "kid";
	public static final String SOURCE_ID = "source_id";
	public static final String FIRST_NAME = "first_name";
	public static final String MIDDLE_NAME = "middle_name";
	public static final String FIRSTNAME = "firstName";
	public static final String MOBILE = "mobile";
	public static final String PHONE = "phone";
	public static final String CONTACT_PHONE_NUMBER_OFFICE = "contact_phone_number_office";
	public static final String CONTACT_PHONE_NUMBER_HOME = "contact_phone_number_home";
	public static final String CONTACT_PHONE_NUMBER_PERSONAL = "contact_phone_number_personal";
	public static final String KNOWN_AS = "known_as";
	public static final String GENDER = "gender";
	public static final String DOB = "dob";
	public static final String SALUTATION = "salutation";
	public static final String EMAIL = "email";
	public static final String PROFILE_DETAILS_KEY = "profiledetails";
	public static final String SOURCE_PROFILE_PICTURE = "source_profile_picture";
	public static final String ROOT_ORG = "root_org";
	public static final String ORG = "org";
	public static final String EMPLOYEMENT_STATUS = "employement_status";
	public static final String CONTRACT_TYPE = "contract_type";
	public static final String JOB_TITLE = "job_title";
	public static final String JOB_ROLE = "job_role";
	public static final String DEPARTMENT_NAME = "department_name";
	public static final String UNIT_NAME = "unit_name";
	public static final String LANGUAGES_KNOWN = "languages_known";
	public static final String LANGUAGES = "languages";
	public static final String GRADUATIONS = "graduations";
	public static final String POST_GRADUATIONS = "postGraduations";
	public static final String INDUSTRIES = "industries";
	public static final String CADRE = "cadre";
	public static final String MINISTRY = "ministry";
	public static final String MINISTRIES = "ministries";
	public static final String SERVICE = "service";
	public static final String NATIONALITIES = "nationalities";
	public static final String COUNTRIES = "countries";
	public static final String NATIONALITY = "nationality";
	public static final String ORGANIZATION_LOCATION_COUNTRY = "organization_location_country";
	public static final String ORGANIZATION_LOCATION_STATE = "organization_location_state";
	public static final String ORGANIZATION_LOCATION_CITY = "organization_location_city";
	public static final String RESIDENCE_COUNTRY = "residence_country";
	public static final String RESIDENCE_STATE = "residence_state";
	public static final String RESIDENCE_CITY = "residence_city";
	public static final String TIME_INSERTED = "time_inserted";
	public static final String TIME_UPDATED = "time_updated";
	public static final String JSON_UNMAPPED_FIELDS = "json_unmapped_fields";
	public static final String IS_ACTIVE = "is_active";
	public static final String ACCOUNT_EXPIRY_DATE = "account_expiry_date";
	public static final String SOURCE_DATA = "source_data";
	public static final String USER_ID = "userId";
	public static final String FILTERS = "filters";
	public static final String CONTENT_ID = "content_id";
	public static final String BATCH_ID = "batchId";
	public static final String COURSE_ID = "courseId";
	public static final String ENROLMENT_TYPE = "enrollmenttype";
	public static final String IDENTIFIER = "identifier";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String ACTIVE = "active";
	public static final String COMPLETION_PERCENTAGE = "completionPercentage";
	public static final String PROGRESS = "progress";
	public static final String PROFILEDETAILS = "profiledetails";
	public static final String EMPLOYMENTDETAILS = "employmentDetails";
	public static final String DEPARTMENTNAME = "departmentName";
	public static final String ISSUED_CERTIFICATES = "issued_certificates";
	public static final String IS_MDO = "isMdo";
	public static final String IS_CBP = "isCbp";
	public static final String PROFILE_DETAILS_DESIGNATION = "profileDetails.professionalDetails.designation";
	public static final String PROFILE_DETAILS_DESIGNATION_OTHER = "profileDetails.professionalDetails.designationOther";
	public static final String PROFILE_DETAILS_PRIMARY_EMAIL = "profileDetails.personalDetails.primaryEmail";
	public static final String DEPARTMENT = "department";
	public static final String DESIGNATION = "designation";
	public static final String DESIGNATIONS = "designations";
	public static final String GRADE_PAY = "gradePay";
	public static final String PRIMARY_EMAIL = "primaryEmail";
	public static final String FIRST_LOGIN_TIME = "firstLoginTime";
	public static final String RESPONSE_CODE = "responseCode";
	public static final String AUTH_TOKEN = "Authorization";
	public static final String X_AUTH_TOKEN = "x-authenticated-user-token";
	public static final String SUCCESSFUL = "Successful";
	public static final String FAILED = "Failed";
	public static final String MESSAGE = "message";
	public static final String DATA = "data";
	public static final String STATUS = "status";
	public static final String RESULT = "result";
	public static final String OK = "OK";
	public static final String ADD = "add";
	public static final String UPDATE = "update";
	public static final String CREATE = "create";
	public static final String ROOT_ORG_CONSTANT = "rootOrg";
	public static final String ORG_CONSTANT = "org";
	public static final String USER_ID_CONSTANT = "userId";
	public static final String FIELD_PASSED_CONSTANT = "fieldsPassed";
	public static final String FETCH_ONE_LEVEL_CONSTANT = "fetchOneLevel";
	public static final String SKIP_ACCESS_CHECK_CONSTANT = "skipAccessCheck";
	public static final String FIELDS_CONSTANT = "fields";
	public static final boolean FIELDS_PASSED = true;
	public static final boolean FETCH_ON_LEVEL = false;
	public static final boolean SKIP_ACCESS_CHECK = true;
	public static final List<String> MINIMUL_FIELDS = Collections
			.unmodifiableList(Arrays.asList("identifier", "duration", "downloadUrl", "description", "mimeType",
					"artifactUrl", "name", STATUS, "resourceType", "categoryType", "category"));
	public static final String FETCH_RESULT_CONSTANT = ".fetchResult:";
	public static final String URI_CONSTANT = "URI: ";
	public static final String REQUEST_CONSTANT = "Request: ";
	public static final String RESPONSE_CONSTANT = "Response: ";
	public static final String SERVICE_ERROR_CONSTANT = "Http Client threw an Exception:";
	public static final String EXTERNAL_SERVICE_ERROR_CODE = "Exception while querying the external service:";
	public static final String CONTENT_ID_REPLACER = "{contentId}";
	// User assessment pass mark
	public static final Float ASSESSMENT_PASS_SCORE = 60.0f;
	public static final String DATE_FORMAT = "yyyy-mm-dd hh:mm:ss";
	public static final String RESPONSE = "response";
	public static final String STAFF = "staff";
	public static final String API_STAFF_POSITION_ADD = "api.staff.position.add";
	public static final String API_STAFF_POSITION_UPDATE = "api.staff.position.update";
	public static final String API_STAFF_POSITION_READ = "api.staff.position.read";
	public static final String API_STAFF_POSITION_DELETE = "api.staff.position.delete";
	public static final String API_STAFF_POSITION_HISTORY_READ = "api.staff.position.history.read";
	public static final String API_BUDGET_SCHEME_ADD = "api.budget.schme.add";
	public static final String API_BUDGET_SCHEME_DOC_ADD = "api.budget.schme.doc.add";
	public static final String API_BUDGET_SCHEME_UPDATE = "api.budget.schme.update";
	public static final String API_BUDGET_SCHEME_READ = "api.budget.scheme.read";
	public static final String API_BUDGET_SCHEME_DELETE = "api.budget.scheme.delete";
	public static final String API_BUDGET_SCHEME_HISTORY_READ = "api.budget.scheme.history.read";
	public static final String API_FILE_UPLOAD = "api.file.upload";
	public static final String API_FILE_DELETE = "api.file.delete";
	public static final String API_PROFILE_UPDATE = "api.profile.update";
	public static final String API_GET_EXPLORE_COURSE_DETAIL = "api.explore.course";
	public static final String API_REFRESH_EXPLORE_COURSE_DETAIL = "api.refresh.explore.course.list";
	public static final String API_GET_MASTER_DATA = "api.get.master.data";
	public static final String API_GET_USER_PROGRESS = "api.get.user.progress";

	public static final String ORG_PROFILE_UPDATE = "org.profile.update";
	public static final String ID = "id";
	public static final String ORG_ID = "orgId";
	public static final String POSITION = "position";
	public static final String TOTAL_POSITION_FILLED = "totalPositionsFilled";
	public static final String TOTAL_POSITION_VACANT = "totalPositionsVacant";
	public static final String NAME = "name";
	public static final String COUNTRY_CODE = "countryCode";
	public static final String URL = "url";
	public static final String DELETED = "deleted";
	public static final String SCHEME_ID = "schemeId";
	public static final String BUDGET = "budget";
	public static final String BUDGET_YEAR = "budgetYear";
	public static final String SCHEME_NAME = "schemeName";
	public static final String SALARY_BUDGET_ALLOCATED = "salaryBudgetAllocated";
	public static final String TRAINING_BUDGET_ALLOCATED = "trainingBudgetAllocated";
	public static final String TRAINING_BUDGET_UTILIZATION = "trainingBudgetUtilization";

	// ratings and review
	public static final String API_RATINGS_ADD = "api.ratings.add";
	public static final String API_RATINGS_READ = "api.ratings.read";
	public static final String API_RATINGS_UPDATE = "api.ratings.update";
	public static final String API_RATINGS_SUMMARY = "api.ratings.summary";
	public static final String API_RATINGS_LOOKUP = "api.ratings.lookup";
	public static final String DATABASE = "sunbird";
	public static final String LOGIN_TABLE = "user_first_login_details";
	public static final String ACTIVITY_ID = "activityId";
	public static final String ACTIVITY_TYPE = "activityType";
	public static final String RATINGS_USER_ID = "userId";
	public static final String USER_FIRST_NAME = "firstName";
	public static final String USERID = "id";
	public static final String COMMENT = "comment";
	public static final String COMMENT_BY = "commentBy";
	public static final String RECOMMENDED = "recommended";
	public static final String COMMENT_UPDATED_ON = "commentUpdatedOn";
	public static final String CREATED_ON = "createdOn";
	public static final String RATING = "rating";
	public static final String REVIEW = "review";
	public static final String UPDATED_ON = "updatedOn";
	public static final String LATEST50REVIEWS = "latest50reviews";
	public static final String SUMMARY_ACTIVITY_ID = "activityid";
	public static final String SUMMARY_ACTIVITY_TYPE = "activitytype";
	public static final String TOTALCOUNT1STARS = "totalcount1stars";
	public static final String TOTALCOUNT2STARS = "totalcount2stars";
	public static final String TOTALCOUNT3STARS = "totalcount3stars";
	public static final String TOTALCOUNT4STARS = "totalcount4stars";
	public static final String TOTALCOUNT5STARS = "totalcount5stars";
	public static final String TOTALNUMBEROFRATINGS = "total_number_of_ratings";
	public static final String SUMOFTOTALRATINGS = "sum_of_total_ratings";
	public static final String NO_RATING_EXCEPTION_MESSAGE = "No ratings found for : ";
	public static final String NO_REVIEW_EXCEPTION_MESSAGE = "No reviews found for : ";
	public static final String RATING_GENERIC_EXCEPTION_MESSAGE = "Exception occurred while adding the course review : ";
	public static final String KAFKA_RATING_EXCEPTION_MESSAGE = "Exception occurred while connecting to kafka topic : ";
	public static final String RATING_UPSERT_OPERATION = "upsert";
	public static final String RATING_LOOKUP_RATING_OPERATION = "lookup";
	public static final String RATING_GET_OPERATION = "getRating";
	public static final String RATING_SUMMARY_OPERATION = "getSummary";
	// assessment
	public static final String QUESTION_SET = "questionSet";
	public static final String ASSESSMENT_QNS_ANS_SET = "assessmentQnsAnsSet_";
	public static final String ASSESSMENT_QNS_SET = "assessmentQnsSet_";
	// Cassandra Constants
	public static final String INSERT_INTO = "INSERT INTO ";
	public static final String DOT = ".";
	public static final String OPEN_BRACE = "(";
	public static final String VALUES_WITH_BRACE = ") VALUES (";
	public static final String QUE_MARK = "?";
	public static final String COMMA = ",";
	public static final String CLOSING_BRACE = ");";
	public static final String SUCCESS = "success";
	public static final String UNKNOWN_IDENTIFIER = "Unknown identifier ";
	public static final String UNDEFINED_IDENTIFIER = "Undefined column name ";
	public static final String EXCEPTION_MSG_FETCH = "Exception occurred while fetching record from ";
	public static final String EXCEPTION_MSG_DELETE = "Exception occurred while deleting record from ";
	public static final String AUDIT_TYPE = "auditType";
	public static final String CREATED_DATE = "createdDate";
	public static final String CREATED_BY = "createdBy";
	public static final String UPDATED_DATE = "updatedDate";
	public static final String UPDATED_BY = "updatedBy";
	public static final String TRANSACTION_DETAILS = "transactionDetails";
	public static final String PROOF_DOCS = "proofDocs";
	public static final String WORK_ORDER_ID = "workOrderId";
	public static final String WORK_ALLOCATION_ID = "workAllocationId";
	public static final String BEGIN_BATCH = "BEGIN BATCH ";
	public static final String APPLY_BATCH = " APPLY BATCH;";
	// Database and Tables
	public static final String KEYSPACE_SUNBIRD = "sunbird";
	public static final String KEYSPACE_SUNBIRD_COURSES = "sunbird_courses";
	public static final String TABLE_ORG_BUDGET_SCHEME = "org_budget_scheme";
	public static final String TABLE_USER_ASSESSMENT_TIME = "user_assessment_time";
	public static final String SHA_256_WITH_RSA = "SHA256withRSA";
	public static final String SUB = "sub";
	public static final String _UNAUTHORIZED = "Unauthorized";
	public static final String DOT_SEPARATOR = ".";
	public static final String ACCESS_TOKEN_PUBLICKEY_BASEPATH = "accesstoken.publickey.basepath";
	public static final String TABLE_ORG_AUDIT = "org_audit";
	public static final String TABLE_ORG_STAFF_POSITION = "org_staff_position";
	public static final String TABLE_WORK_ORDER = "work_order";
	public static final String TABLE_WORK_ALLOCATION = "work_allocation";
	public static final String TABLE_USER_WORK_ALLOCATION_MAPPING = "user_work_allocation_mapping";
	public static final String TABLE_MANDATORY_USER_CONTENT = "mandatory_user_content";
	public static final String TABLE_ORGANIZATION = "organisation";
	public static final String TABLE_USER_ENROLMENT = "user_enrolments";
	public static final String TABLE_USER = "user";
	public static final String TABLE_USER_ROLES = "user_roles";
	public static final String TABLE_COURSE_BATCH = "course_batch";
	public static final String TABLE_RATINGS = "ratings";
	public static final String TABLE_RATINGS_LOOKUP = "ratings_lookup";
	public static final String TABLE_RATINGS_SUMMARY = "ratings_summary";
	public static final String TABLE_EXPLORE_COURSE_LIST = "explore_course_list";
	public static final String TABLE_EXPLORE_COURSE_LIST_V2 = "explore_course_list_v2";
	public static final String VALUE = "value";
	public static final String DEPT_NAME = "deptName";
	public static final String PROFILE_DETAILS = "profileDetails";
	public static final String PROFESSIONAL_DETAILS = "professionalDetails";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON = "application/json";
	public static final String OSID = "osid";
	public static final String INITIATE = "INITIATE";
	public static final String FIELD_KEY = "fieldKey";
	public static final String TO_VALUE = "toValue";
	public static final String FROM_VALUE = "fromValue";
	public static final String STATE = "state";
	public static final String ACTION = "action";
	public static final String APPLICATION_ID = "applicationId";
	public static final String ACTOR_USER_ID = "actorUserId";
	public static final String SERVICE_NAME = "serviceName";
	public static final String PROFILE = "profile";
	public static final String WFID = "wfId";
	public static final String UPDATE_FIELD_VALUES = "updateFieldValues";
	public static final String PROFILE_UPDATE_FIELDS = "profileUpdateFields_";
	public static final String IGOT = "igot";
	public static final String DOPT = "dopt";
	public static final String PERSONAL_DETAILS = "personalDetails";
	public static final String TRANSITION_DETAILS = "transitionDetails";
	public static final String UNAUTHORIZED = "unauthorized";
	// Redis
	public static final String API_REDIS_DELETE = "api.redis.delete";
	public static final String API_REDIS_GET_KEYS = "api.redis.get.keys";
	public static final String API_REDIS_GET_KEYS_VALUE_SET = "api.redis.get.keys&values";
	public static final String REDIS_COMMON_KEY = "CB_EXT_";
	public static final String COMPETENCY_CACHE_NAME = "competency";
	public static final String COMPETENCY_CACHE_NAME_BY_AREA = "competencyByArea";
	public static final String COMPETENCY_CACHE_NAME_BY_TYPE = "competencyByType";
	public static final String PROVIDER_CACHE_NAME = "provider";
	public static final String POSITIONS_CACHE_NAME = "positions";
	public static final String COMPETENCY_FACET_NAME = "competencies_v3.name";
	public static final String IS_TENANT = "isTenant";
	public static final String CHANNEL = "channel";
	public static final String USER_TOKEN = "x-authenticated-user-token";
	public static final String AUTHORIZATION = "authorization";
	public static final String FACETS = "facets";
	public static final String PRIMARY_CATEGORY = "primaryCategory";
	public static final String LIMIT = "limit";
	public static final String REQUEST = "request";
	public static final String FIELDS = "fields";
	public static final String OFFSET = "offset";
	public static final String BUDGET_DOC_UPLOADED_BY = "uploadedBy";
	public static final String BUDGET_DOC_UPLOADED_DATE = "uploadedDate";
	public static final String BUDGET_DOC_FILE_NAME = "fileName";
	public static final String BUDGET_DOC_FILE_TYPE = "fileType";
	public static final String BUDGET_DOC_FILE_SIZE = "fileSize";
	public static final String BUDGET_DOC_FILE_URL = "fileUrl";
	// telemetry audit constants
	public static final String ORG_LIST = "ORGANISATION_LIST";
	public static final String VERSION = "3.0.2";
	public static final String EID = "AUDIT";
	public static final String PDATA_ID = "dev.sunbird.cb.ext.service";
	public static final String PDATA_PID = "sunbird-cb-ext-service";
	public static final String CURRENT_STATE = "FirstLogin";
	public static final String LOGIN_TIME = "login_time";
	public static final List<String> PROPS = Collections.unmodifiableList(Arrays.asList("WAT"));
	public static final String USER_CONST = "User";
	public static final String ASSESSMENT_LEVEL_SCORE_CUTOFF = "assessmentlevel";
	public static final String SECTION_LEVEL_SCORE_CUTOFF = "sectionlevel";
	public static final String IDENTIFIER_REPLACER = "{identifier}";
	public static final String CHILDREN = "children";
	public static final String PARAMS = "params";
	public static final String MAX_QUESTIONS = "maxQuestions";
	public static final String CHILD_NODES = "childNodes";
	public static final String SEARCH = "search";
	public static final String SEARCHES = "searches";
	public static final String QUESTION_ID = "qs_id_";
	public static final String ASSESSMENT_ID = "assess_id_";
	public static final String EDITOR_STATE = "editorState";
	public static final String CHOICES = "choices";
	public static final String ANSWER = "answer";
	public static final String QUESTION = "question";
	public static final String OPTIONS = "options";
	public static final String USER_ASSESS_REQ = "user_assess_req_";
	public static final String HIERARCHY = "hierarchy";
	public static final String CONTENT_HIERARCHY = "content_hierarchy";
	public static final String BATCH_ID_COLUMN = "batchid";
	public static final String COURSE_ID_COLUMN = "courseid";
	public static final String COMPLETION_PERCENTAGE_COLUMN = "completionpercentage";
	public static final String LAST_ACCESS_TIME = "last_access_time";
	public static final String SUNBIRD_KEY_SPACE_NAME = "sunbird";
	public static final String SUNBIRD_COURSES_KEY_SPACE_NAME = "sunbird_courses";
	public static final String LAST_ACCESS_TIME_GAP = "last.access.time.gap.millis";
	public static final String USER_CONTENT_CONSUMPTION = "user_content_consumption";
	public static final String NOTIFICATION_UTIL = "notificationUtil";
	public static final String NOTIFICATIONS = "notifications";
	public static final String CORE_CONNECTIONS_PER_HOST_FOR_LOCAL = "coreConnectionsPerHostForLocal";
	public static final String CORE_CONNECTIONS_PER_HOST_FOR_REMOTE = "coreConnectionsPerHostForRemote";
	public static final String MAX_CONNECTIONS_PER_HOST_FOR_LOCAl = "maxConnectionsPerHostForLocal";
	public static final String MAX_CONNECTIONS_PER_HOST_FOR_REMOTE = "maxConnectionsPerHostForRemote";
	public static final String MAX_REQUEST_PER_CONNECTION = "maxRequestsPerConnection";
	public static final String HEARTBEAT_INTERVAL = "heartbeatIntervalSeconds";
	public static final String POOL_TIMEOUT = "poolTimeoutMillis";
	public static final String CONTACT_POINT = "contactPoint";
	public static final String PORT = "port";
	public static final String QUERY_LOGGER_THRESHOLD = "queryLoggerConstantThreshold";
	public static final String SUNBIRD_CASSANDRA_CONSISTENCY_LEVEL = "sunbird_cassandra_consistency_level";
	public static final String INCOMPLETE_COURSES_MAIL_SUBJECT = "Complete the courses you started";
	public static final String COURSE = "Course";
	public static final String COURSE_KEYWORD = "course";
	public static final String _URL = "_url";
	public static final String THUMBNAIL = "_thumbnail";
	public static final String _NAME = "_name";
	public static final String _DURATION = "_duration";
	public static final String SEND_NOTIFICATION_PROPERTIES = "send-notification";
	public static final String NOTIFICATION_HOST = "notification.service.host";
	public static final String NOTIFICATION_ENDPOINT = "notification.event.endpoint";
	public static final String SUBJECT = "subject";
	public static final String SCHEDULER_TIME_GAP = "scheduler-time-gap";
	public static final String SCHEDULER_RUN_DAY = "scheduler-run-day";
	public static final String SCHEDULER_RUN_TIME = "scheduler-run-time";
	public static final String IS_DELETED = "isDeleted";
	public static final String EXCLUDE_USER_EMAILS = "exclude_user_emails";
	public static final String INCOMPLETE_COURSES = "incompletecourses";
	public static final String POSTER_IMAGE = "posterImage";

	public static final String COURSE_URL = "course.url";
	public static final String SENDER_MAIL = "sender.mail";
	public static final String CASSANDRA_CONFIG_HOST = "cassandra.config.host";
	public static final String OBJECT_TYPE = "objectType";
	public static final String QUESTIONS = "questions";
	public static final String RHS_CHOICES = "rhsChoices";
	public static final String MTF_QUESTION = "MTF Question";
	public static final String FTB_QUESTION = "FTB Question";
	public static final String API_QUESTIONSET_HIERARCHY_GET = "api.questionset.hierarchy.get";
	public static final String VER = "3.0";
	public static final String API_QUESTIONS_LIST = "api.questions.list";
	public static final String MINIMUM_PASS_PERCENTAGE = "minimumPassPercentage";
	public static final String SCORE_CUTOFF_TYPE = "scoreCutoffType";
	public static final String PASS_PERCENTAGE = "passPercentage";
	public static final String TOTAL = "total";
	public static final String BLANK = "blank";
	public static final String CORRECT = "correct";
	public static final String INCORRECT = "incorrect";
	public static final String PASS = "pass";
	public static final String OVERALL_RESULT = "overallResult";
	public static final String DO = "do_";
	public static final String SSO_CLIENT_ID = "sso.client.id";
	public static final String SSO_CLIENT_SECRET = "sso.client.secret";
	public static final String SSO_PASSWORD = "sso.password";
	public static final String SSO_POOL_SIZE = "sso.connection.pool.size";
	public static final String SSO_PUBLIC_KEY = "sunbird_sso_publickey";
	public static final String SSO_REALM = "sso.realm";
	public static final String SSO_URL = "sso.url";
	public static final String SSO_USERNAME = "sso.username";
	public static final String SUNBIRD_SSO_CLIENT_ID = "sunbird_sso_client_id";
	public static final String SUNBIRD_SSO_CLIENT_SECRET = "sunbird_sso_client_secret";
	public static final String SUNBIRD_SSO_PASSWORD = "sunbird_sso_password";
	public static final String SUNBIRD_SSO_RELAM = "sunbird_sso_realm";
	public static final String SUNBIRD_SSO_URL = "sunbird_sso_url";
	public static final String SUNBIRD_SSO_USERNAME = "sunbird_sso_username";
	public static final String DURATION = "duration";
	public static final String USER_REGISTRATION_REGISTER_API = "api.user.registration.register";
	public static final String USER_GENERATE_OTP = "api.otp.generate";
	public static final String USER_REGISTRATION_UPDATE_API = "api.user.registration.update";
	public static final String USER_REGISTRATION_RETRIEVE_API = "api.user.registration.retrieve";
	public static final String ORG_ONBOARDING_PROFILE_RETRIEVE_API = "api.org.onboarding.retrieve";
	public static final String USER_REGISTRATION_DEPT_INFO_API = "api.user.registration.dept.info";
	public static final String API_ORG_LIST = "api.org.list";
	public static final String API_ORG_EXT_CREATE = "api.org.extended.create";
	public static final String API_ORG_EXT_SEARCH = "api.org.extended.search";
	public static final String COUNT = "count";

	// email params
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String TEMPLATE = "template";
	public static final String USER_REGISTERATION_TEMPLATE = "user-registration";
	public static final String STATUS_PARAM = "{status}";
	public static final String REG_CODE_PARAM = "{regCode}";
	public static final String BUTTON_URL = "btn-url";
	public static final String BUTTON_NAME = "btn-name";

	// notification reuest params
	public static final String DELIVERY_TYPE = "deliveryType";
	public static final String CONFIG = "config";
	public static final String IDS = "ids";

	// wf status
	public static final String INITIATED = "initiated";
	public static final String APPROVED = "approved";
	public static final String DENIED = "denied";

	public static final String EMAIL_EXIST_ERROR = "Email id already registered with another User profile";
	public static final String EMAIL_VERIFIED = "emailVerified";
	public static final String USER_NAME = "userName";
	public static final String USER_FULL_NAME = "fullName";

	public static final String MANDATORY_FIELDS_EXISTS = "mandatoryFieldsExists";
	public static final String KEY = "key";
	public static final String TYPE = "type";
	public static final String LINK = "link";
	public static final String ALLOWED_LOGGING = "allowedLoging";

	public static final String API_VERSION_1 = "1.0";
	public static final String BODY = "body";
	public static final String EMAIL_TEMPLATE_TYPE = "emailTemplateType";
	public static final String MODE = "mode";
	public static final String ORG_NAME = "orgName";
	public static final String RECIPIENT_EMAILS = "recipientEmails";
	public static final String SET_PASSWORD_LINK = "setPasswordLink";
	public static final String WELCOME_MESSAGE = "welcomeMessage";
	public static final String HELLO = "Hello";
	public static final String WELCOME_EMAIL_TEMPLATE_TYPE = "iGotWelcome";
	public static final String WELCOME_EMAIL_MESSAGE = "Welcome Email";
	public static final String ORGANIZATION_ID = "organisationId";
	public static final String ROLES = "roles";
	public static final String PUBLIC = "PUBLIC";
	public static final String ASC_ORDER = "asc";
	public static final String SORT_BY = "sort_by";
	public static final String FIELD = "field";
	public static final String KEYWORD = "keyword";
	public static final String VERIFIED = "VERIFIED";
	public static final String COMPETENCY_AREA = "competencyArea";
	public static final String COMPETENCY_TYPE = "competencyType";
	public static final String SOURCE = "source";
	public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
	public static final String CONTENT = "content";
	public static final String COMPETENCY = "competency";
	public static final String CHILD_COUNT = "childCount";
	public static final String RESPONSE_DATA = "responseData";
	public static final String DEPARTMENT_LIST_CACHE_NAME = "deptCacheList";
	public static final String POSITIONS = "positions";
	public static final String BEARER = "bearer ";
	public static final String IMG_URL = "imgurl";
	public static final String VALUES = "values";
	public static final String PROGRAM = "Program";
	public static final String LIVE = "Live";
	public static final String GOVERNMENT = "Government";
	public static final String ORGANIZATION_TYPE = "organisationType";
	public static final String ORGANIZATION_SUB_TYPE = "organisationSubType";
	public static final String FIRST_NAME_LOWER_CASE = "firstname";
	public static final String SPV = "SPV";
	public static final String TABLE_ORG_HIERARCHY = "org_hierarchy";
	public static final String PARENT_MAP_ID = "parentMapId";
	public static final String SB_ORG_TYPE = "sbOrgType";
	public static final String SB_ORG_ID = "sbOrgId";
	public static final String MAP_ID = "mapId";
	public static final String SB_ROOT_ORG_ID = "sbRootOrgId";
	public static final String ERROR_MESSAGE = "errmsg";

	public static final String API_USER_BASIC_INFO = "api.user.basic.info";
	public static final String API_USER_BASIC_PROFILE_UPDATE = "api.user.basic.profile.update";
	public static final String TABLE_SYSTEM_SETTINGS = "system_settings";
	public static final String CUSTODIAN_ORG_ID = "custodianOrgId";
	public static final String CUSTODIAN_ORG_CHANNEL = "custodianOrgChannel";
	public static final String ROOT_ORG_ID = "rootOrgId";
	public static final String IS_UPDATE_REQUIRED = "isUpdateRequired";
	public static final String USER_ROLES = "userRoles";
	public static final String X_AUTH_USER_ID = "x-authenticated-userid";
	public static final String SOFT_DELETE_OLD_ORG = "softDeleteOldOrg";
	public static final String NOTIFY_MIGRATION = "notifyMigration";
	public static final String FORCE_MIGRATION = "forceMigration";
	public static final String PROFILE_DETAILS_LOWER = "profiledetails";
	public static final String EMPLOYMENT_DETAILS = "employmentDetails";
	public static final String DEPARTMENT_ID = "departmentId";
	public static final String ROOT_ORG_ID_LOWER = "rootorgid";
	public static final String OPERATION_TYPE = "operationType";
	public static final String SYNC = "sync";
	public static final String OBJECT_IDS = "objectIds";
	public static final String USER = "user";
	public static final String PUBLIC_COURSE_LIST = "exploreOpenCourseList";
	public static final String LAST_UPDATED_ON = "lastUpdatedOn";
	public static final String DESCENDING_ORDER = "desc";
	public static final String X_AUTH_USER_ORG_ID = "x-authenticated-user-orgid";
	public static final String X_AUTH_USER_ORG_NAME = "x-authenticated-user-orgname";
	public static final String X_AUTH_USER_CHANNEL = "x-authenticated-user-channel";
	public static final String API_USER_SIGNUP = "api.user.signup";
	public static final String API_POSITION_CREATE = "api.create.position";
	public static final String API_POSITION_UPDATE = "api.update.position";
	public static final String API_USER_BULK_UPLOAD = "api.user.bulk.upload";
	public static final String API_USER_BULK_UPLOAD_STATUS = "api.user.bulk.upload.status";

	public static final String API_USER_ENROLMENT_REPORT = "api.user.enrolment.report";
	public static final String API_USER_REPORT = "api.user.report";
	public static final String TABLE_USER_BULK_UPLOAD = "user_bulk_upload";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_PATH = "filePath";
	public static final String DATE_CREATED_ON = "dateCreatedOn";
	public static final String DATE_UPDATE_ON = "dateUpdatedOn";
	public static final String INITIATED_CAPITAL = "INITIATED";
	public static final List<String> COURSE_REMINDER_EMAIL_FIELDS = Arrays.asList(RATINGS_USER_ID, BATCH_ID_COLUMN,
			COURSE_ID_COLUMN, COMPLETION_PERCENTAGE_COLUMN, LAST_ACCESS_TIME);
	public static final String BATCHES = "batches";
	public static final String NO_OF_COURSES = "noOfCourses";
	public static final String _DESCRIPTION = "_description";
	public static final String EMAIL_TYPE = "emailtype";
	public static final String NEW_COURSES_EMAIL = "New Courses Email";
	public static final String EMAIL_RECORD_TABLE = "email_record";
	public static final String LAST_SENT_DATE = "lastsentdate";
	public static final String MIN = "min";
	public static final String MAX = "max";
	public static final String CONTENT_TYPE_KEY = "contentType";
	public static final String PARENT_CONTENT_TYPE = "parentContentType";
	public static final String NEW_COURSES = "newcourses";
	public static final String OVERVIEW_BATCH_KEY = "/overview?batchId=";
	public static final String LEAF_NODES_COUNT = "leafNodesCount";
	public static final String CLIENT_ERROR = "CLIENT_ERROR";
	public static final String PARENT = "parent";
	public static final String ORGANISATIONS = "organisations";

	public static final String CIPHER_ALGORITHM = "AES";
	public static final byte[] CIPHER_KEY = new byte[] { 'T', 'h', 'i', 's', 'A', 's', 'I', 'S', 'e', 'r', 'c', 'e',
			'K', 't', 'e', 'y' };
	public static final List<String> DECRYPTED_FIELDS = Arrays.asList("phone", "email");
	public static final String CREATED_FOR = "createdFor";
	public static final String COURSE_ORG_ID = "courseOrgId";
	public static final String COURSE_ORG_NAME = "courseOrgName";
	public static final String STATUS_ENROLLED = "Enrolled";
	public static final String STATUS_IN_PROGRESS = "In-Progress";
	public static final String STATUS_COMPLETED = "Completed";
	public static final String CONTENT_STATUS = "contentStatus";
	public static final String ROLE = "role";
	public static final String SCOPE = "scope";
	public static final String SB_SUB_ORG_TYPE = "sbSubOrgType";
	public static final String ORG_CODE = "orgCode";
	public static final String MDO = "mdo";
	public static final String BOARD = "board";
	public static final String TRAINING_INSTITUTE = "TrainingInstitute";
	public static final String TOTAL_SCORE = "totalScore";
	public static final String SUBMIT_ASSESSMENT_RESPONSE = "submitassessmentresponse";
	public static final String PRACTICE_QUESTION_SET = "Practice Question Set";
	public static final String EXPECTED_DURATION = "expectedDuration";
	public static final String SUBMITTED = "SUBMITTED";
	public static final String NOT_SUBMITTED = "NOT SUBMITTED";
	public static final String END_TIME = "endtime";
	public static final String ASSESSMENT_ID_KEY = "assessmentId";
	public static final String START_TIME = "starttime";
	public static final String CONTENT_ID_KEY = "contentId";
	public static final String QUESTION_TYPE = "qType";
	public static final String SELECTED_ANSWER = "selectedAnswer";
	public static final String INDEX = "index";
	public static final String MCQ_SCA = "mcq-sca";
	public static final String MCQ_MCA = "mcq-mca";
	public static final String FTB = "ftb";
	public static final String MTF = "mtf";
	public static final String IS_CORRECT = "isCorrect";
	public static final String OPTION_ID = "optionId";

	public static final String TABLE_USER_ASSESSMENT_DATA = "user_assessment_data";
	public static final String TABLE_MASTER_DATA = "master_data";


	public static final String USER_ID_DOESNT_EXIST = "User Id doesn't exist! Please supply a valid auth token";
	public static final String ASSESSMENT_DATA_START_TIME_NOT_UPDATED = "Assessment Data & Start Time not updated in the DB.";
	public static final String FAILED_TO_GET_QUESTION_DETAILS = "Failed to get Question List data from the Question List Api.";
	public static final String ASSESSMENT_RETRY_ATTEMPTS_CROSSED = "Maximum retry attempts for assessment reached.";

	public static final String ASSESSMENT_HIERARCHY_READ_FAILED = "Assessment hierarchy read failed, failed to process request";
	public static final String ASSESSMENT_ID_KEY_IS_NOT_PRESENT_IS_EMPTY = "Assessment Id Key is not present/is empty";

	public static final String USER_ASSESSMENT_DATA_NOT_PRESENT = "User Assessment Data not present in Databases";
	public static final String ASSESSMENT_ID_INVALID = "The Assessment Id is Invalid/Doesn't match with our records";
	public static final String IDENTIFIER_LIST_IS_EMPTY = "Identifier List is Empty";
	public static final String THE_QUESTIONS_IDS_PROVIDED_DONT_MATCH = "The Questions Ids Provided don't match the active user assessment session";
	public static final String ASSESSMENT_ID_INVALID_SESSION_EXPIRED = "Assessment Id Invalid/Session Expired/Redis Cache doesn't have this question list details";
	public static final String INVALID_ASSESSMENT_ID = "Invalid Assessment Id";
	public static final String READ_ASSESSMENT_FAILED = "Failed to read assessment hierarchy for the given AssessmentId.";
	public static final String READ_ASSESSMENT_START_TIME_FAILED = "Failed to read the assessment start time.";
	public static final String WRONG_SECTION_DETAILS = "Wrong section details.";
	public static final String ASSESSMENT_SUBMIT_EXPIRED = "The Assessment submission time-period is over! Assessment can't be submitted";
	public static final String ASSESSMENT_ALREADY_SUBMITTED = "This Assessment is already Submitted!";

	public static final String ASSESSMENT_SUBMIT_INVALID_QUESTION = "The QuestionId provided don't match to the Assessment Read";
	public static final String ASSESSMENT_SUBMIT_QUESTION_READ_FAILED = "Failed to read Question Set from DB";


	public static final String ASSESSMENT_READ_RESPONSE = "assessmentreadresponse";
	public static final String API_SUBMIT_ASSESSMENT = "api.submit.asssessment";
	public static final String MAX_ASSESSMENT_RETAKE_ATTEMPTS = "maxAssessmentRetakeAttempts";
	public static final String TOTAL_RETAKE_ATTEMPTS_ALLOWED = "attemptsAllowed";
	public static final String RETAKE_ATTEMPTS_CONSUMED = "attemptsMade";
	public static final String API_RETAKE_ASSESSMENT_GET = "api.assessmment.attempt";

	public static final List<String> USER_ENROLMENT_REPORT_FIELDS = Arrays.asList(USER_ID, FIRSTNAME, EMAIL,
			PHONE, ROOT_ORG_ID, CHANNEL);

	public static final List<String> COURSE_ENROLMENT_REPORT_FIELDS = Arrays.asList(COURSE_ID, NAME, COURSE_ORG_ID,
			COURSE_ORG_NAME);

	public static final List<String> USER_ENROLMENT_COMMON_FIELDS = Arrays.asList(STATUS, COMPLETION_PERCENTAGE);

	public static final String CONTEXT_TYPE = "contextType";
	public static final String CONTEXT_NAME = "contextName";
	public static final String CONTEXT_DATA = "contextData";
	public static final String CONTEXT_TYPE_ID = "contextTypeId";
	public static final String ADDITIONAL_PARAMS = "additionalParams";
	public static final String CONTEXT_STATUS = "contextStatus";
	public static final String TABLE_OFFENSIVE_DATA_FLAGS = "offensive_data_flags";
	public static final String ADD_OFFENSIVE_DATA_FLAG = "api.add.offensive.data.flag";
	public static final String UPDATE_OFFENSIVE_DATA_FLAG = "api.update.offensive.data.flag";
	public static final String GET_OFFENSIVE_DATA_FLAG = "api.get.offensive.data.flag";
	public static final String API_HEALTH_CHECK = "api.health.check";
	public static final String DRAFT = "DRAFT";
	public static final Object CREATED = "Created";
	public static final Object UPDATED = "Updated";
	public static final String HEALTHY = "healthy";
	public static final String CHECKS = "checks";
	public static final String CASSANDRA_DB = "cassandra db";
	public static final String REDIS_CACHE = "redis cache";
	public static final String PHONE_NUMBER_EXIST_ERROR = "Phone number is already registered with another User profile.";
	public static final String MOBILE_NUMBER_EXIST_ERROR = "Mobile number is already registered.";
	public static final String PHONE_VERIFIED = "phoneVerified";
	public static final String QUERY = "query";
	public static final String SORT_BY_KEYWORD = "sortBy";
	public static final String SUCCESSFUL_UPPERCASE = "SUCCESSFUL";
	public static final String FAILED_UPPERCASE = "FAILED";
	public static final String STATUS_IN_PROGRESS_UPPERCASE = "IN-PROGRESS";
	public static final String LOCAL_BASE_PATH= "/tmp/";
	public static final String USER_CREATION_FAILED = "User Creation Failed";
	public static final String TOTAL_RECORDS = "totalRecords";
	public static final String SUCCESSFUL_RECORDS_COUNT = "successfulRecordsCount";
	public static final String FAILED_RECORDS_COUNT = "failedRecordsCount";
	public static final String API_FILE_DOWNLOAD = "api.file.download";
	public static final String API_ORG_HIERACHY_SEARCH = "api.org.hierarchy.search";
	public static final String INVALID_REQUEST = "Invalid Request";
	public static final String L1_MAP_ID = "l1MapId";
	public static final String L2_MAP_ID = "l2MapId";
	public static final String L1_ORG_NAME = "l1OrgName";
	public static final String L2_ORG_NAME = "l2OrgName";
	public static final String SUCCESS_UPPERCASE = "SUCCESS";
	public static final String EMPTY_FILE_FAILED = "The uploaded file is empty";
	public static final String TAG = "tag";
	public static final String PARENT_TYPE = "parentType";
	public static final String VERIFIED_KARMAYOGI = "verifiedKarmayogi";
	public static final String EXTERNAL_SYSTEM_ID = "externalSystemId";
	public static final String EXTERNAL_SYSTEM = "externalSystem";
	public static final String GROUP = "group";
	public static final String BULK_USER_CREATE_API_FAILED = "Bulk User Create API Failed";
	public static final String BULK_USER_UPDATE_API_FAILED = "Bulk User Update API Failed";
	public static final String SECURE_SETTINGS = "secureSettings";
	public static final String API_USER_ENROLMENT = "user.enrolment";
	public static final String SUBMIT_ASSESSMENT_RESPONSE_KEY = "submitAssessmentResponse";
	public static final String API_READ_ASSESSMENT = "api.assessment.read";
	public static final String API_READ_ASSESSMENT_RESULT = "api.assessment.read.result";
	public static final String STATUS_IS_IN_PROGRESS = "isInProgress";
	public static final String ASSESSMENT_SUBMIT_IN_PROGRESS = "SUBMIT_IN_PROGRESS";
	public static final String ASSESSMENT_READ_RESPONSE_KEY = "assessmentReadResponse";
	public static final String ASSESSMENT_SAVE_READ_RESPONSE_KEY = "savepointsubmitreq";
	public static final String START_TIME_KEY = "startTime";
	public static final String VERIFIED_PROFILE_FIELDS_KEY = "verifiedProfileFields";
	public static final String PROFILE_APPROVAL_FIELDS_KEY = "profileApprovalFields";
	public static final String BULK_UPLOAD_VERIFICATION_REGEX = "bulk.upload.tag.verification.regex";
	public static final String USER_REGISTRATION_GROUP_LIST = "api.user.registration.group.list";

	public static final String TABLE_ENROLLMENT_BATCH_LOOKUP = "enrollment_batch_lookup";
	public static final String UPDATE_CONTENT_PROGRESS_SUCCESS_MSG = "Content progress is updated.";
	public static final String UPDATE_CONTENT_PROGRESS_ERROR_MSG = "Error in updating the content progress.";
	public static final String API_UPDATE_CONTENT_PROGRESS = "api.content.progress.ext.update";
	public static final String TABLE_ENROLMENT_BATCH_LOOKUP = "enrollment_batch_lookup";
	public static final String EMPLOYMENT_DETAILS_DEPARTMENT_NAME = "employmentDetails.departmentName";
	public static final String PROFILE_DETAILS_PHONE = "profileDetails.personalDetails.mobile";
	public static final String USER_SEARCH_CONTENT_RESULT_LIST = "api.user.offline.session.progress";
	public static final String CONTENT_PROGRESS_BATCH_ID_ERROR_MSG = "batchId is mandatory";
	public static final String CONTENT_PROGRESS_COURSE_ID_ERROR_MSG = "courseId is mandatory";
	public static final String CONTENT_PROGRESS_CONTENT_ID_ERROR_MSG = "contentId is mandatory, atleast one is required";

	public static final String END_TIME_KEY = "endTime";

	public static final String SESSION_ID="sessionId";

	public static final String QR_CODE_URL="qrcodeurl";

	public static final String VM="vm";

	public static final String HEADER="header";

	public static final String FOOTER="footer";

	public static final String PDF="pdf";

	public static final String PROGRAM_NAME="programName";

	public static final String EMPTY="";

	public static final String BATCH_SESSION_HEADER="pdf-batch-session-header";

	public static final String BATCH_SESSION_FOOTER="pdf-batch-session-footer";

	public static final String BATCH_SESSION_BODY="pdf-batch-session-body";

	public static final String SESSION="session";

	public static final String TABLE_COURSE_BATCH_ATTRIBUTES="batch_attributes";

	public static final String TABLE_COURSE_SESSION_DETAILS="sessionDetails_v2";
	public static final String BATCH_ALREADY_ENROLLED_MSG = "Already Enrolled in batches.";
	public static final String BATCH_AUTO_ENROLL_ERROR_MSG = "Failed to auto enrol user in batch.";
	public static final String BATCH_NOT_AVAILABLE_ERROR_MSG = "No Batch Available.";
	public static final String AUTO_ENROLL_PRIMARY_CATEGORY_ERROR_MSG = "Auto enrolment is not allowed for '%s' course.";
	public static final String CONTENT_NOT_AVAILABLE = "Content Search failed for Content: %s.";
	public static final String SIGNIN_LINK = "signinLink";
	public static final String DISCOVER_LINK = "discoverLink";
	public static final String MEETING_LINK = "meetingLink";
	public static final String PROFILE_UPDATE_LINK ="profileUpdateLink";


	public static final String BLENDED_PROGRAM_NAME="blendedProgramName";

	public static final String BATCH_NAME="batchName";

	public static final String SESSION_TYPE="sessionType";

	public static final String SESSION_TYPE_OFFLINE="Offline";

	public static final String UNDER_SCORE="_";

	public static final String SESSION_NAME="sessionName";

	public static final String ASSESSMENT_INVALID = "Assessment Data doesn't contain mandatory values of expected duration.";

	public static final String USER_REGISTRATION_PRE_APPROVED_DOMAIN = "userRegistrationPreApprovedDomain";

	public static final String USER_REGISTRATION_DOMAIN = "userRegistrationDomain";
	public static final String PRESENT = "Present";

	public static final String FROM_EMAIL = "fromEmail";

	public static final String TABLE_EMAIL_TEMPLATE = "email_template";

	public static final String ABSENT = "absent";

	public static final String COURSE_NAME = "courseName";

	public static final String ATTENDANCE_MARKED = "ATTENDANCE MARKED";

	public static final Object KARMYOGI_BHARAT = "Karmyogi Bharat";

	public static final String ROOT_ORG_ID_RAW = "rootOrgId.raw";

	public static final String STATUS_RAW = "status.raw";

	public static final String COLON =":";

	public static final String TRENDING_COURSES_REDIS_KEY="lhp_trending";
	public static final String ORGANISATION = "organisation";
	public static final int NUM_WEEKS = 4;

	public static final String INSIGHTS_LEARNING_HOURS_REDIS_KEY="lhp_learningHours";

	public static final String INSIGHTS_CERTIFICATIONS_REDIS_KEY="lhp_certifications";

	public static final String INSIGHTS_TYPE_CERTIFICATE ="certification";
	public static final String INSIGHTS_TYPE_LEARNING_HOURS ="learning-hours";

	public static final String WEEKLY_CLAPS ="weekly-claps";
	public static final String NUDGES ="nudges";
	public static final String LABEL ="label";

	public static final String GROWTH = "growth";

	public static final String POSITIVE="positive";
	public static final String NEGATIVE="negative";

	public static final String YESTERDAY="yesterday";

	public static final String TODAY ="today";

	public static final String WEEK ="week";

	public static final String ACHIEVED ="achieved";

	public static final String API_USER_INSIGHTS="api.user.insights";

	public static final String API_TRENDING_SEARCH="api.trending.search";

	public static final String COURSES="courses";

	public static final String PROGRAMS="programs";

	public static final String CERTIFICATIONS="certifications";

	public static final String TREND_SEARCH_NO_RESULT_ERROR_MESG = "Doesn't have any trending {0} for the requested organization";

	public static final String TOTAL_CLAPS="total_claps";
	public static final String W1 ="w1";
	public static final String W2 ="w2";
	public static final String W3= "w3";
	public static final String W4 ="w4";

	public static final String LEARNER_STATS = "learner_stats";
	public static final String LEARNER_STATUS_USER_ID="userid";

	public static final String VERSION_KEY = "versionKey";
	public static final String AVG_RATING = "avgRating";
	public static final String TOTAL_NO_OF_RATING = "totalNoOfRating";
	public static final String COUNT_ONE_STAR_RATING = "countOf1StarRating";
	public static final String COUNT_TWO_STAR_RATING = "countOf2StarRating";
	public static final String COUNT_THREE_STAR_RATING = "countOf3StarRating";
	public static final String COUNT_FOUR_STAR_RATING = "countOf4StarRating";
	public static final String COUNT_FIVE_STAR_RATING = "countOf5StarRating";
	public static final String TOTAL_NUMBER_UPDATED_COUNT = "totalNumberOfUpdatedContent";
	public static final String TOTAL_NUMBER_ERROR_COUNT = "totalNumberOfErrorContent";
	public static final String API_RATINGS_CONTENT_META_UPDATE = "api.ratings.content.meta.update";
	public static final String API_CONTENT_META_UPDATE = "api.content.meta.update";
	public static final String REDIS_COURSE_MOST_ENROLLED_TAG = "lhp_mostEnrolledTag";
	public static final String ADDITIONAL_TAGS = "additionalTags";
	public static final String MOST_ENROLLED = "mostEnrolled";
	public static final String MOST_TRENDING = "mostTrending";
	public static final String REDIS_COURSE_MOST_TRENDING_TAG = "lhp_trending";
	public static final String ACROSS_COURSES = "across:courses";
	public static final String ACROSS_PROGRAMS = "across:programs";
	public static final String API_PROFILE_EXTERNAL_SYSTEM_UPDATE = "api.profile.external.system.update";
    public static final String ACBP_KEY = "acbp";
	public static final String ALL_USER_KEY = "allusers";
	public static final String CBP_MANUAL_COURSES_REDIS_KEY = "cbp_manual";
	public static final String CBP_MANUAL_COURSES_END_DATE = "cbPlanEndDate";
	public static final String API_CB_PLAN_CREATE = "api.cb.plan.v1.create";
	public static final String API_CB_PLAN_UPDATE = "api.cb.plan.v1.update";
	public static final String API_CB_PLAN_PUBLISH = "api.cb.plan.v1.publish";
	public static final String API_CB_PLAN_READ_BY_ID = "api.cb.plan.v1.read.byId";
	public static final String API_CB_PLAN_RETIRE = "api.cb.plan.v1.retire";
	public static final String CREATED_AT = "createdAt";
	public static final String DRAFT_DATA = "draftData";
	public static final String TABLE_CB_PLAN = "cb_plan";

	public static final String CB_CONTENT_LIST = "contentList";
	public static final String CB_CONTENT_TYPE = "contentType";
	public static final String CB_ASSIGNMENT_TYPE = "assignmentType";
	public static final	String CB_CREATED_AT	= "createdAt";
	public static final	String CB_ASSIGNMENT_TYPE_INFO = "assignmentTypeInfo";
	public static final String UPDATED_AT = "updatedAt";
	public static final	String CB_PUBLISHED_BY	= "publishedBy";
	public static final String CB_PUBLISHED_AT = "publishedAt";
	public static final String CB_PLAN_ID = "cb_plan_id";
	public static final String CB_ASSIGNMENT_TYPE_INFO_KEY = "assignmentTypeInfoKey";
	public static final String TABLE_CB_PLAN_LOOKUP = "cb_plan_lookup";
	public static final String CB_IS_ACTIVE = "isActive";
	public static final String API_V2_READ_DEPT_POSITION="v2.read.dept.position";
	public static final String API_VERSION_2 = "2.0";
	public static final String DEPT_KEY="dept_key";
	public static final String ORG_DESIGNATION ="org_designations";
	public static final String CB_RETIRE = "RETIRE";
	public static final String USER_DETAILS = "userDetails";
	public static final String COMPETENCIES_V5 = "competencies_v5";

	public static final String COMPETENCIES_V3 = "competencies_v3";

	public static final String CONTENT_LIST = "contentList";

	public static final String CB_ALL_USERS_TYPE = "allUser";

	public static final String CB_CUSTOM_TYPE = "customUser";

	public static final String CB_DESIGNATION_TYPE = "designation";
	public static final String CBP_PLAN_USER_LIST_API ="api.cbplan.user.list";
	public static final String CBP_PLAN_CONTENT_REQUEST_API ="api.cbplan.content.request";
	public static final String USER_TYPE = "userType";
	public static final String CB_PLAN_ID_KEY = "cbPlanId";
	public static final String ALL_USER = "AllUser";
	public static final String API_CB_PLAN_LIST = "api.cbplan.list";
	public static final String COURSE_APP_ICON = "appIcon";
	public static final String CREATOR_LOGO = "creatorLogo";
	public static final String CREATED_BY_NAME = "createdByName";
    public static final String USER_KARMA_POINTS ="user_karma_points";
	public static final String KARMA_POINTS_USER_ID = "userid";
    public static final String CONTEXT_ID ="context_id";
	public static final String X_AUTH_USER_ROLES = "x-authenticated-user-roles";
    public static final String MDO_ID ="mdo_id";
    public static final String MONTH = "month";
    public static final String MDO_KARMA_POINTS="mdo_karma_points";
    public static final String NEGATIVE_OR_POSITIVE ="negativeOrPositive";
    public static final String ORGID="org_id";
    public static final String RANK ="rank";
    public static final String YEAR ="year";
    public static final String AVERAGE_KP ="average_kp";
    public static final String MDO_LIST="mdoList";
	public static final String KARMA_POINTS_LIST = "kpList";
	public static final String DB_COLUMN_CREDIT_DATE = "credit_date";
	public static final String DB_CLOUMN_CONTEXT_TYPE="context_type";

	public static final String DB_COLUMN_CONTEXT_ID = "context_id";

	public static final String DB_COLUMN_OPERATION_TYPE = "operation_type";

	public static final String COURSE_COMPLETION = "COURSE_COMPLETION";

	public static final String CONTEXT_ID_CAMEL ="contextId";

	public static final String CONTEXT_TYPE_CAMEL= "contextType";

	public static final String DB_COLUMN_USER_KARMA_POINTS_KEY = "user_karma_points_key";


	public static final String TABLE_KARMA_POINTS_LOOK_UP ="user_karma_points_credit_lookup";
	public static final String TABLE_KARMA_POINTS ="user_karma_points";
    public static final String COMPETENCY_INFO = "competencyInfo";
	public static final String PROVIDER_ORG_ID = "providerOrgId";
	public static final String CB_CONTENT_REQUEST_TABLE = "cb_content_request";
	public static final String ORG_ID_MISSING = "Root Org Ids missing, please send the org Ids for content request";
	public static final String COMPETENCY_DETAILS_MISSING = "Competency details missing for content request";

	public static final String TABLE_USER_KARMA_POINTS_SUMMARY ="user_karma_points_summary";
    public static final String REQUEST_CONTENT_SUBJECT = "New Content Request for Capacity Building Plan Development";
	public static final String COMPETENCY_THEMES = "competencyThemes";
	public static final String COMPETENCY_SUB_THEMES = "competencySubThemes";
	public static final String FOOTNOTE = "footnote";
	public static final String FIRST_PARA = "firstPara";
	public static final String SECOND_PARA = "secondPara";
	public static final String MDO_NAME = "mdoName";
	public static final String PROVIDER_EMAIL_ID_LIST = "providerEmailIdList";
	public static final String CBP_ADMIN = "CBP_Admin";
	public static final String ORGANIZATIONS_ROLES = "organisations.roles";
	public static final String MDO_NAME_PARAM= "mdo_name";
	public static final String FIRST_BODY_PARAM = "body_para1"; //"body_para2"
	public static final String SECOND_BODY_PARAM = "body_para2";
	public static final String COMPETENCY_AREA_PARAM = "competency_area";
	public static final String COMPETENCY_THEME_PARAM = "competency_theme";
	public static final String COMPETENCY_SUB_THEME_PARAM = "competency_subtheme";
	public static final String COPY_EMAIL = "copyEmail";
	public static final String SEQ_NO = "seqNo";
	public static final String USER_CONTENT_RECOMMENDATION = "user.v1.content.recommendation";
	public static final String RECOMMEND_CONTENT_SUBJECT = " recommends this iGOT Karmayogi #primaryCategory for you!";
	public static final String COURSE_POSTER_IMAGE_URL= "coursePosterImageUrl";
	public static final String COURSE_PROVIDER = "courseProvider";
	public static final String RECIPIENTS = "recipients";
	public static final String OVERVIEW = "overview";
	public static final String CATEGORY = "category";
	public static final String ORG_ID_LIST = "orgIdList";
	public static final String EHRMS = "ehrms";
	public static final String EMP_CODE = "emp_code";
	public static final String EHRMS_AUTH_USERNAME = "username";
	public static final String EHRMS_AUTH_PASSWORD = "password";
	public static final String USER_ID_LOWER = "userid";
	public static final String EHRMS_USER_TOKEN = "ehrmsUserToken";
	public static final String API_READ_WHEEBOX_RESULT = "api.wheebox.read.result";
	public static final String DOC_ID = "docId";
	public static final String DIGI_LOCKER_ID = "digiLockerId";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String CERTIFICATE_ID = "certificateId";
	public static final String CERTIFICATE_NAME = "certificateName";
	public static final String LAST_ISSUED_ON = "lastIssuedOn";
	public static final String TABLE_DIGILOCKER_DOC_INFO = "digilocker_doc_lookup";
	public static final String ISSUED_USER_CERTIFICATE = "issuedCertificates";
	public static final String DOC_TYPE = "docType";
	public static final String LEARNER_LEADER_BOARD = "learnerLeaderBoard";
  public static final String PRIMARY_CATEGORY_TAG = "#primaryCategory";
  public static final String PASSWORD = "password";
	public static final String STRING_FORMAT_UNZIP="%s/%s/%s/%s";
	public static final String UNZIP_PATH="/unzippath";
	public static final String OUTPUT_PATH="output";
	public static final String MDO_REPORT_ACCESSOR = "MDO_REPORT_ACCESSOR";
	public static final String GRANT_REPORT_ACCESS_API ="api.grant.access.report";
	public static final String MDO_ADMIN_ROLE = "mdoadmin";
	public static final String MDO_ADMIN = "MDO_ADMIN";
	public static final String REPORT_ACCESS_EXPIRY_TABLE = "user_report_expiry";
	public static final String REPORT_EXPIRY_DATE  = "reportExpiryDate";
	public static final String ERROR  = "error";
	public static final String CONTEXT_TOKEN  = "contextToken";
	public static final String TABLE_LEARNER_LEADER_BOARD_LOOK_UP="learner_leaderboard_lookup";
	public static final String TABLE_LEARNER_LEADER_BOARD="learner_leaderboard";
	public static final String DB_COLUMN_ROW_NUM ="row_num";
	public static final String GET_FILE_INFO_OPERATIONAL_REPORTS ="api.getfileinfo.report";
	public static final String X_DIGILOCKER_HMAC = "x-digilocker-hmac";
	public static final String LAST_MODIFIED="lastModified";
	public static final String FILE_METADATA="fileMetaData";
	public static final String USER_REPORT_EXPIRY_DATE = "report_expiry_date";
	public static final String REPORT_ACCESS_EXPIRY = "reportAccessExpiry";
	public static final String READ_REPORT_ACCESS_API = "api.read.access.report";
	public static final String API_SECTOR_LIST = "api.sector.list";
	public static final String TERMS = "terms";
	public static final String SECTORS = "sectors";
	public static final String API_SECTOR_READ = "api.read.sector";
	public static final String API_SECTOR_CREATE = "api.create.sector";
	public static final String API_SUB_SECTOR_CREATE = "api.create.sub.sector";
	public static final String TERM = "term";
	public static final String SECTOR = "sector";
	public static final String IMG_URL_KEY = "imgUrl";
	public static final String CODE = "code";
	public static final String PARENTS = "parents";
	public static final String SUB_SECTORS = "subsectors";
	public static final String COURSE_LINK = "courseLink";
	public static final String NEGATIVE_MARKING_PERCENTAGE = "negativeMarkingPercentage";
	public static final String ASSESSMENT_TYPE = "assessmentType";
	public static final String TOTAL_MARKS = "totalMarks";
	public static final String QUESTION_SECTION_SCHEME = "questionSectionScheme";
	public static final String OPTION_WEIGHTAGE = "optionalWeightage";
	public static final String QUESTION_WEIGHTAGE = "questionWeightage";
	public static final String QUESTION_LEVEL = "questionLevel";
	public static final String TOTAL_SECTION_MARKS = "totalSectionMarks";
	public static final String SECTION_RESULT = "sectionResult";
	public static final String SECTION_MARKS= "sectionMarks";
	public static final String FAIL = "fail";
	public static final String API_CALENDAR_EVENT_BULK_UPLOAD = "api.calendar.event.bulk.upload";
	public static final String TABLE_CALENDAR_EVENT_BULK_UPLOAD = "calendar_event_bulk_upload";
	public static final String EVENT = "event";
	public static final String REGISTRATION_LINK = "registrationLink";
	public static final String EVENT_TYPE = "eventType";
	public static final String ADDRESS = "address";
	public static final String LOCATION = "location";
	public static final String SOURCE_NAME = "sourceName";
	public static final String CALENDAR = "Calendar";
	public static final String RESOURCE_TYPE = "resourceType";
	public static final String REGISTRATION_END_DATE = "registrationEndDate";
	public static final String EVENT_KEY = "Event";
	public static final String SLASH = "/";
	public static final String API_MICRO_SITE_INSIGHTS ="api.micro-site.insights";
  	public static final String TOP_COMMENTS_ORG_REDIS_KEY = "cbp_top_10_users_reviews_by_org";
	public static final String PROFILE_IMAGE_URL = "profileImageUrl";
	public static final String API_TOD_COMMENT_FOR_USER = "api.rating.top.v1.comment";
	public static final String CONTENT_INFO = "contentInfo";
	public static final String COURSE_ID_KEY = "courseID";
	public static final String USER_ID_KEY = "userID";
	public static final String PAGE_ID = "pageId";
	public static final String USERS_LIST = "userList";
	public static final String TOTAL_COUNT = "totalCount";
	public static final String TOO_MANY_REQUESTS = "TOO_MANY_REQUESTS";
	public static final String SERVER_ERROR = "serverError";
	public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
	public static final String MICROSITE_TOP_CONTENT_API = "api.microsite.top.content";
	public static final String ICON = "icon";
	public static final String DOMICILE_MEDIUM = "domicileMedium";
	public static final String PINCODE = "pinCode";
	public static final String EMPLOYEE_CODE = "employeeCode";
	public static final String OPTION_WEIGHT = "optionWeight";
	public static final String PROFILE_STATUS = "profileStatus";
	public static final String NOT_VERIFIED = "NOT-VERIFIED";
	public static final String PROFILE_STATUS_UPDATED_ON = "profileStatusUpdatedOn";
	public static final String PROFILE_STATUS_UPDATED_MSG_VIEWED = "isProfileUpdatedMsgViewed";
	public static final String PROFILE_GROUP_STATUS = "profileGroupStatus";
	public static final String PROFILE_DESIGNATION_STATUS = "profileDesignationStatus";
	public static final String NOT_MY_USER = "NOT-MY-USER";
	public static final String SPACE = " ";
	public static final String REQUEST_TYPE = "requestType";
	public static final String INSIGHT_FIELD_KEY = ".insights.fields";
	public static final String INSIGHT_REDIS_KEY_MAPPING = ".insights.redis.key.mapping";
	public static final String INSIGHT_PROPERTY_FIELDS = ".insights.property.fields";
	public static final String TIME_TAKEN_FOR_ASSESSMENT= "timeTakenForAssessment";
	public static final String RETAKE_ATTEMPT_CONSUMED = "retakeAttemptsConsumed";
	public static final String TOTAL_PERCENTAGE = "totalPercentage";
	public static final String SECTION_LEVEL_DEFINITION = "sectionLevelDefinition";

	public static final String ASSESSMENT_HIERARCHY_SAVE_NOT_AVBL = "Assessment hierarchy save point not available, failed to process request";
	public static final String SEQUENCE_NO = "seqno";
	public static final String API_EXPLORE_COURSE_UPDATE = "api.explore.course.update";
	public static final String API_EXPLORE_COURSE_DELETE = "api.explore.course.delete";
	public static final String COMPETENCIES_ORG_COURSES_REDIS_KEY = "dashboard_core_competencies_by_user_org";
	public static final String ORG_COMPETENCY_SEARCH_API = "api.org.competency.search";
	public static final String SEARCH_COMPETENCY_THEMES = "competencyTheme";
	public static final String SEARCH_COMPETENCY_SUB_THEMES = "competencySubTheme";
	public static final String MCQ_MCA_W = "mcq-mca-w";
	public static final String MCQ_SCA_TF = "mcq-sca-tf";
	public static final String CSV_FILE = ".csv";
	public static final String XLSX_FILE = ".xlsx";

	private Constants() {
		throw new IllegalStateException("Utility class");
	}

}
