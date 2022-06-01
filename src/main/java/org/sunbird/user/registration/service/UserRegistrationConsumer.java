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

	@KafkaListener(id = "id1", groupId = "userRegistrationTopic-consumer", topicPartitions = {
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
			RestStatus status = indexerService.addEntity(serverProperties.getUserRegistrationIndex(),
					serverProperties.getEsProfileIndexType(), userRegistration.getId(),
					mapper.convertValue(userRegistration, Map.class));

			if (!status.equals(RestStatus.CREATED)) {
				LOGGER.info("Failed to update registration status for the document id : "
						+ userRegistration.getRegistrationCode() + "and status : " + userRegistration.getStatus());
			}
		} else {
			userRegistration.setStatus(UserRegistrationStatus.FAILED.name());
		}
		// send notification
		sendNotification(userRegistration);
	}

	private WfRequest wfRequestObj(UserRegistration userRegistration) {
		WfRequest wfRequest = new WfRequest();
		wfRequest.setState(Constants.INITIATE);
		wfRequest.setAction(Constants.INITIATE);
		String uuid = UUID.randomUUID().toString();
		wfRequest.setUserId(uuid);
		wfRequest.setActorUserId(uuid);
		wfRequest.setApplicationId(userRegistration.getId());
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
				put(Constants.SUBJECT, "iGOT - Registration");
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
		template.put(Constants.ID, "user-registration");
		Map<String, Object> params = new HashMap<>();
		template.put("params", params);
		switch (status) {
		case "WF_INITIATED":
			params.put(Constants.TITLE, "Thankyou for registering in iGOT!");
			params.put(Constants.STATUS, "Your request is initiated.");
			params.put(Constants.DESCRIPTION, "Please use the code " + regCode + " for further process.");
			break;
		case "WF_APPROVED":
			params.put(Constants.TITLE, "iGOT registration approved");
			params.put(Constants.STATUS, "Your request is approved and your account is active now.");
			params.put(Constants.DESCRIPTION, "Click on the below link to set your password and explore iGOT.");
			params.put("btn-url", "https://igot-dev.in");
			params.put("btn-name", "Click here");
			break;
		case "WF_DENIED":
			params.put(Constants.TITLE, "iGOT registration denied");
			params.put(Constants.STATUS, "Your request is denied");
			break;
		case "FAILED":
			params.put(Constants.TITLE, "iGOT registration failed");
			params.put(Constants.STATUS, "Your registration request is failed. Please try again later.");
			break;

		default:
			template = null;
			break;
		}
		return template;
	}

}
