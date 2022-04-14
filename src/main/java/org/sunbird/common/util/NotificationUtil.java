package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.EmailConfig;
import org.sunbird.common.model.Notification;
import org.sunbird.common.model.Template;

import javax.validation.constraints.Email;
import java.lang.reflect.Type;
import java.util.*;

import static org.sunbird.common.util.Constants.INCOMPLETE_COURSES;
import static org.sunbird.common.util.Constants.SUBJECT_;

@Service(Constants.NOTIFICATION_UTIL)
public class NotificationUtil {
    public static final Logger Logger;
    private static final String EXCEPTION = "Exception in Send Notification %s";

    static {
        Logger = LoggerFactory.getLogger(NotificationUtil.class);
    }

    public <params> void sendNotification(List<String> sendTo, Map<String, Object> params, String senderMail, Boolean sendNotification, String notificationUrl) {
        new Thread(() -> {
            try {
                if (sendNotification) {
                    Logger.info("Entering the send notification");
                    HttpHeaders headers = new HttpHeaders();
                    RestTemplate restTemplate = new RestTemplate();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> notificationRequest = new HashMap<>();
                    Map<String, List<Notification>> notifications = new HashMap<>();
                    EmailConfig ec =new EmailConfig(senderMail, (String) params.get(SUBJECT_));
                    Template t = new Template(null, INCOMPLETE_COURSES, params);
                    Notification n = new Notification();
                    n.setConfig(ec);
                    n.setMode(Constants.EMAIL);
                    n.setDeliveryType(Constants.MESSAGE);
                    n.setIds(sendTo);
                    Logger.info(sendTo.toString());
                    n.setTemplate(t);
                    notifications.put("notifications", Arrays.asList(n));
                    notificationRequest.put("request", notifications);
                    Logger.info(String.format("Notification Request : %s", notificationRequest));
                    HttpEntity<Object> req = new HttpEntity<>(notificationRequest, headers);
                    ResponseEntity<String> resp = restTemplate.postForEntity(notificationUrl, req, String.class);
                    Logger.info(resp.toString());
                }
            } catch (Exception e) {
                Logger.error(String.format(EXCEPTION, e.getMessage()));
            }
        }).start();
    }

}