package org.sunbird.common.util;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.EmailConfig;
import org.sunbird.common.model.Notification;
import org.sunbird.common.model.Template;

@Service(Constants.NOTIFICATION_UTIL)
public class NotificationUtil {
	private static final Logger logger = LoggerFactory.getLogger(NotificationUtil.class);
	private static final String EXCEPTION = "Exception in Send Notification %s";

	@Autowired
	RestTemplate restTemplate;

	public <params> void sendNotification(List<String> sendTo, Map<String, Object> params, String senderMail,
			String notificationUrl, String emailTemplate, String emailSubject) {
		new Thread(() -> {
			try {
				HttpHeaders headers = new HttpHeaders();
				RestTemplate restTemplate = new RestTemplate();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set(Constants.AUTHORIZATION, "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJSSXZtaHRpTkxRT1lKT3dYR2xnRElReFp4bHdyZmVTZCJ9.onjwk3QTql0oZYvM-xOPuCDcBJKGTVa65J64j2hy8H0");
				headers.set(Constants.X_AUTH_TOKEN, "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJVMkpUdlpERFY4eG83ZmtfNHd1Yy1kNVJmNjRPTG1oemlRRUhjR25Vc2hNIn0.eyJqdGkiOiIwZTgyMmNmMS00OWQ2LTQwZjQtYmJjZC0xMDY1YzUxMWUzYjgiLCJleHAiOjE2NjQ1MzE1NDAsIm5iZiI6MCwiaWF0IjoxNjY0NDQ1MTQwLCJpc3MiOiJodHRwczovL2lnb3QtZGV2LmluL2F1dGgvcmVhbG1zL3N1bmJpcmQiLCJzdWIiOiJmOjkyM2JkYzE4LTUyMGQtNDhkNC1hODRlLTNjZGUxZTY1NWViZDpkYWNlZTk1YS05Zjg5LTQxZmMtODZiMi05MTU0ZGFhMjI5YzkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJiYTgzZjQ2Mi0yNjI3LTRkZjUtYmQyZC1iMDAzY2Y1ZDE3NDYiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInNjb3BlIjoiIiwibmFtZSI6IkthcnRoaWsgVGVzdENCUEVkaXRvciIsInByZWZlcnJlZF91c2VybmFtZSI6ImthcnRoaWt0ZXN0Y2JwZWRpdG9yX2c2NHQiLCJnaXZlbl9uYW1lIjoiS2FydGhpayIsImZhbWlseV9uYW1lIjoiVGVzdENCUEVkaXRvciIsImVtYWlsIjoia2EqKioqKioqKioqKioqKkB5b3BtYWlsLmNvbSJ9.i51r4r2itPcQPo2QGtXsl4691JFE0ctlK-ybvEZ0d4Ql1qf2fGP2jo3gz1lTOWqGYrb9_q1AOHeQGe6JG6G48FBVudted8lpIGv66hwBaVZ_Bj1LzkOg5T88B-I-HRt63GLTihLI2oI6azrBA0Ud4gqFQpVqvQhHLuHcHf2Ii0Zgs2uStDWycX1TySzwqGDhrgCaefCeBSglLlHupHoPUh4tQdJQn_wWmB7K3J5T0jN0escjE8T1GdVk17hOCC0Pc0fCKsoKgPGr_jzQRw81lPR6zNpaKWCVqMyfkBvZEt9aRCAUmxE415pvoFqvpsNYgsmahmBcjbRHW4YbOXMujA");
				Map<String, Object> notificationRequest = new HashMap<>();
				List<Object> notificationTosend = new ArrayList<>(Arrays.asList(new Notification(Constants.EMAIL,
						Constants.MESSAGE, new EmailConfig(senderMail, emailSubject),
						sendTo, new Template(null, emailTemplate, params))));
				notificationRequest.put(Constants.REQUEST, new HashMap<String, List<Object>>() {
					{
						put(Constants.NOTIFICATIONS, notificationTosend);
					}
				});
				logger.info(String.format("Notification Request : %s", notificationRequest));
				HttpEntity<Object> req = new HttpEntity<>(notificationRequest, headers);
				restTemplate.postForEntity(notificationUrl, req, String.class);
			} catch (Exception e) {
				logger.error(String.format(EXCEPTION, e.getMessage()));
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
				logger.info(String.format("Notification Request : %s", notificationRequest));
				restTemplate.postForEntity(notificationUrl, req, Object.class);
			} catch (Exception e) {
				logger.error(String.format(EXCEPTION, e.getMessage()));
			}
		}).start();
	}

}