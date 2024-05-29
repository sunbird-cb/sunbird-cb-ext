package org.sunbird.user.registration.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiRespContent;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.model.SunbirdApiResultResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.producer.Producer;
import org.sunbird.org.service.ExtendedOrgService;
import org.sunbird.portal.department.model.DeptPublicInfo;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.UserRegistrationInfo;
import org.sunbird.user.registration.util.UserRegistrationStatus;
import org.sunbird.user.service.UserUtilityService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

	private Logger LOGGER = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	IndexerService indexerService;

	@Autowired
	Producer kafkaProducer;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	UserUtilityService userUtilityService;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	ExtendedOrgService extOrgService;

	@Autowired
	CassandraOperation cassandraOperation;

	@Override
	public SBApiResponse registerUser(UserRegistrationInfo userRegInfo) {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_REGISTER_API);
		String errMsg = validateRegisterationPayload(userRegInfo);
		if (StringUtils.isBlank(errMsg)) {
			try {
				if (userUtilityService.isUserExist(Constants.EMAIL, userRegInfo.getEmail().toLowerCase())) {
					errMsg = Constants.EMAIL_EXIST_ERROR;
				}
				if (userUtilityService.isUserExist(Constants.PHONE, userRegInfo.getPhone())) {
					errMsg = Constants.PHONE_NUMBER_EXIST_ERROR;
				} else {
					// verify the given email exist in ES Server
					UserRegistration regDocument = getUserRegistrationDocument(new HashMap<String, Object>() {
						private static final long serialVersionUID = 1L;
						{
							put(Constants.EMAIL, userRegInfo.getEmail());
						}
					});

					if (regDocument == null
							|| UserRegistrationStatus.FAILED.name().equalsIgnoreCase(regDocument.getStatus())) {
						// create / update the doc in ES
						RestStatus status = null;
						if (regDocument == null) {
							regDocument = getRegistrationObject(userRegInfo);
							status = indexerService.addEntity(serverProperties.getUserRegistrationIndex(),
									serverProperties.getEsProfileIndexType(), regDocument.getRegistrationCode(),
									mapper.convertValue(regDocument, Map.class));
						} else {
							updateValues(regDocument, userRegInfo);
							regDocument.setStatus(UserRegistrationStatus.CREATED.name());
							status = indexerService.updateEntity(serverProperties.getUserRegistrationIndex(),
									serverProperties.getEsProfileIndexType(), regDocument.getRegistrationCode(),
									mapper.convertValue(regDocument, Map.class));
						}

						if (status.equals(RestStatus.CREATED) || status.equals(RestStatus.OK)) {
							if (isPreApprovedDomain(regDocument.getEmail())) {
								// Fire createUser event
								kafkaProducer.push(serverProperties.getUserRegistrationAutoCreateUserTopic(),
										regDocument);
							} else {
								// Fire register event
								kafkaProducer.push(serverProperties.getUserRegistrationTopic(), regDocument);
							}
							response.setResponseCode(HttpStatus.ACCEPTED);
							response.getResult().put(Constants.RESULT, regDocument);
						} else {
							response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
							response.getParams().setErrmsg("Failed to add details to ES Service");
						}
					} else {
						errMsg = Constants.EMAIL_EXIST_ERROR;
					}
				}
			} catch (Exception e) {
				LOGGER.error(String.format("Exception in %s : %s", "registerUser", e.getMessage()), e);
				errMsg = "Failed to process message. Exception: " + e.getMessage();
			}
		}
		if (StringUtils.isNotBlank(errMsg)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Override
	public SBApiResponse getUserRegistrationDetails(String registrationCode) {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_RETRIEVE_API);
		UserRegistration userRegistration = getUserRegistrationForRegCode(registrationCode);
		if (userRegistration != null) {
			response.getResult().put(Constants.RESULT, userRegistration);
		} else {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg("Failed to get response");
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	public SBApiResponse getDeptDetails() {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_DEPT_INFO_API);

		try {
			String strDeptListMap = redisCacheMgr.getCache(Constants.DEPARTMENT_LIST_CACHE_NAME);
			Map<String, List<DeptPublicInfo>> deptListMap = new HashMap<>();
			if (StringUtils.isNotBlank(strDeptListMap)) {
				deptListMap = mapper.readValue(strDeptListMap,
						new TypeReference<Map<String, List<DeptPublicInfo>>>() {
						});
			}
			List<DeptPublicInfo> orgList = null;
			if (ObjectUtils.isEmpty(deptListMap)
					|| CollectionUtils.isEmpty(deptListMap.get(Constants.DEPARTMENT_LIST_CACHE_NAME))) {
				orgList = getDepartmentDetails();
			} else {
				orgList = deptListMap.get(Constants.DEPARTMENT_LIST_CACHE_NAME);
			}
			response.getResult().put(Constants.COUNT, orgList.size());
			response.getResult().put(Constants.CONTENT, orgList);
		} catch (Exception e) {
			LOGGER.error("Exception occurred in getDeptDetails", e);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setErrmsg("Exception occurred in getDeptDetails. Exception: " + e.getMessage());
		}

		return response;
	}

	public SBApiResponse generateOTP(Map<String, Object> otpRequests) {
		SBApiResponse response = createDefaultResponse(Constants.USER_GENERATE_OTP);
		String errMsg = validateOTPPayload(otpRequests);
		if (StringUtils.isBlank(errMsg)) {
			try {
				String url = serverProperties.getSbUrl() + serverProperties.getSbOTPGeneratePath();
				Map<String, String> headers = new HashMap();
				headers.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
				Map<String, Object> apiResponse = outboundRequestHandlerService.fetchResultUsingPost(url, otpRequests,
						headers);
				if (Constants.OK.equalsIgnoreCase((String) apiResponse.get(Constants.RESPONSE_CODE))) {
					LOGGER.info("OTP Generation successful");
					response.setVer("v1");
					response.getParams().setStatus(Constants.SUCCESS.toUpperCase());
					response.getParams().setResmsgid(UUID.randomUUID().toString());
					response.getParams().setMsgid(UUID.randomUUID().toString());
					response.setResult(new HashMap<>());
					response.getResult().put(Constants.RESPONSE, Constants.SUCCESS.toUpperCase());
				} else {
					errMsg = (String) ((Map<String, Object>)apiResponse.get(Constants.PARAMS)).get(Constants.ERROR_MESSAGE);
				}
			} catch (Exception e) {
				LOGGER.error(String.format("Exception in %s : %s", "generateOTP", e.getMessage()), e);
				errMsg = "Failed to process message. Exception: " + e.getMessage();
			}
		}
		if (StringUtils.isNotBlank(errMsg)) {
			LOGGER.error("OTP generation request failed, error message : ",errMsg);
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(errMsg);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public void initiateCreateUserFlow(String registrationCode) {
		try {
			/**
			 * 1. Create User 2. Read created User 3. Update User 4. Create NodeBB user Id
			 * 5. Assign Role 6. Reset Password and get activation link
			 */
			LOGGER.info("Initiated User Creation flow for Reg. Code :: " + registrationCode);
			UserRegistration userReg = getUserRegistrationForRegCode(registrationCode);

			// Create the org if it's not already onboarded.
			if ("null".equalsIgnoreCase(userReg.getSbOrgId()) || StringUtils.isEmpty(userReg.getSbOrgId())) {
				SBApiResponse orgResponse = extOrgService.createOrgForUserRegistration(getOrgCreateRequest(userReg));
				if (orgResponse.getResponseCode() == HttpStatus.OK) {
					String orgId = (String) orgResponse.getResult().get(Constants.ORGANIZATION_ID);
					userReg.setSbOrgId(orgId);
					LOGGER.info(String.format("Auto on-boarded organisation with Name: %s, MapId: %s, OrgId: %s",
							userReg.getOrgName(), userReg.getMapId(), userReg.getSbOrgId()));
					// TODO - Need to find a best way to give time for org creation takes effect.
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
				} else {
					try {
						LOGGER.error("Failed to auto onboard organisation. Error: "
								+ (new ObjectMapper()).writeValueAsString(orgResponse));
					} catch (Exception e) {
					}
					return;
				}
			}

			UserRegistrationStatus regStatus = UserRegistrationStatus.WF_APPROVED;
			if (userUtilityService.createUser(userReg)) {
				LOGGER.info("Successfully completed user creation flow.");
			} else {
				LOGGER.error("Failed to create user for Reg.Code :: " + registrationCode);
				regStatus = UserRegistrationStatus.FAILED;
			}

			userReg.setStatus(regStatus.name());
			RestStatus status = indexerService.updateEntity(serverProperties.getUserRegistrationIndex(),
					serverProperties.getEsProfileIndexType(), userReg.getRegistrationCode(),
					mapper.convertValue(userReg, Map.class));

			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("UserRegistration Code :: '").append(userReg.getRegistrationCode());
			strBuilder.append("'. Create User Flow is ")
					.append(regStatus == UserRegistrationStatus.WF_APPROVED ? " successful" : " failed");
			strBuilder.append(". ES object update operation is ")
					.append(status == RestStatus.OK ? " successful." : " failed.");

			LOGGER.info(strBuilder.toString());
		} catch (Exception e) {
			LOGGER.error("Failed to process user create flow.", e);
		}
	}

	private SBApiResponse createDefaultResponse(String api) {
		SBApiResponse response = new SBApiResponse();
		response.setId(api);
		response.setVer(Constants.API_VERSION_1);
		response.setParams(new SunbirdApiRespParam(UUID.randomUUID().toString()));
		response.getParams().setStatus(Constants.SUCCESS);
		response.setResponseCode(HttpStatus.OK);
		response.setTs(DateTime.now().toString());
		return response;
	}

	private String validateRegisterationPayload(UserRegistrationInfo userRegInfo) {
		StringBuffer str = new StringBuffer();
		List<String> errList = new ArrayList<String>();
		if (StringUtils.isBlank(userRegInfo.getFirstName())) {
			errList.add("FirstName");
		}
		if (StringUtils.isBlank(userRegInfo.getEmail())) {
			errList.add("Email");
		}
		if (StringUtils.isBlank(userRegInfo.getSbOrgId()) && StringUtils.isBlank(userRegInfo.getMapId())) {
			errList.add("DeptId [or] MapId is mandatory.");
		}
		if (StringUtils.isBlank(userRegInfo.getOrgName())) {
			errList.add("OrgName");
		}
		if (StringUtils.isBlank(userRegInfo.getGroup())) {
			errList.add("Group");
		}
		if (StringUtils.isBlank(userRegInfo.getSource())) {
			errList.add("Source");
		}
		if(StringUtils.isBlank(userRegInfo.getPhone())) {
			errList.add("Phone");
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Register User Details. Missing Params - [").append(errList.toString()).append("]");
		}
		// email Validation
		if (StringUtils.isNotBlank(userRegInfo.getEmail())) {
			String validateErr = emailValidation(userRegInfo.getEmail());
			if (StringUtils.isNotBlank(validateErr)){
				str.setLength(0);
				str.append(validateErr);
			}
		}
		if(StringUtils.isNotBlank(userRegInfo.getPhone()) && !ProjectUtil.validateContactPattern(userRegInfo.getPhone())) {
			str.setLength(0);
			str.append("Invalid phone number");
		}
		//group validation
		if (StringUtils.isNotBlank(userRegInfo.getGroup()) && !serverProperties.getBulkUploadGroupValue().contains(userRegInfo.getGroup())) {
			str.setLength(0);
			str.append("Invalid Group : Group can be only among one of these ").append(serverProperties.getBulkUploadGroupValue());
		}
		return str.toString();
	}

	private String validateOTPPayload(Map<String,Object> otpRequest) {
		StringBuffer str = new StringBuffer();
		Map<String,Object> request = (Map<String, Object>) otpRequest.get("request");
		List<String> errList = new ArrayList<String>();
		if (ObjectUtils.isEmpty(request.get(Constants.KEY))) {
			errList.add(Constants.KEY);
		}
		if (ObjectUtils.isEmpty(Constants.TYPE)) {
			errList.add(Constants.TYPE);
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Generate OTP. Missing Params - [").append(errList.toString()).append("]");
		}
		// email Validation
		if (request.get(Constants.TYPE).equals(Constants.EMAIL)) {
			String validateErr = emailValidation((String) request.get(Constants.KEY));
				if (StringUtils.isNotBlank(validateErr)){
					str.append(validateErr);
				}
			}
		return str.toString();
	}

	private UserRegistration getUserRegistrationDocument(Map<String, Object> mustMatch) throws Exception {
		SearchResponse searchResponse = indexerService.getEsResult(serverProperties.getUserRegistrationIndex(),
				serverProperties.getEsProfileIndexType(), queryBuilder(mustMatch), false);

		if (searchResponse.getHits().getTotalHits() > 0) {
			SearchHit hit = searchResponse.getHits().getAt(0);
			return mapper.convertValue(hit.getSourceAsMap(), UserRegistration.class);
		}

		return null;
	}

	private UserRegistration getRegistrationObject(UserRegistrationInfo userRegInfo) {
		UserRegistration userRegistration = new UserRegistration();
		userRegistration.setFirstName(userRegInfo.getFirstName());
		userRegistration.setEmail(userRegInfo.getEmail());
		userRegistration.setSbOrgId(userRegInfo.getSbOrgId());
		userRegistration.setOrgName(userRegInfo.getOrgName());
		userRegistration.setChannel(userRegInfo.getChannel());
		userRegistration.setSbRootOrgId(userRegInfo.getSbRootOrgId());
		if (StringUtils.isNotBlank(userRegInfo.getPosition())) {
			userRegistration.setPosition(userRegInfo.getPosition());
		}
		userRegistration.setSource(userRegInfo.getSource());
		userRegistration.setMapId(userRegInfo.getMapId());
		userRegistration.setOrganisationType(userRegInfo.getOrganisationType());
		userRegistration.setOrganisationSubType(userRegInfo.getOrganisationSubType());
		userRegistration.setPhone(userRegInfo.getPhone());
		userRegistration.setGroup(userRegInfo.getGroup());

		if (StringUtils.isBlank(userRegInfo.getRegistrationCode())) {
			userRegistration.setRegistrationCode(serverProperties.getUserRegCodePrefix() + "-"
					+ userRegistration.getMapId() + "-" + RandomStringUtils.random(8, Boolean.TRUE, Boolean.TRUE));
			userRegistration.setCreatedOn(new Date().getTime());
		} else {
			userRegistration.setUpdatedOn(new Date().getTime());
		}
		userRegistration.setStatus(UserRegistrationStatus.CREATED.name());

		return userRegistration;
	}

	/**
	 * Elasticsearch must match bool search query builder
	 * 
	 * @param mustMatch Map<String, Object>
	 * @return SearchSourceBuilder
	 */
	private SearchSourceBuilder queryBuilder(Map<String, Object> mustMatch) {
		BoolQueryBuilder boolBuilder = new BoolQueryBuilder();

		for (Map.Entry<String, Object> entry : mustMatch.entrySet()) {
			boolBuilder.must(QueryBuilders.termQuery(entry.getKey() + ".raw", entry.getValue()));
		}
		return new SearchSourceBuilder().query(boolBuilder);
	}

	/**
	 * Check the email id is valid or not
	 * 
	 * @param email String
	 * @return Boolean
	 */
	public String emailValidation(String email) {
		StringBuffer str = new StringBuffer();
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";
		Pattern pat = Pattern.compile(emailRegex);
		if (pat.matcher(email).matches()) {
			String emailDomain = email.split("@")[1];
			Boolean retValue = isApprovedDomains(emailDomain, Constants.USER_REGISTRATION_DOMAIN)
					|| isApprovedDomains(emailDomain, Constants.USER_REGISTRATION_PRE_APPROVED_DOMAIN);
			if (!retValue) {
				str.append("Email domain of this email address is not approved. Please use Request for help.");
			}
		} else {
			str.append("Invalid Email id");
		}
		return str.toString();
	}

	private Boolean isPreApprovedDomain(String email) {
		return isApprovedDomains(email.split("@")[1], Constants.USER_REGISTRATION_PRE_APPROVED_DOMAIN);
	}

	private UserRegistration getUserRegistrationForRegCode(String registrationCode) {
		try {
			Map<String, Object> esObject = indexerService.readEntity(serverProperties.getUserRegistrationIndex(),
					serverProperties.getEsProfileIndexType(), registrationCode);
			return mapper.convertValue(esObject, UserRegistration.class);
		} catch (Exception e) {
			LOGGER.error(String.format("Exception in %s : %s", "getUserRegistrationDetails", e.getMessage()));
		}
		return null;
	}

	private List<String> getMasterOrgList() {
		List<String> orgList = new ArrayList<String>();
		// read file into stream, try-with-resources

		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream(serverProperties.getMasterOrgListFileName());
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = br.readLine()) != null) {
				orgList.add(line.trim());
			}
		} catch (Exception e) {
			LOGGER.error("Failed to read the master org list. Exception: ", e);
		}

		return orgList;
	}

	private List<DeptPublicInfo> getDepartmentDetails() throws Exception {
		Set<String> orgNameList = new HashSet<String>();
		List<DeptPublicInfo> orgList = new ArrayList<>();
		int count = 0;
		int iterateCount = 0;
		do {
			// request body
			Map<String, Object> requestMap = new HashMap<>();
			requestMap.put(Constants.OFFSET, iterateCount);
			requestMap.put(Constants.LIMIT, 1000);
			requestMap.put(Constants.FIELDS, new ArrayList<>(Arrays.asList(Constants.CHANNEL, Constants.IDENTIFIER)));
			Map<String, Object> sortByMap = new HashMap<String, Object>();
			sortByMap.put(Constants.CHANNEL, Constants.ASC_ORDER);
			requestMap.put(Constants.SORT_BY, sortByMap);
			requestMap.put(Constants.FILTERS, new HashMap<String, Object>() {
				{
					put(Constants.IS_TENANT, Boolean.TRUE);
				}
			});

			String serviceURL = serverProperties.getSbUrl() + serverProperties.getSbOrgSearchPath();
			SunbirdApiResp orgResponse = mapper.convertValue(
					outboundRequestHandlerService.fetchResultUsingPost(serviceURL, new HashMap<String, Object>() {
						{
							put(Constants.REQUEST, requestMap);
						}
					}), SunbirdApiResp.class);

			SunbirdApiResultResponse resultResp = orgResponse.getResult().getResponse();
			count = resultResp.getCount();
			iterateCount = iterateCount + resultResp.getContent().size();
			List<String> excludeList = serverProperties.getUserRegistrationDeptExcludeList();
			for (SunbirdApiRespContent content : resultResp.getContent()) {
				if (!excludeList.isEmpty() && !excludeList.contains(content.getIdentifier())) {
					orgList.add(new DeptPublicInfo(content.getIdentifier(), content.getChannel()));
					orgNameList.add(content.getChannel());
				}
			}

			List<String> masterOrgList = getMasterOrgList();
			for (String orgName : masterOrgList) {
				if (!orgNameList.contains(orgName)) {
					orgList.add(new DeptPublicInfo(serverProperties.getCustodianOrgId(), orgName));
				}
			}
		} while (count != iterateCount);

		if (CollectionUtils.isEmpty(orgList)) {
			throw new Exception("Failed to retrieve organisation details.");
		}

		Map<String, List<DeptPublicInfo>> deptListMap = new HashMap<String, List<DeptPublicInfo>>();
		deptListMap.put(Constants.DEPARTMENT_LIST_CACHE_NAME, orgList);
		redisCacheMgr.putCache(Constants.DEPARTMENT_LIST_CACHE_NAME, deptListMap);
		return orgList;
	}

	private Map<String, Object> getOrgCreateRequest(UserRegistration userReg) {
		Map<String, Object> orgRequestBody = new HashMap<String, Object>();
		Map<String, Object> orgRequest = new HashMap<String, Object>();
		orgRequest.put(Constants.ORG_NAME, userReg.getOrgName());
		orgRequest.put(Constants.CHANNEL, userReg.getChannel());
		orgRequest.put(Constants.ORGANIZATION_TYPE, userReg.getOrganisationType());
		orgRequest.put(Constants.ORGANIZATION_SUB_TYPE, userReg.getOrganisationSubType());
		orgRequest.put(Constants.MAP_ID, userReg.getMapId());
		orgRequest.put(Constants.IS_TENANT, true);
		orgRequest.put(Constants.SB_ROOT_ORG_ID, userReg.getSbRootOrgId());
		orgRequestBody.put(Constants.REQUEST, orgRequest);
		return orgRequestBody;
	}

	private void updateValues(UserRegistration userReg, UserRegistrationInfo userRegInfo) {
		userReg.setOrgName(userRegInfo.getOrgName());
		userReg.setChannel(userRegInfo.getOrgName());
		userReg.setOrganisationType(userRegInfo.getOrganisationType());
		userReg.setOrganisationSubType(userRegInfo.getOrganisationSubType());
		userReg.setSbRootOrgId(userRegInfo.getSbRootOrgId());
		userReg.setSbOrgId(userRegInfo.getSbOrgId());
	}

	private Boolean isApprovedDomains(String emailDomain, String domainType) {
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.CONTEXT_TYPE, domainType);
		propertyMap.put(Constants.CONTEXT_NAME, emailDomain);
		List<Map<String, Object>> listOfDomains = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
				Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, propertyMap, Arrays.asList(Constants.CONTEXT_TYPE, Constants.CONTEXT_NAME));
		return CollectionUtils.isNotEmpty(listOfDomains);
	}
}
