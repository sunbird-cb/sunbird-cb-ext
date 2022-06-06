package org.sunbird.common.util;

import static org.sunbird.common.util.Constants.INCOMPLETE_COURSES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.EmailConfig;
import org.sunbird.common.model.Notification;
import org.sunbird.common.model.Template;

@Service(Constants.NOTIFICATION_UTIL)
public class NotificationUtil {
	public static final Logger Logger;
	private static final String EXCEPTION = "Exception in Send Notification %s";

	@Autowired
	RestTemplate restTemplate;

	static {
		Logger = LoggerFactory.getLogger(NotificationUtil.class);
	}

	public <params> void sendNotification(List<String> sendTo, Map<String, Object> params, String senderMail,
			String notificationUrl) {
		new Thread(() -> {
			try {
				HttpHeaders headers = new HttpHeaders();
				RestTemplate restTemplate = new RestTemplate();
				headers.setContentType(MediaType.APPLICATION_JSON);
				Map<String, Object> notificationRequest = new HashMap<>();
				List<Object> notificationTosend = new ArrayList<>(Arrays.asList(new Notification(Constants.EMAIL,
						Constants.MESSAGE, new EmailConfig(senderMail, Constants.INCOMPLETE_COURSES_MAIL_SUBJECT),
						sendTo, new Template(null, INCOMPLETE_COURSES, params))));
				notificationRequest.put(Constants.REQUEST, new HashMap<String, List<Object>>() {
					{
						put(Constants.NOTIFICATIONS, notificationTosend);
					}
				});
				Logger.info(String.format("Notification Request : %s", notificationRequest));
				HttpEntity<Object> req = new HttpEntity<>(notificationRequest, headers);
				ResponseEntity<String> resp = restTemplate.postForEntity(notificationUrl, req, String.class);
			} catch (Exception e) {
				Logger.error(String.format(EXCEPTION, e.getMessage()));
			}
		}).start();
	}

	public void sendNotification(List<Map<String, Object>> notifications) {
		new Thread(() -> {
			try {
				String notificationUrl = PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST)
						+ PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				Map<String, Object> notificationRequest = new HashMap<>();
				notificationRequest.put(Constants.REQUEST, new HashMap<String, Object>() {
					{
						put(Constants.NOTIFICATIONS, notifications);
					}
				});

				HttpEntity<Object> req = new HttpEntity<>(notificationRequest, headers);
				Logger.info(String.format("Notification Request : %s", notificationRequest));
				restTemplate.postForEntity(notificationUrl, req, Object.class);
			} catch (Exception e) {
				Logger.error(String.format(EXCEPTION, e.getMessage()));
			}
		}).start();
	}

}