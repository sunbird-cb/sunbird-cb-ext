package org.sunbird.course.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.sunbird.core.logger.CbExtLogger;

@Component
public class CourseAlertNotificationConsumer {

	private static final CbExtLogger logger = new CbExtLogger(CourseAlertNotificationConsumer.class.getName());

	@Autowired
	CourseReminderNotificationService notifyService;

	@KafkaListener(topics = "${kafka.topics.course.reminder.notification.event}", groupId = "${kafka.topics.course.reminder.notification.event.consumer.group}")
	public void processCourseReminderMessage(ConsumerRecord<String, String> data) {
		logger.info(
				"CourseAlertNotificationConsumer::processMessage: Received event to initiate courser reminder email...");
		logger.info("Received message:: " + data.value());
		notifyService.initiateCourseReminderEmail();
	}
}
