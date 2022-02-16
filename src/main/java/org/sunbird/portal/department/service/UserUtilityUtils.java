package org.sunbird.portal.department.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.workallocation.model.telemetryevent.Actor;
import org.sunbird.workallocation.model.telemetryevent.Context;
import org.sunbird.workallocation.model.telemetryevent.Event;
import org.sunbird.workallocation.model.telemetryevent.ObjectData;
import org.sunbird.workallocation.model.telemetryevent.Pdata;
import org.sunbird.workallocation.util.WorkAllocationConstants;

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

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddXHH:mm:ss.ms", Locale.getDefault());

	private Event getTelemetryEvent(Map<String, Object> watObject) {
		HashMap<String, Object> eData = new HashMap<>();
		eData.put("state", watObject.get("status"));
		eData.put("props", WorkAllocationConstants.PROPS);
		HashMap<String, Object> cbObject = new HashMap<>();
		cbObject.put("id", watObject.get("id"));
		cbObject.put("type", WorkAllocationConstants.TYPE);
		cbObject.put("ver", String.valueOf(1.0));
		cbObject.put("name", watObject.get("name"));
		cbObject.put("org", watObject.get("deptName"));
		eData.put("cb_object", cbObject);
		HashMap<String, Object> data = new HashMap<>();
		data.put("data", watObject);
		eData.put("cb_data", data);
		Event event = new Event();
		Actor actor = new Actor();
		actor.setId((String) watObject.get("id"));
		actor.setType(WorkAllocationConstants.USER_CONST);
		event.setActor(actor);
		event.setEid(WorkAllocationConstants.EID);
		event.setEdata(eData);
		event.setVer(WorkAllocationConstants.VERSION);
		event.setEts((Long) watObject.get("updatedAt"));
		event.setMid(WorkAllocationConstants.CB_NAME + "." + UUID.randomUUID());
		Context context = new Context();
		context.setChannel((String) watObject.get("deptId"));
		context.setEnv(WorkAllocationConstants.WAT_NAME);
		Pdata pdata = new Pdata();
		pdata.setId(cbExtServerProperties.getWatTelemetryEnv());
		pdata.setPid(WorkAllocationConstants.MDO_NAME_CONST);
		pdata.setVer(WorkAllocationConstants.VERSION_TYPE);
		context.setPdata(pdata);
		event.setContext(context);
		ObjectData objectData = new ObjectData();
		objectData.setId((String) watObject.get("id"));
		objectData.setType(WorkAllocationConstants.WORK_ORDER_ID_CONST);
		event.setObject(objectData);
		return event;
	}

	public void pushDataToKafka() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Event event = getTelemetryEvent(null);
			logger.info("audit records for users who login for the first time .....");
			logger.info(mapper.writeValueAsString(event));
			producer.push(cbExtServerProperties.getUserUtilityTopic(), event);
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
