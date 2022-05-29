package org.sunbird.user.registration.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationConsumer {

	@KafkaListener(id = "id1", groupId = "userRegistrationTopic-consumer", topicPartitions = {
			@TopicPartition(topic = "${kafka.topics.user.registration.register.event}", partitions = { "0", "1", "2",
					"3" }) })
	public void processMessage(ConsumerRecord<String, String> data) {
		/*
		 * 1. This event should create workflow transition request for user registration
		 * flow
		 * 
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
	}
}
