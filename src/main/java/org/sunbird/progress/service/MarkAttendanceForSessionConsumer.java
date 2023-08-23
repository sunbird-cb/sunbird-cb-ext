package org.sunbird.progress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.progress.model.MarkAttendanceRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MarkAttendanceForSessionConsumer {

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundReqService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    private ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.topic.mark.attendance.offline.session}", groupId = "${kafka.topic.mark.attendance.offline.session.group}")
    public void markUserAttendanceForSessionConsumer(ConsumerRecord<String, String> data) {
        try {
            HashMap<String, Object> req;
            MarkAttendanceRequest markAttendanceRequest = mapper.readValue(data.value(), MarkAttendanceRequest.class);
            logger.info("Received message:: " + markAttendanceRequest);
            List<Object> requestList = (List<Object>) markAttendanceRequest.getRequestBody().getRequest();
            for(Object request: requestList) {
                req = new HashMap<>();
                req.put("request", request);
                Map<String, Object> response = outboundReqService.fetchResultUsingPatch(
                        cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getProgressUpdateEndPoint(),
                        req, markAttendanceRequest.getHeadersValues());
                if (response.get("responseCode").equals("OK")) {
                    logger.info("Attendance marked for resource::" + request);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
