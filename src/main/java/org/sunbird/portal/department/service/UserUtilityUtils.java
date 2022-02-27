package org.sunbird.portal.department.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.portal.department.model.Actor;
import org.sunbird.portal.department.model.AuditEvents;
import org.sunbird.portal.department.model.Context;
import org.sunbird.portal.department.model.Edata;
import org.sunbird.portal.department.model.LastLoginInfo;
import org.sunbird.portal.department.model.ObjectData;
import org.sunbird.portal.department.model.Pdata;
import org.sunbird.portal.department.model.Rollup;
import org.sunbird.workallocation.util.LastLoginConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserUtilityUtils {

	@Autowired
	private CbExtServerProperties cbExtServerProperties;

	@Autowired
	private Producer producer;

	@Value("${kafka.topics.userutility.telemetry.event}")
	public String telemetryEventTopicName;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	private AuditEvents getTelemetryEvent(LastLoginInfo userLoginInfo) {
		AuditEvents event = new AuditEvents();
		event.setEid(LastLoginConstants.EID);
		long ts = new Timestamp(new Date().getTime()).getTime();
		event.setEts(ts);
		event.setVer(cbExtServerProperties.getVersion());
		event.setMid(UUID.randomUUID().toString());
		event.setActor(new Actor(userLoginInfo.getUserId(), LastLoginConstants.USER_CONST));
		event.setContext(new Context(userLoginInfo.getOrgId(),
				new Pdata(cbExtServerProperties.getFirstLoginId(), cbExtServerProperties.getFirstLoginPid(),
						cbExtServerProperties.getVersion()),
				LastLoginConstants.USER_CONST, new ArrayList<>(), new Rollup(userLoginInfo.getOrgId())));
		event.setObject(new ObjectData(userLoginInfo.getUserId(), LastLoginConstants.USER_CONST));
		event.setEdata(
				new Edata(LastLoginConstants.STATE, new ArrayList<>(Arrays.asList(LastLoginConstants.LOGIN_TIME)),
						userLoginInfo.getLoginTime().getTime()));
		return event;
	}

	public void pushDataToKafka(LastLoginInfo userLoginInfo) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			AuditEvents event = getTelemetryEvent(userLoginInfo);
			logger.info("audit records for users who login for the first time .....");
			logger.info(mapper.writeValueAsString(event));
			producer.push(cbExtServerProperties.getUserUtilityTopic(), event);
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
