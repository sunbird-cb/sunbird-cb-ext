package org.sunbird.portal.department.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.portal.department.dto.UserDepartmentRoleAudit;
import org.sunbird.portal.department.repo.UserDepartmentRoleAuditRepo;

import java.io.IOException;
import java.util.Date;

@Service
public class UserDepartmentRoleAuditProcessing {

    @Autowired
    private UserDepartmentRoleAuditRepo userDepartmentRoleAuditRepo;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @KafkaListener(id = "id1", groupId = "userRoleAuditTopic-consumer", topicPartitions = {
            @TopicPartition(topic = "${kafka.topics.userrole.audit}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {
        try {
            logger.info("Consuming the audit records .....");
            ObjectMapper mapper = new ObjectMapper();
            UserDepartmentRoleAudit auditObject = mapper.readValue(String.valueOf(data.value()), UserDepartmentRoleAudit.class);
            auditObject.setCreatedTime(new Date().getTime());
            userDepartmentRoleAuditRepo.save(auditObject);
        } catch (IOException e) {
            logger.error(e);
        }
    }

}
