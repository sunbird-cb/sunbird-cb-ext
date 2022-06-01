package org.sunbird.user.registration.service;

import java.util.ArrayList;
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
import org.sunbird.common.util.PropertiesCache;
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

	@SuppressWarnings("unchecked")
	@KafkaListener(topicPartitions = {
			@TopicPartition(topic = "${kafka.topics.user.registration.register.event}", partitions = { "0", "1", "2",
					"3" }) })
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
		sendNotification(userRegistration);
	}

	@KafkaListener(topicPartitions = {
			@TopicPartition(topic = "${kafka.topics.user.registration.createUser}", partitions = { "0", "1", "2",
					"3" }) })
	public void processCreateUserMessage(ConsumerRecord<String, String> data) {
		try {
			WfRequest wfRequest = gson.fromJson(data.value(), WfRequest.class);
			LOGGER.info("Consumed Request in Topic to create user in registration:: "
					+ mapper.writeValueAsString(wfRequest));
		} catch (Exception e) {
			LOGGER.error("Failed to process message in Topic to create user in registration.", e);
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
		wfRequest.setDeptName(userRegistration.getDeptName());
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

	private void sendNotification(UserRegistration userRegistration) {
		List<String> sendTo = new ArrayList<String>() {
			{
				add(userRegistration.getEmail());
			}
		};

		Map<String, Object> notificationObj = new HashMap<>();
		notificationObj.put("mode", Constants.EMAIL);
		notificationObj.put("deliveryType", Constants.MESSAGE);
		notificationObj.put("config", new HashMap<String, Object>() {
			{
				put(Constants.SUBJECT, serverProperties.getUserRegistrationSubject());
			}
		});
		notificationObj.put("ids", sendTo);
		notificationObj.put(Constants.TEMPLATE,
				notificationMessage(userRegistration.getStatus(), userRegistration.getRegistrationCode()));

		if (notificationObj.get(Constants.TEMPLATE) != null) {
			notificationUtil.sendNotification(sendTo, notificationObj,
					PropertiesCache.getInstance().getProperty(Constants.SENDER_MAIL),
					PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST)
							+ PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT));
		}
	}

	private Map<String, Object> notificationMessage(String status, String regCode) {
		Map<String, Object> template = new HashMap<>();
		template.put(Constants.ID, Constants.USER_REGISTERATION_TEMPLATE);
		Map<String, Object> params = new HashMap<>();
		params.put(Constants.STATUS, serverProperties.getUserRegistrationStatus().replace("{status}", status));
		params.put(Constants.TITLE, serverProperties.getUserRegistrationTitle().replace("{status}", status));
		template.put("params", params);
		switch (status) {
		case "WF_INITIATED":
			params.put(Constants.TITLE, serverProperties.getUserRegistrationThankyouMessage());
			params.put(Constants.DESCRIPTION, serverProperties.getUserRegistrationInitiatedMessage()
					.replace("{regCode}", "<b>" + regCode + "</b>"));
			break;
		case "WF_APPROVED":
			params.put(Constants.DESCRIPTION, serverProperties.getUserRegistrationApprovedMessage());
			params.put("btn-url", serverProperties.getUserRegistrationDomainName());
			params.put("btn-name", serverProperties.getUserRegisterationButtonName());
			break;
		case "WF_DENIED":
			break;
		case "FAILED":
			params.put(Constants.STATUS, serverProperties.getUserRegistrationFailedMessage());
			break;

		default:
			template = null;
			break;
		}
		return template;
	}

}
