package org.sunbird.progress.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.util.internal.StringUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jboss.resteasy.spi.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UpdateContentProgressConsumer {

    private final CbExtLogger logger = new CbExtLogger(getClass().getName());


    private final  OutboundRequestHandlerServiceImpl outboundReqService;


    private final CbExtServerProperties cbExtServerProperties;

    private  final ObjectMapper mapper = new ObjectMapper();



    CbExtServerProperties serverProperties;


    CassandraOperation cassandraOperation;


    PropertiesConfig configuration;


    private  final RestTemplate restTemplate;
    @Autowired
    public UpdateContentProgressConsumer(OutboundRequestHandlerServiceImpl outboundReqService, CbExtServerProperties cbExtServerProperties, CbExtServerProperties serverProperties, CassandraOperation cassandraOperation, PropertiesConfig configuration, RestTemplate restTemplate) {
        this.outboundReqService = outboundReqService;
        this.cbExtServerProperties = cbExtServerProperties;
        this.serverProperties = serverProperties;
        this.cassandraOperation = cassandraOperation;
        this.configuration = configuration;
        this.restTemplate = restTemplate;
    }

    private static final String BLENDED_PROGRAM_TAG = "#blended_program_name";

    private static final String SESSION_TAG = "#session_name";

    private static final String SESSION_DATE_TAG = "#session_date";

    private static final String SESSION_START_TIME= "#session_start_time";

    private static final String SESSOION_END_TIME= "#session_end_time";

    private static final String ATTENDANCE_TAG = "#attendance";


    @KafkaListener(topics = "${kafka.topic.update.content.progress}", groupId = "${kafka.topic.update.content.progress.group}")
    public void updateContentProgressConsumer(ConsumerRecord<String, String> data) {
        try {
            HashMap<String, Object> req;
            UpdateContentProgressRequest contentProgressRequest = mapper.readValue(data.value(), UpdateContentProgressRequest.class);
            logger.info("Received message:: " + contentProgressRequest);
            List<Object> requestList = (List<Object>) contentProgressRequest.getRequestBody().getRequest();
            Map<String,Object> firstEntry = ((HashMap<String, Object>)requestList.get(0));
            Map<String, Object> content = (Map<String, Object>) ((List<Object>) firstEntry.get("contents")).get(0);
            Map<String, String> mailNotificationDetails = getMailNotificationDetails(content);
            for(Object request: requestList) {
                Map<String, Object> learnerInfo = (HashMap<String, Object>)request;
                String userId = (String) learnerInfo.get(Constants.USER_ID);
                mailNotificationDetails.put(Constants.USER_ID, userId);
                req = new HashMap<>();
                req.put(Constants.REQUEST, request);
                Map<String, Object> response = outboundReqService.fetchResultUsingPatch(
                        cbExtServerProperties.getCourseServiceHost() + cbExtServerProperties.getProgressUpdateEndPoint(),
                        req, contentProgressRequest.getHeadersValues());
                if (response.get("responseCode").equals("OK")) {
                    logger.info("Content progress is updated for resource::" + request + "Mail Notification Details for the User is::" + mailNotificationDetails);
                    sendNotificationToLearner(mailNotificationDetails);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }


    public Map<String, String> getMailNotificationDetails(Map<String, Object> content){
        int status = (int) content.get("status");
        String batchId = (String)content.get(Constants.BATCH_ID);
        String courseId = (String)content.get(Constants.COURSE_ID);
        String sessionId = (String) content.get("contentId");
        String batchName = "";
        String courseName = "";
        String sessionName = "";
        String sessionEndTime = "";
        String sessionStartDate = "";
        String sessionStartTime = "";

        if(!StringUtil.isNullOrEmpty(courseId) && !StringUtil.isNullOrEmpty(batchId)){
            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put(Constants.COURSE_ID, courseId);
            propertiesMap.put(Constants.BATCH_ID, batchId);

            List<Map<String, Object>> result = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_COURSES_KEY_SPACE_NAME,
                    Constants.TABLE_COURSE_BATCH,
                    propertiesMap,
                    Arrays.asList(Constants.NAME, Constants.TABLE_COURSE_BATCH_ATTRIBUTES));

            Map<String, Object> batchDetails = result.get(0);
            batchName = (String) result.get(0).get(Constants.NAME);
            HashMap<String,Object> batchAttributes = new Gson().fromJson((String) batchDetails.get("batch_attributes"), new TypeToken<HashMap<String, Object>>(){}.getType());
            List<Map<String, Object>> sessions = (List<Map<String, Object>>) batchAttributes.get(Constants.TABLE_COURSE_SESSION_DETAILS);
            Map<String, Object> sessionDetails = sessions.stream()
                    .filter(session -> ((String) session.get(Constants.SESSION_ID)).equalsIgnoreCase(sessionId))
                    .collect(Collectors.toList())
                    .get(0);
            sessionEndTime = (String) sessionDetails.get("endTime");
            sessionName = (String) sessionDetails.get(Constants.TITLE);
            sessionStartTime = (String) sessionDetails.get("startTime");
            sessionStartDate = (String) sessionDetails.get(Constants.START_DATE);

            propertiesMap.clear();
            propertiesMap.put(Constants.IDENTIFIER, courseId);
            List<Map<String, Object>> coursesDataList = cassandraOperation.getRecordsByProperties(configuration.getHierarchyStoreKeyspaceName(),
                    Constants.CONTENT_HIERARCHY,
                    propertiesMap,
                    Arrays.asList(Constants.IDENTIFIER, Constants.HIERARCHY));
            Map<String, Object> hierarchy = null;
            try {
                hierarchy = mapper.readValue((String) coursesDataList.get(0).get("hierarchy"), new TypeReference<HashMap<String, Object>>() {});
                courseName = (String) hierarchy.get(Constants.NAME);
            } catch (IOException e) {
                logger.error(e);
            }
        }
        Map<String, String> mailNotificationDetails = new HashMap<>();
        mailNotificationDetails.put(Constants.STATUS, status+"");
        mailNotificationDetails.put(Constants.BATCH_NAME, batchName);
        mailNotificationDetails.put(Constants.COURSE_NAME, courseName);
        mailNotificationDetails.put(Constants.SESSION_NAME, sessionName);
        mailNotificationDetails.put(Constants.END_TIME, sessionEndTime);
        mailNotificationDetails.put(Constants.START_TIME, sessionStartTime);
        mailNotificationDetails.put(Constants.START_DATE, sessionStartDate);

        return mailNotificationDetails;
    }


    private void sendNotificationToLearner( Map<String, String> mailNotificationDetails) {
        int status = Integer.parseInt(mailNotificationDetails.get(Constants.STATUS));
        String userId = mailNotificationDetails.get(Constants.USER_ID);
        Set<String> userIdSet = new HashSet<>();
        userIdSet.add(userId);
        Map<String, String> userDetails = getUserDetails(userIdSet);
        String learnerName = userDetails.get(Constants.FIRST_NAME);
        String learnerEmail = userDetails.get(Constants.EMAIL);

        Map<String, Object> params = new HashMap<>();
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setDeliveryType("message");
        notificationRequest.setIds(Arrays.asList(learnerEmail));
        notificationRequest.setMode("email");

        String attendance = "";
        if(status == 2){
            attendance = Constants.PRESENT;
        } else if(status == 0){
            attendance = Constants.ABSENT;
        }

        String body = configuration.getAttendanceNotificationMailBody().replace(SESSION_TAG,  mailNotificationDetails.get(Constants.SESSION_NAME))
                .replace(BLENDED_PROGRAM_TAG,  mailNotificationDetails.get(Constants.COURSE_NAME))
                .replace(SESSION_DATE_TAG, mailNotificationDetails.get(Constants.START_DATE))
                .replace(SESSION_START_TIME, mailNotificationDetails.get(Constants.START_TIME))
                .replace(SESSOION_END_TIME, mailNotificationDetails.get(Constants.END_TIME))
                .replace(ATTENDANCE_TAG, attendance);
        logger.info("Mail Body for the mail notification is : " + body);
        params.put("body", body);
        params.put(Constants.NAME, learnerName);
        params.put(Constants.FROM_EMAIL, configuration.getSupportEmail());
        params.put(Constants.ORG_NAME, Constants.KARMYOGI_BHARAT);
        Template template = new Template(constructEmailTemplate(configuration.getNotificationEmailTemplate(), params),configuration.getNotificationEmailTemplate(), params);
        template.setParams(params);
        Config config = new Config();
        config.setSubject(Constants.ATTENDANCE_MARKED);
        config.setSender(configuration.getSupportEmail());
        Map<String, Object> req = new HashMap<>();
        notificationRequest.setTemplate(template);
        notificationRequest.setConfig(config);
        Map<String, List<NotificationRequest>> notificationMap = new HashMap<>();
        notificationMap.put("notifications", Arrays.asList(notificationRequest));
        req.put(Constants.REQUEST, notificationMap);
        sendNotification(req);
    }

    public Map<String, String> getUserDetails(Set<String> userIds){
        Map<String, Object> request = getSearchObject(userIds);
        HashMap<String, String> headersValue = new HashMap<>();
        headersValue.put("Content-Type", "application/json");
        try {
            Map<String, String> record = new HashMap<>();
            StringBuilder url = new StringBuilder(configuration.getLmsServiceHost()).append(configuration.getLmsUserSearchEndPoint());
            Map<String, Object> searchProfileApiResp = outboundReqService.fetchResultUsingPost(url.toString(), request, headersValue);
            if (searchProfileApiResp != null
                    && "OK".equalsIgnoreCase((String) searchProfileApiResp.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
                Map<String, Object> response = (Map<String, Object>) map.get(Constants.RESPONSE);
                List<Map<String, Object>> contents = (List<Map<String, Object>>) response.get(Constants.CONTENT);
                if (!CollectionUtils.isEmpty(contents)) {
                    Map<String, Object> content = contents.get(0);
                    HashMap<String, Object> profileDetails = (HashMap<String, Object>) content.get(Constants.PROFILE_DETAILS);
                    if (!CollectionUtils.isEmpty(profileDetails)) {
                        HashMap<String, Object> personalDetails = (HashMap<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
                        if (!CollectionUtils.isEmpty(personalDetails)) {
                            record.put(Constants.FIRST_NAME, (String)personalDetails.get("firstname"));
                            record.put(Constants.EMAIL, (String)personalDetails.get(Constants.PRIMARY_EMAIL));
                        }
                    }
                }
            }
            logger.info("User Details Successfully fetched: " + record);
            return record;
        } catch (Exception e) {
            logger.error("Exception while fetching user setails : ",e);
            throw new ApplicationException("Hub Service ERROR: ", e);
        }
    }

    private Map<String, Object> getSearchObject(Set<String> userIds) {
        Map<String, Object> requestObject = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", userIds);
        request.put("limit", userIds.size());
        request.put("offset", 0);
        request.put("filters", filters);
        requestObject.put(Constants.REQUEST, request);
        return requestObject;
    }

    private void sendNotification(Map<String, Object> request) {
        StringBuilder builder = new StringBuilder();
        builder.append(configuration.getNotifyServiceHost()).append(configuration.getNotifyServicePath());
        try {
            Map<String, Object> response = outboundReqService.fetchResultUsingPost(builder.toString(), request, null);
            logger.info("The email notification is successfully sent, response is: " + response);
        } catch (Exception e) {
            logger.error("Exception while posting the data in notification service: ", e);
        }
    }

    private String constructEmailTemplate(String templateName, Map<String, Object> params) {
        String replacedHTML = new String();
        try {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.NAME, templateName);
            List<Map<String, Object>> templateMap = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_EMAIL_TEMPLATE, propertyMap, Collections.singletonList(Constants.TEMPLATE));
            String htmlTemplate = templateMap.stream()
                    .findFirst()
                    .map(template -> (String) template.get(Constants.TEMPLATE))
                    .orElse(null);
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();
            VelocityContext context = new VelocityContext();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
            StringWriter writer = new StringWriter();
            velocityEngine.evaluate(context, writer, "HTMLTemplate", htmlTemplate);
            replacedHTML = writer.toString();
        } catch (Exception e) {
            logger.error("Unable to create template ", e);
        }
        return replacedHTML;
    }
}
