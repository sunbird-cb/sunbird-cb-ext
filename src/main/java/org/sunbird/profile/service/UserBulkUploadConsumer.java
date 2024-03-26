package org.sunbird.profile.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserBulkUploadConsumer {
    private Logger logger = LoggerFactory.getLogger(UserBulkUploadConsumer.class);
    @Autowired
    UserBulkUploadService userBulkUploadService;


    @KafkaListener(topics = "${kafka.topics.user.bulk.upload}", groupId = "${kafka.topics.user.bulk.upload.group}")
    public void processUserBulkUploadMessage(ConsumerRecord<String, String> data) {
        logger.info(
                "UserBulkUploadConsumer::processMessage: Received event to initiate User Bulk Upload Process...");
        logger.info("Received message:: {}" , data.value());
        try {
            if (StringUtils.isNoneBlank(data.value())) {
                CompletableFuture.runAsync(() -> {
                    userBulkUploadService.initiateUserBulkUploadProcess(data.value());
                });
            } else {
                logger.error("Error in User Bulk Upload Consumer: Invalid Kafka Msg");
            }
        } catch (Exception e) {
            logger.error(String.format("Error in User Bulk Upload Consumer: Error Msg :%s", e.getMessage()), e);
        }
    }
}