package org.sunbird.course.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;

@Component
public class CourseAlertNotificationConsumer {
	private static final CbExtLogger logger = new CbExtLogger(CourseAlertNotificationConsumer.class.getName());

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	CourseReminderNotificationService incompleteCourseService;

	@Autowired
	LatestCoursesAlertNotificationService latestCourseService;

	@KafkaListener(topics = "${kafka.topics.course.reminder.notification.event}", groupId = "${kafka.topics.course.reminder.notification.event.consumer.group}")
	public void processCourseReminderMessage(ConsumerRecord<String, String> data) {
		logger.info(
				"CourseAlertNotificationConsumer::processMessage: Received event to initiate courser reminder email...");
		logger.info("Received message:: " + data.value());
		String value = data.value();

		if (StringUtils.isNoneBlank(value)) {
			if (value.equalsIgnoreCase(serverProperties.getIncompleteCourseAlertEmailKey()) && serverProperties.getSendIncompleteCoursesAlert()) {
				incompleteCourseService.initiateCourseReminderEmail();
			} else if (value.equalsIgnoreCase(serverProperties.getLatestCourseAlertEmailKey()) && serverProperties.getSendLatestCoursesAlert()) {
				latestCourseService.initiateLatestCourseAlertEmail();
			} else {
				logger.error("The Email Switch for this property is off/Invalid Kafka Msg",
						new Exception("The Email Switch for this property is off/Invalid Kafka Msg"));
			}
		}
	}
}