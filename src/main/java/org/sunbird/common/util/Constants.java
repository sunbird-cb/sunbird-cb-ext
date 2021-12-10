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

	public static final String SUCCESSFUL = "Successful";
	public static final String FAILED = "Failed";
	public static final String MESSAGE = "message";
	public static final String DATA = "data";
	public static final String STATUS = "status";

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
	public static final String API_BUDGET_SCHEME_UPDATE = "api.budget.schme.update";
	public static final String API_BUDGET_SCHEME_READ = "api.budget.scheme.read";
	public static final String API_BUDGET_SCHEME_DELETE = "api.budget.scheme.delete";
	public static final String API_BUDGET_SCHEME_HISTORY_READ = "api.budget.scheme.history.read";
	public static final String API_FILE_UPLOAD = "api.file.upload";
	public static final String API_FILE_DELETE = "api.file.delete";

	public static final String ID = "id";
	public static final String ORG_ID = "orgId";
	public static final String POSITION = "position";
	public static final String TOTAL_POSITION_FILLED = "totalPositionsFilled";
	public static final String TOTAL_POSITION_VACANT = "totalPositionsVacant";
	public static final String NAME = "name";
	public static final String URL = "url";
	public static final String DELETED = "deleted";

	public static final String BUDGET = "budget";
	public static final String BUDGET_YEAR = "budgetYear";
	public static final String SCHEME_NAME = "schemeName";
	public static final String SALARY_BUDGET_ALLOCATED = "salaryBudgetAllocated";
	public static final String TRAINING_BUDGET_ALLOCATED = "trainingBudgetAllocated";
	public static final String TRAINING_BUDGET_UTILIZATION = "trainingBudgetUtilization";

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
	public static final String SUCCESS = "SUCCESS";
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
	public static final String DATABASE = "sunbird";
	public static final String BUDGET_TABLE = "org_budget_scheme";
	public static final String AUDIT_TABLE = "org_audit";
	public static final String STAFF_TABLE = "org_staff_position";
	public static final String WORK_ORDER = "work_order";
	public static final String WORK_ALLOCATION = "work_allocation";
	public static final String WORK_ALLOCATION_MAPPING = "user_work_allocation_mapping";
	public static final String MANDATORY_CONTENT = "mandatory_user_content";

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

}
