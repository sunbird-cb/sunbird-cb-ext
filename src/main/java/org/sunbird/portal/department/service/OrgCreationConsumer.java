package org.sunbird.portal.department.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.sunbird.core.logger.CbExtLogger;

import java.io.IOException;
import java.util.Map;

@Service
public class OrgCreationConsumer {

    @Autowired
    OrgCreationProcessService processService;

    private CbExtLogger log = new CbExtLogger(getClass().getName());

    @KafkaListener(id = "id0", groupId = "orgCreationTopic-consumer", topicPartitions = {
            @TopicPartition(topic = "${kafka.topics.org.creation}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> orgObject = mapper.readValue(String.valueOf(data.value()), Map.class);
            processService.createOrg(orgObject);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
