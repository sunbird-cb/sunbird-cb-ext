package org.sunbird.user.registration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiRespContent;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.model.SunbirdApiResultResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.core.producer.Producer;
import org.sunbird.portal.department.model.DeptPublicInfo;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.UserRegistrationInfo;
import org.sunbird.user.registration.util.UserRegistrationStatus;
import org.sunbird.user.registration.util.Utility;

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

	@Override
	public SBApiResponse registerUser(UserRegistrationInfo userRegInfo) {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_REGISTER_API);
		String payloadvalidation = validateRegisterationPayload(userRegInfo);
		if (StringUtils.isBlank(payloadvalidation)) {
			try {
				// verify the given email exist in ES Server
				UserRegistration regDocument = getUserRegistrationDocument(new HashMap<String, Object>() {
					{
						put(Constants.EMAIL, userRegInfo.getEmail());
					}
				});
				if (regDocument == null
						|| UserRegistrationStatus.FAILED.name().equalsIgnoreCase(regDocument.getStatus())) {
					// create the doc in ES
					UserRegistration userRegistration = getRegistrationObject(userRegInfo);
					if (StringUtils.isBlank(userRegistration.getId())) {
						userRegistration.setId(regDocument.getId());
					}
					RestStatus status = indexerService.addEntity(serverProperties.getUserRegistrationIndex(),
							serverProperties.getEsProfileIndexType(), userRegistration.getId(),
							mapper.convertValue(userRegistration, Map.class));
					if (status.equals(RestStatus.CREATED)) {
						// fire Kafka topic event
						kafkaProducer.push(serverProperties.getUserRegistrationTopic(), userRegistration);
						response.setResponseCode(HttpStatus.ACCEPTED);
						response.getResult().put(Constants.RESULT, userRegistration);
					}
				} else {
					payloadvalidation = "Email id already exists";
				}

			} catch (Exception e) {
				LOGGER.error(String.format("Exception in %s : %s", "registerUser", e.getMessage()));
			}
		}
		if (StringUtils.isNotBlank(payloadvalidation)) {
			response.getParams().setStatus(Constants.FAILED);
			response.getParams().setErrmsg(payloadvalidation);
			response.setResponseCode(HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Override
	public SBApiResponse getUserRegistrationDetails(String registrationCode) {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_RETRIEVE_API);
		UserRegistration userRegistration = null;
		try {
			userRegistration = getUserRegistrationDocument(new HashMap<String, Object>() {
				{
					put("registrationCode", registrationCode);
				}
			});

		} catch (Exception e) {
			LOGGER.error(String.format("Exception in %s : %s", "getUserRegistrationDetails", e.getMessage()));
		}

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
				requestMap.put(Constants.LIMIT, 100);
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
					if (!excludeList.isEmpty() && excludeList.contains(content.getIdentifier())) {
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
		if (!Utility.emailValidation(userRegInfo.getEmail())) {
			str.setLength(0);
			str.append("Invalid email id");
		}
		return str.toString();

	}

	private UserRegistration getUserRegistrationDocument(Map<String, Object> mustMatch) throws Exception {
		SearchSourceBuilder searchSourceBuilder = queryBuilder(mustMatch);
		SearchResponse searchResponse = indexerService.getEsResult(serverProperties.getUserRegistrationIndex(),
				serverProperties.getEsProfileIndexType(), searchSourceBuilder);

		if (searchResponse.getHits().totalHits > 0) {
			// Record already exists. will return the existing one.
			SearchHit[] searchHit = searchResponse.getHits().getHits();
			return mapper.convertValue(searchHit[0], UserRegistration.class);
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
					+ userRegInfo.getDeptName() + "-" + RandomStringUtils.random(6, Boolean.TRUE, Boolean.TRUE));
			userRegistration.setId(UUID.randomUUID().toString());
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
		SearchSourceBuilder searchBuilder = new SearchSourceBuilder().query(boolBuilder);
		for (Map.Entry<String, Object> entry : mustMatch.entrySet()) {
			boolBuilder.must(QueryBuilders.matchQuery(entry.getKey() + ".keyword", entry.getValue()));
		}
		return searchBuilder;
	}
}
