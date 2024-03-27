package org.sunbird.cbp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jboss.resteasy.spi.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.Config;
import org.sunbird.common.model.NotificationRequest;
import org.sunbird.common.model.Template;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.Constants;
import org.sunbird.core.config.PropertiesConfig;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.user.service.UserUtilityService;

import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class CbplanContentConsumer {

    private final CbExtLogger logger = new CbExtLogger(getClass().getName());


    PropertiesConfig configuration;


    private final  OutboundRequestHandlerServiceImpl outboundReqService;

    private ObjectMapper mapper = new ObjectMapper();


    CassandraOperation cassandraOperation;

    UserUtilityService userService;
    @Autowired
    public CbplanContentConsumer(PropertiesConfig configuration, OutboundRequestHandlerServiceImpl outboundReqService, ObjectMapper mapper, CassandraOperation cassandraOperation, UserUtilityService userService) {
        this.configuration = configuration;
        this.outboundReqService = outboundReqService;
        this.mapper = mapper;
        this.cassandraOperation = cassandraOperation;
        this.userService = userService;
    }

    @KafkaListener(topics = "${kafka.topic.cbplan.content.request}", groupId = "${kafka.topic.cbplan.content.request.group}")
    public void cbplanContentRequestConsumer(ConsumerRecord<String, String> data) {
        try {
            Map<String, Object> cbplanContentRequest = mapper.readValue(data.value(), HashMap.class);
            CompletableFuture.runAsync(() -> {
                processKafkaMessage(cbplanContentRequest);
            });
        } catch(Exception e) {           
            logger.error("Failed to process content request. Message received : " + data.value(), e);
        }
    }

    private void processKafkaMessage(Map<String, Object> cbplanContentRequest) {
        try {
            long startTime = System.currentTimeMillis();
            String competencyInfo = (String) cbplanContentRequest.get(Constants.COMPETENCY_INFO);
            Map<String, Object> competencyInfoMap = mapper.readValue(competencyInfo, HashMap.class);

            List<Map<String, Object>> competencyThemes = (List<Map<String, Object>>) competencyInfoMap.get(Constants.CHILDREN);
            StringBuilder allThemes = new StringBuilder();
            StringBuilder allSubThemes = new StringBuilder();
            for(Map<String, Object> theme : competencyThemes){
                allThemes.append(theme.get(Constants.NAME)).append(", ");
                List<Map<String, Object>> competencySubThemes = (List<Map<String, Object>>) theme.get(Constants.CHILDREN);
                for(Map<String, Object>  subTheme : competencySubThemes){
                    allSubThemes.append(subTheme.get(Constants.NAME)).append(", ");
                }
            }
            Set<String> providerRootOrgIds = new HashSet<>((List<String>) cbplanContentRequest.get(Constants.PROVIDER_ORG_ID));
            String mdoAdminId = (String) cbplanContentRequest.get(Constants.CREATED_BY);
            String mdoAdminEmail = userService.getUserDetails(Collections.singletonList(mdoAdminId), new ArrayList<>()).get(mdoAdminId).get(Constants.EMAIL);

            Map<String, Object> mailNotificationDetails = new HashMap<>();
            mailNotificationDetails.put(Constants.PROVIDER_EMAIL_ID_LIST, getCBPAdminDetails(providerRootOrgIds));
            mailNotificationDetails.put(Constants.MDO_NAME, cbplanContentRequest.get(Constants.MDO_NAME));
            mailNotificationDetails.put(Constants.COMPETENCY_AREA, competencyInfoMap.get(Constants.NAME));
            mailNotificationDetails.put(Constants.COMPETENCY_THEMES, allThemes.replace(allThemes.length()-2, allThemes.length() - 1, "."));
            mailNotificationDetails.put(Constants.COMPETENCY_SUB_THEMES, allSubThemes.replace(allSubThemes.length()-2, allSubThemes.length()-1, "."));
            mailNotificationDetails.put(Constants.DESCRIPTION , cbplanContentRequest.get(Constants.DESCRIPTION));
            mailNotificationDetails.put(Constants.COPY_EMAIL, mdoAdminEmail);
            sendNotificationToProviders(mailNotificationDetails);
            logger.info(String.format("Completed request for content. Time taken: %d milliseconds", (System.currentTimeMillis() - startTime)));
        } catch (Exception e) {
            logger.error("Exception occurred while sending email : " + e.getMessage(), e);
        }
    }

    public List<String> getCBPAdminDetails(Set<String> rootOrgIds){
        Map<String, Object> request = getSearchObject(rootOrgIds);
        HashMap<String, String> headersValue = new HashMap<>();
        headersValue.put("Content-Type", "application/json");
        try {
            List<String> providerIdEmails = new ArrayList<>();
            StringBuilder url = new StringBuilder(configuration.getLmsServiceHost()).append(configuration.getLmsUserSearchEndPoint());
            Map<String, Object> searchProfileApiResp = outboundReqService.fetchResultUsingPost(url.toString(), request, headersValue);
            if (searchProfileApiResp != null
                    && "OK".equalsIgnoreCase((String) searchProfileApiResp.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
                Map<String, Object> response = (Map<String, Object>) map.get(Constants.RESPONSE);
                List<Map<String, Object>> contents = (List<Map<String, Object>>) response.get(Constants.CONTENT);
                if (!CollectionUtils.isEmpty(contents)) {
                    for(Map<String, Object> content: contents){
                        String rootOrgId = (String)content.get(Constants.ROOT_ORG_ID);
                        HashMap<String, Object> profileDetails = (HashMap<String, Object>) content.get(Constants.PROFILE_DETAILS);
                        if (!CollectionUtils.isEmpty(profileDetails)) {
                            HashMap<String, Object> personalDetails = (HashMap<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
                            if (!CollectionUtils.isEmpty(personalDetails) && personalDetails.get(Constants.PRIMARY_EMAIL)!= null ) {
                                if(rootOrgIds.contains(rootOrgId))
                                    providerIdEmails.add((String)personalDetails.get(Constants.PRIMARY_EMAIL));
                            }
                        }
                    }
                }
            }
            logger.info("CBP Admin emails fetched successfully: " + providerIdEmails);
            return providerIdEmails;
        } catch (Exception e) {
            logger.error("Exception while fetching cbp admin details : " +e.getMessage() + " request : " + request,e);
            throw new ApplicationException("Hub Service ERROR: ", e);
        }
    }

    private Map<String, Object> getSearchObject(Set<String> rootOrgIds) {
        Map<String, Object> requestObject = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put(Constants.ROOT_ORG_ID, rootOrgIds);
        filters.put(Constants.ORGANIZATIONS_ROLES, Collections.singletonList(Constants.CBP_ADMIN));
        request.put(Constants.LIMIT, 100);
        request.put(Constants.OFFSET, 0);
        request.put(Constants.FILTERS, filters);
        request.put(Constants.FIELDS_CONSTANT, Arrays.asList("profileDetails.personalDetails.primaryEmail", Constants.ROOT_ORG_ID));
        requestObject.put(Constants.REQUEST, request);
        return requestObject;
    }

    private void sendNotificationToProviders( Map<String, Object> mailNotificationDetails) {
        List<String> providerIdList = (List<String>) mailNotificationDetails.get(Constants.PROVIDER_EMAIL_ID_LIST);
        String mdoName = (String) mailNotificationDetails.get(Constants.MDO_NAME);
        String mdoAdminEmail = (String) mailNotificationDetails.get(Constants.COPY_EMAIL);

        Map<String, Object> params = new HashMap<>();
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setDeliveryType(Constants.MESSAGE);
        notificationRequest.setIds(providerIdList);
        notificationRequest.setMode(Constants.EMAIL);
        notificationRequest.setCopyEmail(Collections.singletonList(mdoAdminEmail));

        params.put(Constants.MDO_NAME_PARAM, mdoName);
        params.put(Constants.NAME, mdoName);
        params.put(Constants.COMPETENCY_AREA_PARAM, mailNotificationDetails.get(Constants.COMPETENCY_AREA));
        params.put(Constants.COMPETENCY_THEME_PARAM, mailNotificationDetails.get(Constants.COMPETENCY_THEMES));
        params.put(Constants.COMPETENCY_SUB_THEME_PARAM, mailNotificationDetails.get(Constants.COMPETENCY_SUB_THEMES));
        params.put(Constants.DESCRIPTION, mailNotificationDetails.get(Constants.DESCRIPTION));
        params.put(Constants.FROM_EMAIL, configuration.getSupportEmail());
        params.put(Constants.ORG_NAME, mdoName);
        Template template = new Template(constructEmailTemplate(configuration.getCbplanContentRequestTemplate(), params),configuration.getCbplanContentRequestTemplate(), params);
        template.setParams(params);
        Config config = new Config();
        config.setSubject(Constants.REQUEST_CONTENT_SUBJECT);
        config.setSender(configuration.getSupportEmail());
        Map<String, Object> req = new HashMap<>();
        notificationRequest.setTemplate(template);
        notificationRequest.setConfig(config);
        Map<String, List<NotificationRequest>> notificationMap = new HashMap<>();
        notificationMap.put(Constants.NOTIFICATIONS, Collections.singletonList(notificationRequest));
        req.put(Constants.REQUEST, notificationMap);
        sendNotification(req);
    }

    private void sendNotification(Map<String, Object> request) {
        StringBuilder builder = new StringBuilder();
        builder.append(configuration.getNotifyServiceHost()).append(configuration.getNotifyServicePath());
        try {
            Map<String, Object> response = outboundReqService.fetchResultUsingPost(builder.toString(), request, null);
            logger.debug("The email notification is successfully sent, response is: " + response);
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
