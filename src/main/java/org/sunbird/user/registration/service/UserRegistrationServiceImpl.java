package org.sunbird.user.registration.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.producer.Producer;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.UserRegistrationInfo;
import org.sunbird.user.registration.util.UserRegistrationStatus;
import org.sunbird.user.registration.util.Utility;
import org.sunbird.workallocation.service.IndexerService;

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

	public SBApiResponse registerUser(UserRegistrationInfo userRegInfo) {

		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_REGISTER_API);
		String payloadvalidation = validateRegisterationPayload(userRegInfo);
		if (StringUtils.isBlank(payloadvalidation)) {
			try {
				// verify the given email is exist in ES Server
				SearchSourceBuilder searchSourceBuilder = queryBuilder(new HashMap<String, Object>() {
					{
						put(Constants.EMAIL, userRegInfo.getEmail());
					}
				});
				long emailExist = indexerService.getDocumentCount(serverProperties.getUserRegistrationIndex(),
						searchSourceBuilder);
				if (emailExist == 0l) {
					// create the doc in ES
					UserRegistration userRegistration = getRegistrationObject(userRegInfo);
					RestStatus status = indexerService.addEntity(serverProperties.getUserRegistrationIndex(),
							serverProperties.getEsProfileIndexType(), userRegistration.getId(),
							mapper.convertValue(userRegistration, Map.class));
					if (status.equals(RestStatus.CREATED)) {
						// fire Kafka topic event
						kafkaProducer.push(serverProperties.getUserRegistrationTopic(), userRegistration);
						response.setResponseCode(HttpStatus.ACCEPTED);
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

	public SBApiResponse getUserRegistrationDetails(String registrationCode) {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_RETRIEVE_API);
		return response;
	}

	public SBApiResponse getDeptDetails() {
		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_DEPT_INFO_API);
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
		if (StringUtils.isBlank(userRegInfo.getFirstName())) {
			return "Firstname missing";
		}
		if (StringUtils.isBlank(userRegInfo.getLastName())) {
			return "Lastname missing";
		}
		if (StringUtils.isBlank(userRegInfo.getEmail())) {
			return "Email missing";
		}
		if (StringUtils.isBlank(userRegInfo.getDeptId())) {
			return "Department Id missing";
		}
		if (StringUtils.isBlank(userRegInfo.getDeptName())) {
			return "Department name missing";
		}
		if (StringUtils.isBlank(userRegInfo.getPosition())) {
			return "Position missing";
		}
		if (StringUtils.isBlank(userRegInfo.getSource())) {
			return "Source missing";
		}
		// email Validation
		if (!Utility.emailValidation(userRegInfo.getEmail())) {
			return "Invalid email id";
		}

		return StringUtils.EMPTY;
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

		userRegistration.setRegistrationCode(serverProperties.getUserRegistrationCode() + "-"
				+ userRegInfo.getDeptName() + "-" + RandomStringUtils.random(15, Boolean.TRUE, Boolean.TRUE));
		userRegistration.setId(RandomStringUtils.random(15, Boolean.TRUE, Boolean.TRUE));
		userRegistration.setStatus(UserRegistrationStatus.INITIATED.name());
		userRegistration.setCreatedOn(new Date().getTime());
		return userRegistration;
	}

	/**
	 * Elasticsearch must match bool search query builder
	 * 
	 * @param mustMatch
	 *            Map<String, Object>
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
