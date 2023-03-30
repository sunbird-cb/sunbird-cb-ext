package org.sunbird.profile.service;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.sunbird.core.logger.CbExtLogger;

@Component
public class UserBulkUploadConsumer {
	private static final CbExtLogger logger = new CbExtLogger(UserBulkUploadConsumer.class.getName());

	@Autowired
	UserBulkUploadService userBulkUploadService;


	@KafkaListener(topics = "${kafka.topics.user.bulk.upload}", groupId = "${kafka.topics.user.bulk.upload.group}")
	public void processUserBulkUploadMessage(ConsumerRecord<String, String> data) {
		logger.info(
				"UserBulkUploadConsumer::processMessage: Received event to initiate User Bulk Upload Process...");
		logger.info("Received message:: " + data.value());
		String value = data.value();
		try {
			if (StringUtils.isNoneBlank(value)) {
				CompletableFuture.runAsync(() -> {
					userBulkUploadService.initiateUserBulkUploadProcess(value);
				});
				}
				else {
					logger.error("The Switch for this property is off/Invalid Kafka Msg",
							new Exception("The Switch for this property is off/Invalid Kafka Msg"));
				}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}