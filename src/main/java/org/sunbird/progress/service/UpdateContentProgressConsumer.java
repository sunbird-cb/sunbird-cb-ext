package org.sunbird.progress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.progress.model.UpdateContentProgressRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpdateContentProgressConsumer {

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundReqService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    private ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.topic.update.content.progress}", groupId = "${kafka.topic.update.content.progress.group}")
    public void updateContentProgressConsumer(ConsumerRecord<String, String> data) {
        try {
            HashMap<String, Object> req;
            UpdateContentProgressRequest contentProgressRequest = mapper.readValue(data.value(), UpdateContentProgressRequest.class);
            logger.info("Received message:: " + contentProgressRequest);
            List<Object> requestList = (List<Object>) contentProgressRequest.getRequestBody().getRequest();
            for(Object request: requestList) {
                req = new HashMap<>();
                req.put("request", request);
                Map<String, Object> response = outboundReqService.fetchResultUsingPatch(
                        cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getProgressUpdateEndPoint(),
                        req, contentProgressRequest.getHeadersValues());
                if (response.get("responseCode").equals("OK")) {
                    logger.info("Content progress is updated for resource::" + request);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
