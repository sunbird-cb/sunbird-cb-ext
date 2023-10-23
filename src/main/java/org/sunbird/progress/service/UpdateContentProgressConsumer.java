package org.sunbird.progress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.util.internal.StringUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jboss.resteasy.spi.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.Config;
import org.sunbird.common.model.NotificationRequest;
import org.sunbird.common.model.Template;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.config.PropertiesConfig;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.progress.model.UpdateContentProgressRequest;

import java.util.*;

@Component
public class UpdateContentProgressConsumer {

    private static final String EMAILTEMPLATE = "emailtemplate";
    private static final String MAIL_BODY = "Your attendance for the #session_name in the #blended_program_name Program, held on #session_date, from #session_start_time to #session_end_time has been marked #attendance by the Program Coordinator.";
    private static final String BLENDED_PROGRAM_TAG = "#blended_program_name";
    private static final String SESSION_TAG = "#session_name";
    private static final String SESSION_DATE_TAG = "#session_date";
    private static final String SESSION_START_DATE = "#session_start_time";
    private static final String SESSOION_END_DATE = "#session_end_time";
    private static final String ATTENDANCE = "#attendance";
    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundReqService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    PropertiesConfig configuration;

    @Autowired
    private RestTemplate restTemplate;

    @KafkaListener(topics = "${kafka.topic.update.content.progress}", groupId = "${kafka.topic.update.content.progress.group}")
    public void updateContentProgressConsumer(ConsumerRecord<String, String> data) {
        try {
            HashMap<String, Object> req;
            UpdateContentProgressRequest contentProgressRequest = mapper.readValue(data.value(), UpdateContentProgressRequest.class);
            logger.info("Received message:: " + contentProgressRequest);
            List<Object> requestList = (List<Object>) contentProgressRequest.getRequestBody().getRequest();
            Map<String,Object> firstEntry = ((HashMap<String, Object>)requestList.get(0));
            Map<String, Object> content = (Map<String, Object>) ((List<Object>) firstEntry.get("contents")).get(0);
            String courseId = (String)content.get("courseId");
            String courseName = null;
            if(!StringUtil.isNullOrEmpty(courseId)){
                Map<String, Object> propertiesMap = new HashMap<>();
                propertiesMap.put(Constants.COURSE_ID, courseId);
                List<Map<String, Object>> result = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_COURSES_KEY_SPACE_NAME,
                        Constants.TABLE_COURSE_BATCH, propertiesMap, Arrays.asList(Constants.COURSE_NAME));
                courseName = (String) result.get(0).get(Constants.COURSE_NAME);
            }
            for(Object request: requestList) {
                req = new HashMap<>();
                req.put("request", request);
                Map<String, Object> response = outboundReqService.fetchResultUsingPatch(
                        cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getProgressUpdateEndPoint(),
                        req, contentProgressRequest.getHeadersValues());
                if (response.get("responseCode").equals("OK")) {
                    logger.info("Content progress is updated for resource::" + request);
                    sendNotificationToLearner(request, courseName);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void sendNotificationToLearner(Object request, String courseName) {
        Map<String, Object> learnerInfo = (HashMap<String, Object>)request;
        Map<String, Object> content = (Map<String, Object>) ((List<Object>) learnerInfo.get("contents")).get(0);
        int status = (int) content.get("status");
        String userId = (String) learnerInfo.get(Constants.USER_ID);
        Set<String> userIdSet = new HashSet<>();
        userIdSet.add(userId);
        HashMap<String, Object> usersObj = getUsersResult(userIdSet);
        Map<String, Object> recipientInfo = (Map<String, Object>)usersObj.get(userId);

        Map<String, Object> params = new HashMap<>();
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setDeliveryType("message");
        notificationRequest.setIds(Arrays.asList((String)recipientInfo.get("email")));
        notificationRequest.setMode("email");

        String attendance = "";
        if(status == 2){
            attendance = Constants.PRESENT;
        } else if(status == 0){
            attendance = "absent";
        }

        params.put("body", MAIL_BODY.replace(BLENDED_PROGRAM_TAG, courseName).replace(ATTENDANCE, attendance));
        Template template = new Template("",EMAILTEMPLATE, params);
        template.setParams(params);
        Config config = new Config();
        config.setSubject("ATTENDANCE FOR " + courseName + " PROGRAMME");
        config.setSender((String)recipientInfo.get("email"));
        Map<String, Object> req = new HashMap<>();
        notificationRequest.setTemplate(template);
        notificationRequest.setConfig(config);
        Map<String, List<NotificationRequest>> notificationMap = new HashMap<>();
        notificationMap.put("notifications", Arrays.asList(notificationRequest));
        req.put("request", notificationMap);
        sendNotification(req);
    }

    private void sendNotification(Map<String, Object> request) {
        StringBuilder builder = new StringBuilder();
        builder.append(configuration.getNotifyServiceHost()).append(configuration.getNotifyServicePath());
        try {
            fetchResultUsingPost(builder, request, Map.class, null);
        } catch (Exception e) {
            logger.error("Exception while posting the data in notification service: ", e);
        }

    }

    private Object fetchResultUsingPost(StringBuilder uri, Object request, Class objectType,HashMap<String, String> headersValue) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        StringBuilder str = new StringBuilder(this.getClass().getCanonicalName()).append(".fetchResult:")
                .append(System.lineSeparator());
        str.append("URI: ").append(uri.toString()).append(System.lineSeparator());
        try {
            str.append("Request: ").append(mapper.writeValueAsString(request)).append(System.lineSeparator());
            String message = str.toString();
            logger.info(message);
            HttpHeaders headers = new HttpHeaders();
            if (!ObjectUtils.isEmpty(headersValue)) {
                for (Map.Entry<String, String> map : headersValue.entrySet()) {
                    headers.set(map.getKey(), map.getValue());
                }
            }
            headers.set(Constants.ROOT_ORG_CONSTANT, configuration.getHubRootOrg());
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);
            response = restTemplate.postForObject(uri.toString(), entity, objectType);
        } catch (HttpClientErrorException e) {
            logger.error("External Service threw an Exception: ", e);
        } catch (Exception e) {
            logger.error("Exception occured while calling the exteranl service: ", e);
        }
        return response;
    }



    private  HashMap<String, Object> getUsersResult(Set<String> userIds) {
        HashMap<String, Object> userResult = new HashMap<>();
        Map<String, Object> request = getSearchObject(userIds);
        HashMap<String, String> headersValue = new HashMap<>();
        headersValue.put("Content-Type", "application/json");
        try {
            StringBuilder builder = new StringBuilder(configuration.getLmsServiceHost());
            builder.append(configuration.getLmsUserSearchEndPoint());
            Map<String, Object> searchProfileApiResp = (Map<String, Object>)fetchResultUsingPost(builder, request, Map.class, getHeaders());
            if (searchProfileApiResp != null
                    && "OK".equalsIgnoreCase((String) searchProfileApiResp.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
                Map<String, Object> response = (Map<String, Object>) map.get(Constants.RESPONSE);
                List<Map<String, Object>> contents = (List<Map<String, Object>>) response.get(Constants.CONTENT);
                if (!CollectionUtils.isEmpty(contents)) {
                    for (Map<String, Object> content : contents) {
                        HashMap<String, Object> profileDetails = (HashMap<String, Object>) content
                                .get(Constants.PROFILE_DETAILS);
                        if (!CollectionUtils.isEmpty(profileDetails)) {
                            HashMap<String, Object> personalDetails = (HashMap<String, Object>) profileDetails
                                    .get(Constants.PERSONAL_DETAILS);
                            Map<String, Object> record = new HashMap<>();
                            if (!CollectionUtils.isEmpty(personalDetails)) {
                                record.put(Constants.UUID, content.get(Constants.USER_ID));
                                record.put(Constants.FIRST_NAME, personalDetails.get(Constants.FIRSTNAME));
                                record.put(Constants.EMAIL, personalDetails.get(Constants.PRIMARY_EMAIL));
                            }
                            Map<String, Object> additionalProperties = (Map<String, Object>) profileDetails.get(Constants.ADDITIONAL_PROPERTIES);
                            if (!CollectionUtils.isEmpty(additionalProperties)) {
                                record.put(Constants.TAG, additionalProperties.get(Constants.TAG));
                            }
                            userResult.put((String) content.get(Constants.USER_ID), record);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception while fetching user setails : ",e);
            throw new ApplicationException("Hub Service ERROR: ", e);
        }
        return userResult;
    }


    private Map<String, Object> getSearchObject(Set<String> userIds) {
        Map<String, Object> requestObject = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", userIds);
        request.put("limit", userIds.size());
        request.put("offset", 0);
        request.put("filters", filters);
        requestObject.put("request", request);
        return requestObject;
    }

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headersValue = new HashMap<>();
        headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        return headersValue;
    }

}
