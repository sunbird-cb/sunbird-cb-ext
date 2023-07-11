package org.sunbird.assessment.service;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class AssessmentAsyncSubmitConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentAsyncSubmitConsumer.class);

    @Autowired
	AssessmentServiceV4 assessmentServiceV4;

    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "${kafka.topics.user.assessment.async.submit.handler}", groupId = "${kafka.topics.user.assessment.async.submit.handler.group}")
    public void processMessage(ConsumerRecord<String, String> data) {
        LOGGER.info("AssessmentAsyncSubmitConsumer::processMessage.. started.");
        Gson gson = new Gson();
        Map<String, Object> asyncRequest = gson.fromJson(data.value(), Map.class);
        assessmentServiceV4.handleAssessmentSubmitRequest(asyncRequest);
    }
}
