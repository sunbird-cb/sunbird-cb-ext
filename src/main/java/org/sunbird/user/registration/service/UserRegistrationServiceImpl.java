package org.sunbird.user.registration.service;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.common.util.Constants;
import org.sunbird.user.registration.model.UserRegistrationInfo;

public class UserRegistrationServiceImpl implements UserRegistrationService {

	public SBApiResponse registerUser(UserRegistrationInfo userRegInfo) {
		// This method should perform the following
		// 1. verify the incoming data is proper
		// 2. verify the incoming email is valid domain
		// 3. verify the given email is exist in ES Server
		// 4. create the doc in ES and fire Kafka topic event
		// 5. return 200 OK response once the ES entry created successfully.

		SBApiResponse response = createDefaultResponse(Constants.USER_REGISTRATION_REGISTER_API);
		response.setResponseCode(HttpStatus.ACCEPTED);
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
}
