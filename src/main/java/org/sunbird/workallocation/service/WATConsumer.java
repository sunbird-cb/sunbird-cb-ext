package org.sunbird.workallocation.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.workallocation.model.PropertyFilterMixIn;
import org.sunbird.workallocation.model.WorkAllocationDTOV2;
import org.sunbird.workallocation.model.telemetryEvent.Actor;
import org.sunbird.workallocation.model.telemetryEvent.Context;
import org.sunbird.workallocation.model.telemetryEvent.Event;
import org.sunbird.workallocation.model.telemetryEvent.ObjectData;
import org.sunbird.workallocation.model.telemetryEvent.Pdata;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@Service
public class WATConsumer {

	@Autowired
	private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Autowired
	private CbExtServerProperties cbExtServerProperties;

	@Autowired
	private Producer producer;

	@Autowired
	private CassandraOperation cassandraOperation;

	@Value("${kafka.topics.parent.telemetry.event}")
	public String telemetryEventTopicName;

	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddXHH:mm:ss.ms", Locale.getDefault());

	private static final String[] ignorableFieldsForPublishedState = { "userName", "userEmail", "submittedFromName",
			"submittedFromEmail", "submittedToName", "submittedToEmail", "createdByName", "updatedByName" };

	@KafkaListener(id = "id2", topics = "${kafka.topics.wat.telemetry.event}", groupId = "${kafka.topics.wat.telemetry.event.topic.consumer}")
	public void processMessage(ConsumerRecord<String, String> data) {
		try {
			logger.info("Consuming the audit records for WAT .....");
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> workAllocationObj = mapper.readValue(String.valueOf(data.value()), Map.class);

			Map<String, Object> workOrderMap = new HashMap<>();
			workOrderMap.put(Constants.ID, (String) workAllocationObj.get("workorderId"));
			List<Map<String, Object>> workOrderCassandraModelOptional = cassandraOperation
					.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_WORK_ORDER, workOrderMap, new ArrayList<>());

			if (!workOrderCassandraModelOptional.isEmpty()) {
				Map<String, Object> watObj = mapper
						.readValue((String) workOrderCassandraModelOptional.get(0).get(Constants.DATA), Map.class);
				logger.info("consumed record for WAT ...");
				logger.info(mapper.writeValueAsString(watObj));
				List<String> userIds = (List<String>) watObj.get("userIds");
				if (!CollectionUtils.isEmpty(userIds)) {
					Map<String, Object> workAllocationMap = new HashMap<>();
					workAllocationMap.put(Constants.ID, userIds);
					List<Map<String, Object>> workAllocationList = cassandraOperation.getRecordsByProperties(
							Constants.KEYSPACE_SUNBIRD, Constants.TABLE_WORK_ALLOCATION, workAllocationMap, new ArrayList<>());

					List<WorkAllocationDTOV2> workAllocations = new ArrayList<>();
					for (Map<String, Object> workAllocationCassandraModel : workAllocationList) {
						try {
							workAllocations
									.add(mapper.readValue((String) workAllocationCassandraModel.get(Constants.DATA),
											WorkAllocationDTOV2.class));
						} catch (IOException e) {
							logger.error(e);
						}
					}
					watObj.put("users", workAllocations);

					// update the user_workorder_mapping table
					updateUserWorkOrderMappings(watObj, workAllocations);
				}
				watObj = getFilterObject(watObj);
				Event event = getTelemetryEvent(watObj);
				logger.info("Posting WAT event to telemetry ...");
				logger.info(mapper.writeValueAsString(event));
				// postTelemetryEvent(event);
				producer.push(telemetryEventTopicName, event);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private Map<String, Object> getFilterObject(Map<String, Object> watObj) throws IOException {
		ObjectMapper mapper1 = new ObjectMapper();
		mapper1.addMixIn(Object.class, PropertyFilterMixIn.class);
		FilterProvider filters = new SimpleFilterProvider().addFilter("PropertyFilter",
				SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldsForPublishedState));
		String writer = mapper1.writer(filters).writeValueAsString(watObj);
		watObj = mapper1.readValue(writer, Map.class);
		return watObj;
	}

	private void postTelemetryEvent(Event event) {
		outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getTelemetryBaseUrl() + cbExtServerProperties.getTelemetryEndpoint(), event);
	}

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
		// event.setType(WorkAllocationConstants.EVENTS_NAME);
		return event;
	}

	public void updateUserWorkOrderMappings(Map<String, Object> workOrderMap,
			List<WorkAllocationDTOV2> workAllocationDTOV2List) {
		try {
			String workOrderId = (String) workOrderMap.get("id");
			String status = (String) workOrderMap.get("status");
			if (!CollectionUtils.isEmpty(workAllocationDTOV2List) && !StringUtils.isEmpty(status)) {
				List<Map<String, Object>> userAllocationMappingList = new ArrayList<>();
				workAllocationDTOV2List.forEach(workAllocationDTOV2 -> {
					if (!StringUtils.isEmpty(workAllocationDTOV2.getUserId())
							&& !StringUtils.isEmpty(workAllocationDTOV2.getId())) {
						Map<String, Object> propertyMap = new HashMap<>();
						propertyMap.put(Constants.USER_ID, workAllocationDTOV2.getUserId());
						propertyMap.put(Constants.WORK_ALLOCATION_ID, workAllocationDTOV2.getId());
						propertyMap.put(Constants.WORK_ORDER_ID, workOrderId);
						propertyMap.put(Constants.STATUS, status);
						userAllocationMappingList.add(propertyMap);
					}
				});

				cassandraOperation.insertBulkRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_USER_WORK_ALLOCATION_MAPPING,
						userAllocationMappingList);
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

}
