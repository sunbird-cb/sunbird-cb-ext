package org.sunbird.user.registration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

			if (status.equals(RestStatus.CREATED)) {
				// send notification
				List<String> sendTo = new ArrayList<String>() {
					{
						add(userRegistration.getEmail());
					}
				};
				Map<String, Object> params = notificationPayload();
				notificationUtil.sendNotification(sendTo, params,
						PropertiesCache.getInstance().getProperty(Constants.SENDER_MAIL),
						PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST)
								+ PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT));
			}

		}

	}

	private WfRequest wfRequestObj(UserRegistration userRegistration) {
		WfRequest wfRequest = new WfRequest();
		wfRequest.setState(userRegistration.getStatus());
		wfRequest.setAction(userRegistration.getStatus());
		wfRequest.setUserId("1234");
		wfRequest.setActorUserId("1234");
		wfRequest.setApplicationId(userRegistration.getId());
		wfRequest.setServiceName("user-registration");
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

	private Map<String, Object> notificationPayload() {
		Map<String, Object> notificationObj = new HashMap<>();
		notificationObj.put("mode", "email");
		notificationObj.put("deliveryType", "message");
		notificationObj.put("config", new HashMap<String, Object>() {
			{
				put("sender", "");
				put("subject", "");
			}
		});
		notificationObj.put("ids", new ArrayList<String>() {
			{
				add("");
			}
		});
		notificationObj.put("template", new HashMap<String, Object>() {
			{
				put("data", "");
				put("params", new HashMap<String, Object>() {
					{
						put("", "");
					}
				});
			}
		});
		return notificationObj;
	}

}
