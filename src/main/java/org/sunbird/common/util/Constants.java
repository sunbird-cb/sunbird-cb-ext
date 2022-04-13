package org.sunbird.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String UUID = "wid";
    public static final String KID = "kid";
    public static final String SOURCE_ID = "source_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String MIDDLE_NAME = "middle_name";
    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String CONTACT_PHONE_NUMBER_OFFICE = "contact_phone_number_office";
    public static final String CONTACT_PHONE_NUMBER_HOME = "contact_phone_number_home";
    public static final String CONTACT_PHONE_NUMBER_PERSONAL = "contact_phone_number_personal";
    public static final String KNOWN_AS = "known_as";
    public static final String GENDER = "gender";
    public static final String DOB = "dob";
    public static final String SALUTATION = "salutation";
    public static final String EMAIL = "email";
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
    public static final String ISSUED_CERTIFICATES = "issued_certificates";
    public static final String IS_MDO = "isMdo";
    public static final String IS_CBP = "isCbp";
    public static final String PROFILE_DETAILS_DESIGNATION = "profileDetails.professionalDetails.designation";
    public static final String PROFILE_DETAILS_DESIGNATION_OTHER = "profileDetails.professionalDetails.designationOther";
    public static final String PROFILE_DETAILS_PRIMARY_EMAIL = "profileDetails.personalDetails.primaryEmail";
    public static final String DEPARTMENT = "department";
    public static final String DESIGNATION = "designation";
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


    public static final String ID = "id";
    public static final String ORG_ID = "orgId";
    public static final String POSITION = "position";
    public static final String TOTAL_POSITION_FILLED = "totalPositionsFilled";
    public static final String TOTAL_POSITION_VACANT = "totalPositionsVacant";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String DELETED = "deleted";
    public static final String SCHEME_ID = "schemeId";

    public static final String BUDGET = "budget";
    public static final String BUDGET_YEAR = "budgetYear";
    public static final String SCHEME_NAME = "schemeName";
    public static final String SALARY_BUDGET_ALLOCATED = "salaryBudgetAllocated";
    public static final String TRAINING_BUDGET_ALLOCATED = "trainingBudgetAllocated";
    public static final String TRAINING_BUDGET_UTILIZATION = "trainingBudgetUtilization";

    //ratings and review
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
    public static final String USER_LAST_NAME = "lastName";
    public static final String USERID = "id";
    public static final String COMMENT = "comment";
    public static final String COMMENT_BY = "commentby";
    public static final String COMMENT_UPDATED_ON = "commentupdatedon";
    public static final String CREATED_ON = "createdon";
    public static final String RATING = "rating";
    public static final String REVIEW = "review";
    public static final String UPDATED_ON = "updatedon";
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
    public static final String TABLE_ORG_AUDIT = "org_audit";
    public static final String TABLE_ORG_STAFF_POSITION = "org_staff_position";
    public static final String TABLE_WORK_ORDER = "work_order";
    public static final String TABLE_WORK_ALLOCATION = "work_allocation";
    public static final String TABLE_USER_WORK_ALLOCATION_MAPPING = "user_work_allocation_mapping";
    public static final String TABLE_MANDATORY_USER_CONTENT = "mandatory_user_content";
    public static final String TABLE_ORGANIZATION = "organisation";
    public static final String TABLE_USER_ENROLMENT = "user_enrolments";
    public static final String TABLE_USER = "user";
    public static final String TABLE_COURSE_BATCH = "course_batch";
    public static final String TABLE_RATINGS = "ratings";
    public static final String TABLE_RATINGS_LOOKUP = "ratings_lookup";
    public static final String TABLE_RATINGS_SUMMARY = "ratings_summary";
    public static final String VALUE = "value";
    public static final String DEPT_NAME = "deptName";
    public static final String PROFILE_DETAILS = "profileDetails";
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

    // Redis
    public static final String API_REDIS_DELETE = "api.redis.delete";
    public static final String API_REDIS_GET_KEYS = "api.redis.get.keys";
    public static final String API_REDIS_GET_KEYS_VALUE_SET = "api.redis.get.keys&values";
    public static final String REDIS_COMMON_KEY = "CB_EXT_";

    public static final String COMPETENCY_CACHE_NAME = "competency";
    public static final String COMPETENCY_CACHE_NAME_BY_AREA = "competencyByArea";
    public static final String COMPETENCY_CACHE_NAME_BY_TYPE = "competencyByType";
    public static final String PROVIDER_CACHE_NAME = "provider";
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

    //telemetry audit constants
    public static final String ORG_LIST = "ORGANISATION_LIST";
	public static final String VERSION = "3.0.2";
	public static final String EID = "AUDIT";
	public static final String PDATA_ID = "dev.sunbird.cb.ext.service";
	public static final String PDATA_PID = "sunbird-cb-ext-service";
	public static final String TYPE = "WorkOrder";
	public static final String CURRENT_STATE = "FirstLogin";
	public static final String LOGIN_TIME = "login_time";
	public static final List<String> PROPS = Collections.unmodifiableList(Arrays.asList("WAT"));
	public static final String USER_CONST = "User";

}
