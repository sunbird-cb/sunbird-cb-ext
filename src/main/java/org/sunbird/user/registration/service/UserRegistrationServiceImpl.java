package org.sunbird.user.registration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiRespContent;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.model.SunbirdApiResultResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.producer.Producer;
import org.sunbird.portal.department.model.DeptPublicInfo;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.UserRegistrationInfo;
import org.sunbird.user.registration.util.UserRegistrationStatus;
import org.sunbird.user.service.UserUtilityService;

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

	@Override
	public SBApiResponse registerUser(UserRegistrationInfo userRegInfo) {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_REGISTER_API);
		String errMsg = validateRegisterationPayload(userRegInfo);
		if (StringUtils.isBlank(errMsg)) {
			try {
				if (isUserExist(userRegInfo.getEmail().toLowerCase())) {
					errMsg = Constants.EMAIL_EXIST_ERROR;
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
			List<DeptPublicInfo> orgList = new ArrayList<>();
			int count = 0;
			int iterateCount = 0;
			do {
				// request body
				Map<String, Object> requestMap = new HashMap<>();
				requestMap.put(Constants.OFFSET, iterateCount);
				requestMap.put(Constants.LIMIT, 1000);
				requestMap.put(Constants.FIELDS,
						new ArrayList<>(Arrays.asList(Constants.CHANNEL, Constants.IDENTIFIER)));
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
					}
				}
			} while (count != iterateCount);

			response.getResult().put("count", orgList.size());
			response.getResult().put("content", orgList);

		} catch (Exception e) {
			LOGGER.error("Exception occurred in getDeptDetails", e);
			response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.getParams().setErrmsg("Exception occurred in getDeptDetails. Exception: " + e.getMessage());
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
			if (userUtilityService.createUser(getUserRegistrationForRegCode(registrationCode))) {
				LOGGER.info("Successfully completed user creation flow.");
			} else {
				LOGGER.error("Failed to create user for Reg.Code :: " + registrationCode);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to process user create flow.", e);
		}
	}

	private SBApiResponse createDefaultResponse(String api) {
		SBApiResponse response = new SBApiResponse();
		response.setId(api);
		response.setVer(Constants.API_VERSION_1);
		response.setParams(new SunbirdApiRespParam());
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
		if (StringUtils.isBlank(userRegInfo.getLastName())) {
			errList.add("LastName");
		}
		if (StringUtils.isBlank(userRegInfo.getEmail())) {
			errList.add("Email");
		}
		if (StringUtils.isBlank(userRegInfo.getDeptId())) {
			errList.add("DeptId");
		}
		if (StringUtils.isBlank(userRegInfo.getDeptName())) {
			errList.add("Department");
		}
		if (StringUtils.isBlank(userRegInfo.getPosition())) {
			errList.add("Position");
		}
		if (StringUtils.isBlank(userRegInfo.getSource())) {
			errList.add("Source");
		}
		if (!errList.isEmpty()) {
			str.append("Failed to Register User Details. Missing Params - [").append(errList.toString()).append("]");
		}
		// email Validation
		if (StringUtils.isNotBlank(userRegInfo.getEmail()) && !emailValidation(userRegInfo.getEmail())) {
			str.setLength(0);
			str.append("Invalid email id");
		}
		return str.toString();

	}

	private UserRegistration getUserRegistrationDocument(Map<String, Object> mustMatch) throws Exception {
		SearchResponse searchResponse = indexerService.getEsResult(serverProperties.getUserRegistrationIndex(),
				serverProperties.getEsProfileIndexType(), queryBuilder(mustMatch));

		if (searchResponse.getHits().getTotalHits() > 0) {
			SearchHit hit = searchResponse.getHits().getAt(0);
			return mapper.convertValue(hit.getSourceAsMap(), UserRegistration.class);
		}

		return null;
	}

	private UserRegistration getRegistrationObject(UserRegistrationInfo userRegInfo) {
		UserRegistration userRegistration = new UserRegistration();
		userRegistration.setFirstName(userRegInfo.getFirstName());
		userRegistration.setLastName(userRegInfo.getLastName());
		userRegistration.setEmail(userRegInfo.getEmail());
		userRegistration.setDeptId(userRegInfo.getDeptId());
		userRegistration.setDeptName(userRegInfo.getDeptName());
		userRegistration.setPosition(userRegInfo.getPosition());
		userRegistration.setSource(userRegInfo.getSource());

		if (StringUtils.isBlank(userRegInfo.getRegistrationCode())) {
			userRegistration.setRegistrationCode(serverProperties.getUserRegCodePrefix() + "-"
					+ userRegInfo.getDeptName() + "-" + RandomStringUtils.random(8, Boolean.TRUE, Boolean.TRUE));
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

	private boolean isUserExist(String email) {
		// request body
		SunbirdApiRequest requestObj = new SunbirdApiRequest();
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(Constants.FILTERS, new HashMap<String, Object>() {
			{
				put(Constants.EMAIL, email);
			}
		});
		requestObj.setRequest(reqMap);

		HashMap<String, String> headersValue = new HashMap<>();
		headersValue.put(Constants.CONTENT_TYPE, "application/json");
		headersValue.put(Constants.AUTHORIZATION, serverProperties.getSbApiKey());

		try {
			String url = serverProperties.getSbUrl() + serverProperties.getUserSearchEndPoint();

			Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(url, requestObj,
					headersValue);
			if (response != null && "OK".equalsIgnoreCase((String) response.get("responseCode"))) {
				Map<String, Object> map = (Map<String, Object>) response.get("result");
				if (map.get("response") != null) {
					Map<String, Object> responseObj = (Map<String, Object>) map.get("response");
					int count = (int) responseObj.get(Constants.COUNT);
					if (count == 0)
						return false;
					else
						return true;
				}
			}
		} catch (Exception e) {
			throw new ApplicationLogicError("Sunbird Service ERROR: ", e);
		}
		return true;
	}

	/**
	 * Check the email id is valid or not
	 * 
	 * @param email String
	 * @return Boolean
	 */
	public Boolean emailValidation(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";
		Boolean retValue = Boolean.FALSE;
		Pattern pat = Pattern.compile(emailRegex);
		if (pat.matcher(email).matches()) {
			String emailDomain = email.split("@")[1];
			retValue = serverProperties.getUserRegistrationDomain().contains(emailDomain)
					|| serverProperties.getUserRegistrationPreApprovedDomainList().contains(emailDomain);
		}
		return retValue;
	}

	private Boolean isPreApprovedDomain(String email) {
		return serverProperties.getUserRegistrationPreApprovedDomainList().contains(email.split("@")[1]);
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
}
