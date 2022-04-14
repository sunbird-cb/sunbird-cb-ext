package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.EmailConfig;
import org.sunbird.common.model.Notification;
import org.sunbird.common.model.Template;

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

    public <params> void sendNotification(List<String> sendTo, Map<String, Object> params, String senderMail, Boolean sendNotification, String notificationUrl, String authApiKey) {
        new Thread(() -> {
            try {
                if (sendNotification) {
                    HttpHeaders headers = new HttpHeaders();
                    RestTemplate restTemplate = new RestTemplate();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set(Constants.AUTHORIZATION, authApiKey);
                    //headers.set(Constants.X_USER_, params.getAuthToken());
                    Map<String, Object> notificationRequest = new HashMap<>();
                    List<Object> notificationTosend = new ArrayList<>(Arrays.asList(new Notification(Constants.EMAIL, Constants.MESSAGE,
                            new EmailConfig(senderMail, (String) params.get(SUBJECT_)), sendTo,
                            new Template(null, INCOMPLETE_COURSES, params))));
                    notificationRequest.put(Constants.REQUEST, new HashMap<String, List<Object>>() {
                        {
                            put(Constants.NOTIFICATIONS, notificationTosend);
                        }
                    });
                    HttpEntity<Object> req = new HttpEntity<>(notificationRequest, headers);
                    restTemplate.postForEntity(notificationUrl, req, String.class);
                }
            } catch (Exception e) {
                Logger.error(String.format(EXCEPTION, e.getMessage()));
            }
        }).start();
    }

}