package org.sunbird.assessment.service;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AssessmentAsyncSubmitConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentAsyncSubmitConsumer.class);

    ObjectMapper mapper;

    AssessmentServiceV4 assessmentServiceV4;

    @Autowired
    public AssessmentAsyncSubmitConsumer(AssessmentServiceV4 assessmentServiceV4, ObjectMapper mapper) {
        this.assessmentServiceV4 = assessmentServiceV4;
        this.mapper = mapper;
    }
    @KafkaListener(topics = "${kafka.topics.user.assessment.async.submit.handler}", groupId = "${kafka.topics.user.assessment.async.submit.handler.group}")
    public void processMessage(ConsumerRecord<String, String> data) {
            LOGGER.info("AssessmentAsyncSubmitConsumer::processMessage.. started.");
        try {
            Map<String, Object> asyncRequest = mapper.readValue(data.value(), new TypeReference<Map<String, Object>>() {
            });
            assessmentServiceV4.handleAssessmentSubmitRequest(asyncRequest,false,null);
        } catch (Exception e) {
            String errMsg = String.format("Error: %s", e.getMessage());
            LOGGER.error(errMsg, e);
        }
    }
}
