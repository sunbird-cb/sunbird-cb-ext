package org.sunbird.user.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.telemetry.model.Actor;
import org.sunbird.telemetry.model.AuditEvents;
import org.sunbird.telemetry.model.Context;
import org.sunbird.telemetry.model.Edata;
import org.sunbird.telemetry.model.LastLoginInfo;
import org.sunbird.telemetry.model.ObjectData;
import org.sunbird.telemetry.model.Pdata;
import org.sunbird.telemetry.model.Rollup;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TelemetryUtils {

	@Autowired
	private CbExtServerProperties cbExtServerProperties;

	@Autowired
	private Producer producer;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	public AuditEvents getUserslastLoginTelemetryEventData(LastLoginInfo userLoginInfo) {
		AuditEvents event = new AuditEvents();
		event.setEid(Constants.EID);
		long ts = new Timestamp(new Date().getTime()).getTime();
		event.setEts(ts);
		event.setVer(cbExtServerProperties.getVersion());
		event.setMid(UUID.randomUUID().toString());
		event.setActor(new Actor(userLoginInfo.getUserId(), Constants.USER_CONST));
		event.setContext(new Context(userLoginInfo.getOrgId(),
				new Pdata(cbExtServerProperties.getFirstLoginId(), cbExtServerProperties.getFirstLoginPid(),
						cbExtServerProperties.getVersion()),
				Constants.USER_CONST, new ArrayList<>(), new Rollup(userLoginInfo.getOrgId())));
		event.setObject(new ObjectData(userLoginInfo.getUserId(), Constants.USER_CONST));
		event.setEdata(new Edata(Constants.CURRENT_STATE, new ArrayList<>(Arrays.asList(Constants.LOGIN_TIME)),
				userLoginInfo.getLoginTime().getTime()));
		return event;
	}

	public void pushDataToKafka(AuditEvents event, String topicName) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			logger.info("audit records for users who login for the first time .....");
			logger.info(mapper.writeValueAsString(event));
			producer.push(topicName, event);
		} catch (IOException e) {
			logger.error(e);
		}
	}

}