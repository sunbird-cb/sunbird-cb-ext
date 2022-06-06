package org.sunbird.user.registration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.NotificationUtil;
import org.sunbird.user.registration.model.UserRegistration;

@Service
public class UserRegistrationNotificationServiceImpl implements UserRegistrationNotificationService {

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	NotificationUtil notificationUtil;

	@Override
	public void sendNotification(UserRegistration userRegistration) {
		List<String> sendTo = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			{
				add(userRegistration.getEmail());
			}
		};

		Map<String, Object> notificationObj = new HashMap<>();
		notificationObj.put(Constants.MODE, Constants.EMAIL);
		notificationObj.put(Constants.DELIVERY_TYPE, Constants.MESSAGE);
		notificationObj.put(Constants.CONFIG, new HashMap<String, Object>() {
			{
				put(Constants.SUBJECT, serverProperties.getUserRegistrationSubject());
			}
		});
		notificationObj.put(Constants.IDS, sendTo);
		notificationObj.put(Constants.TEMPLATE,
				notificationMessage(userRegistration.getStatus(), userRegistration.getRegistrationCode()));

		if (notificationObj.get(Constants.TEMPLATE) != null) {
			notificationUtil.sendNotification(Arrays.asList(notificationObj));
		}
	}

	private Map<String, Object> notificationMessage(String status, String regCode) {
		Map<String, Object> template = new HashMap<>();
		template.put(Constants.ID, Constants.USER_REGISTERATION_TEMPLATE);
		Map<String, Object> params = new HashMap<>();

		template.put(Constants.PARAMS, params);
		switch (status) {
		case "WF_INITIATED":
			params.put(Constants.TITLE, serverProperties.getUserRegistrationThankyouMessage());
			params.put(Constants.STATUS,
					serverProperties.getUserRegistrationStatus().replace(Constants.STATUS_PARAM, Constants.INITIATED));
			params.put(Constants.TITLE,
					serverProperties.getUserRegistrationTitle().replace(Constants.STATUS_PARAM, Constants.INITIATED));
			params.put(Constants.DESCRIPTION, serverProperties.getUserRegistrationInitiatedMessage()
					.replace(Constants.REG_CODE_PARAM, "<b>" + regCode + "</b>"));
			break;
		case "WF_APPROVED":
			params.put(Constants.STATUS,
					serverProperties.getUserRegistrationStatus().replace(Constants.STATUS_PARAM, Constants.APPROVED));
			params.put(Constants.TITLE,
					serverProperties.getUserRegistrationTitle().replace(Constants.STATUS_PARAM, Constants.APPROVED));
			params.put(Constants.DESCRIPTION, serverProperties.getUserRegistrationApprovedMessage());
			params.put(Constants.BUTTON_URL, serverProperties.getUserRegistrationDomainName());
			params.put(Constants.BUTTON_NAME, serverProperties.getUserRegisterationButtonName());
			break;
		case "WF_DENIED":
			params.put(Constants.STATUS,
					serverProperties.getUserRegistrationStatus().replace(Constants.STATUS_PARAM, Constants.DENIED));
			params.put(Constants.TITLE,
					serverProperties.getUserRegistrationTitle().replace(Constants.STATUS_PARAM, Constants.DENIED));
			break;
		case "FAILED":
			params.put(Constants.STATUS, serverProperties.getUserRegistrationStatus().replace(Constants.STATUS_PARAM,
					Constants.FAILED.toLowerCase()));
			params.put(Constants.TITLE, serverProperties.getUserRegistrationTitle().replace(Constants.STATUS_PARAM,
					Constants.FAILED.toLowerCase()));
			params.put(Constants.STATUS, serverProperties.getUserRegistrationFailedMessage());
			break;

		default:
			template = null;
			break;
		}
		return template;
	}

}
