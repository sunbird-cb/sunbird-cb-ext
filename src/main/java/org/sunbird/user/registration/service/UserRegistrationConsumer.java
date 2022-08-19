package org.sunbird.user.registration.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.common.util.NotificationUtil;
import org.sunbird.user.registration.model.UserRegistration;
import org.sunbird.user.registration.model.WfRequest;
import org.sunbird.user.registration.util.UserRegistrationStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Component
public class UserRegistrationConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationConsumer.class);

	Gson gson = new Gson();

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	OutboundRequestHandlerServiceImpl outboundHandlerService;

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	NotificationUtil notificationUtil;

	@Autowired
	IndexerService indexerService;

	@Autowired
	UserRegistrationService userRegService;

	@Autowired
	UserRegistrationNotificationService userRegNotificationService;

	@SuppressWarnings("unchecked")
	@KafkaListener(topics = "${kafka.topics.user.registration.register.event}",groupId = "${kafka.topics.user.registration.register.event.consumer.group}" )
	public void processMessage(ConsumerRecord<String, String> data) {
		UserRegistration userRegistration = gson.fromJson(data.value(), UserRegistration.class);
		/*
		 * 1. This event should create workflow transition request for user registration
		 * flow
		 */
		WfRequest wfRequest = wfRequestObj(userRegistration);
		Map<String, Object> wfTransitionData = workflowTransition(wfRequest);
		/*
		 * 2. Upon successful workflow transition request this event should generate an
		 * email to user. Email should mention User Registration is accepted and
		 * regisrationCode should be mentioned for subsequent communication
		 *
		 * 3. Failure of creating transition request -- should trigger email
		 * notification to user mentioning the registration request failed and try again
		 * later. And also update in the ES doc status value.
		 * 
		 * 4. TODO - In case of transition request created but failed to send email
		 * notification.
		 */
		if (wfTransitionData != null) {
			List<String> wfIds = (List<String>) wfTransitionData.get("wfIds");
			userRegistration.setStatus((String) wfTransitionData.get(Constants.STATUS));
			userRegistration.setWfId(wfIds.get(0));
		} else {
			userRegistration.setStatus(UserRegistrationStatus.FAILED.name());
		}
		RestStatus status = indexerService.updateEntity(serverProperties.getUserRegistrationIndex(),
				serverProperties.getEsProfileIndexType(), userRegistration.getRegistrationCode(),
				mapper.convertValue(userRegistration, Map.class));

		LOGGER.info("Successfully called ES update request. REST Status ? " + (status != null ? status.name() : null));

		if (!RestStatus.OK.equals(status)) {
			LOGGER.info("Failed to update registration status for the document id : "
					+ userRegistration.getRegistrationCode() + "and status : " + userRegistration.getStatus());
		}
		// send notification
		userRegNotificationService.sendNotification(userRegistration);
	}

	@KafkaListener(topics = "${kafka.topics.user.registration.createUser}",groupId = "${kafka.topics.user.registration.createUser.consumer.group}")
	public void processCreateUserMessage(ConsumerRecord<String, String> data) {
		try {
			WfRequest wfRequest = gson.fromJson(data.value(), WfRequest.class);
			LOGGER.info("Consumed Request in Topic to create user in registration:: "
					+ mapper.writeValueAsString(wfRequest));
			userRegService.initiateCreateUserFlow(wfRequest.getApplicationId());
		} catch (Exception e) {
			LOGGER.error("Failed to process message in Topic to create user in registration.", e);
		}
	}

	@KafkaListener(topics = "${kafka.topics.user.registration.auto.createUser}",groupId = "${kafka.topics.user.registration.auto.createUser.consumer.group}")
	public void processAutoCreateUserEvent(ConsumerRecord<String, String> data) {
		try {
			UserRegistration userRegistration = gson.fromJson(data.value(), UserRegistration.class);
			LOGGER.info("Consumed Request in Topic to auto create user in registration:: "
					+ mapper.writeValueAsString(userRegistration));
			userRegService.initiateCreateUserFlow(userRegistration.getRegistrationCode());
		} catch (Exception e) {
			LOGGER.error("Failed to process message in Topic to auto create user in registration.", e);
		}
	}

	private WfRequest wfRequestObj(UserRegistration userRegistration) {
		WfRequest wfRequest = new WfRequest();
		wfRequest.setState(Constants.INITIATE);
		wfRequest.setAction(Constants.INITIATE);
		String uuid = UUID.randomUUID().toString();
		wfRequest.setUserId(uuid);
		wfRequest.setActorUserId(uuid);
		wfRequest.setApplicationId(userRegistration.getRegistrationCode());
		wfRequest.setDeptName(userRegistration.getOrgName());
		wfRequest.setServiceName(serverProperties.getUserRegistrationWorkFlowServiceName());
		wfRequest.setUpdateFieldValues(Arrays.asList(new HashMap<>()));
		return wfRequest;
	}

	private Map<String, Object> workflowTransition(WfRequest wfRequest) {
		try {
			Map<String, String> headerValues = new HashMap<>();
			headerValues.put(Constants.ROOT_ORG_CONSTANT, Constants.IGOT);
			headerValues.put(Constants.ORG_CONSTANT, Constants.DOPT);

			Map<String, Object> responseObject = outboundHandlerService.fetchResultUsingPost(
					serverProperties.getWfServiceHost() + serverProperties.getWfServiceTransitionPath(), wfRequest,
					headerValues);
			Map<String, Object> resultValue = (Map<String, Object>) responseObject.get(Constants.RESULT);
			if (resultValue.get(Constants.STATUS).equals(Constants.OK)) {
				return (Map<String, Object>) resultValue.get("data");
			}

		} catch (Exception e) {
			LOGGER.error(String.format("Exception in %s : %s", "workflowTransition", e.getMessage()));
		}
		return null;
	}
}
