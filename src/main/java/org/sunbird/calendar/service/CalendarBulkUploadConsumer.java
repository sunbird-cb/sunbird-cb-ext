package org.sunbird.calendar.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CalendarBulkUploadConsumer {

    private Logger logger = LoggerFactory.getLogger(CalendarBulkUploadConsumer.class);
    @Autowired
    CalendarBulkUploadService calendarBulkUploadService;


    @KafkaListener(topics = "${kafka.topics.calendar.bulk.upload.event}", groupId = "${kafka.topics.calendar.bulk.upload.event.group}")
    public void processCalendarEventBulkUploadMessage(ConsumerRecord<String, String> data) {
        logger.info(
                "CalendarEventBulkUploadMessag::processMessage: Received event to initiate Calendar Bulk Upload Process...");
        logger.info("Received message:: " + data.value());
        try {
            if (StringUtils.isNoneBlank(data.value())) {
                CompletableFuture.runAsync(() -> {
                    calendarBulkUploadService.initiateCalendarEventBulkUploadProcess(data.value());
                });
            } else {
                logger.error("Error in Calendar Bulk Upload Consumer: Invalid Kafka Msg");
            }
        } catch (Exception e) {
            logger.error(String.format("Error in Calendar Bulk Upload Consumer: Error Msg :%s", e.getMessage()), e);
        }
    }
}
