package org.sunbird.workallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.workallocation.model.PropertyFilterMixIn;
import org.sunbird.workallocation.model.WorkAllocationCassandraModel;
import org.sunbird.workallocation.model.WorkOrderCassandraModel;
import org.sunbird.workallocation.model.WorkOrderPrimaryKeyModel;
import org.sunbird.workallocation.model.telemetryEvent.*;
import org.sunbird.workallocation.repo.WorkAllocationRepo;
import org.sunbird.workallocation.repo.WorkOrderRepo;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class WATConsumer {

    @Autowired
    private WorkOrderRepo workOrderRepo;

    @Autowired
    private WorkAllocationRepo workAllocationRepo;

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddXHH:mm:ss.ms", Locale.getDefault());

    private static final String[] ignorableFieldsForPublishedState = {"userName", "userEmail", "submittedFromName", "submittedFromEmail", "submittedToName", "submittedToEmail", "createdByName", "updatedByName"};

    @KafkaListener(id = "id2", groupId = "watTelemetryTopic-consumer", topicPartitions = {
            @TopicPartition(topic = "${kafka.topics.wat.telemetry.event}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {
        try {
            logger.info("Consuming the audit records for WAT .....");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> workAllocationObj = mapper.readValue(String.valueOf(data.value()), Map.class);
            WorkOrderPrimaryKeyModel primaryKeyModel = new WorkOrderPrimaryKeyModel();
            primaryKeyModel.setId((String) workAllocationObj.get("workorderId"));
            Optional<WorkOrderCassandraModel> workOrderCassandraModelOptional = workOrderRepo.findById(primaryKeyModel);
            if (workOrderCassandraModelOptional.isPresent()) {
                WorkOrderCassandraModel workOrderCassandraModel = workOrderCassandraModelOptional.get();
                Map<String, Object> watObj = mapper.readValue(workOrderCassandraModel.getData(), Map.class);
                logger.info("consumed record for WAT ...");
                logger.info(mapper.writeValueAsString(watObj));
                List<String> userIds = (List<String>) watObj.get("userIds");
                if (!CollectionUtils.isEmpty(userIds)) {
                    List<WorkAllocationCassandraModel> workAllocationList = workAllocationRepo.findByIdIn(userIds);
                    List<Map<String, Object>> workAllocations = new ArrayList<>();
                    workAllocationList.forEach(workAllocationCassandraModel -> {
                        try {
                            workAllocations.add(mapper.readValue(workAllocationCassandraModel.getData(), Map.class));
                        } catch (IOException e) {
                            logger.error(e);
                        }
                    });
                    watObj.put("users", workAllocations);
                }
                watObj = getFilterObject(watObj);
                Event event = getTelemetryEvent(watObj);
                logger.info("Posting WAT event to telemetry ...");
                logger.info(mapper.writeValueAsString(event));
                postTelemetryEvent(event);
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private Map<String, Object> getFilterObject(Map<String, Object> watObj) throws IOException {
        ObjectMapper mapper1 = new ObjectMapper();
        mapper1.addMixIn(Object.class, PropertyFilterMixIn.class);
        FilterProvider filters = new SimpleFilterProvider().addFilter("PropertyFilter", SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldsForPublishedState));
        String writer = mapper1.writer(filters).writeValueAsString(watObj);
        watObj = mapper1.readValue(writer, Map.class);
        return watObj;
    }

    private void postTelemetryEvent(Event event) {
        outboundRequestHandlerService.fetchResultUsingPost(cbExtServerProperties.getTelemetryBaseUrl() + cbExtServerProperties.getTelemetryEndpoint(),
                event);
    }

    private Event getTelemetryEvent(Map<String, Object> watObject) {
        watObject.put("mdo_name", watObject.get("deptName"));
        watObject.put("state", watObject.get("status"));
        watObject.put("props", WorkAllocationConstants.PROPS);
        Event event = new Event();
        Actor actor = new Actor();
        actor.setId((String) watObject.get("id"));
        actor.setType(WorkAllocationConstants.USER_CONST);
        event.setActor(actor);
        event.setEid(WorkAllocationConstants.EID);
        event.setEdata(watObject);
        event.setVer(WorkAllocationConstants.VERSION);
        event.setTimestamp(dateFormat.format(new Date((Long) watObject.get("updatedAt"))));
        event.setEts((Long) watObject.get("updatedAt"));
        Context context = new Context();
        context.setChannel((String) watObject.get("deptId"));
        context.setEnv(WorkAllocationConstants.WAT_NAME);
        Pdata pdata = new Pdata();
        pdata.setId(cbExtServerProperties.getWatTelemetryEnv());
        pdata.setPid(WorkAllocationConstants.MDO_NAME_CONST);
        pdata.setVer(WorkAllocationConstants.VERSION_TYPE);
        context.setPdata(pdata);
        event.setContext(context);
        Flags flags = new Flags();
        flags.setPp_duplicate_skipped(true);
        flags.setPp_validation_processed(true);
        event.setFlags(flags);
        ObjectData objectData = new ObjectData();
        objectData.setId((String) watObject.get("id"));
        objectData.setType(WorkAllocationConstants.WORK_ORDER_ID_CONST);
        if (WorkAllocationConstants.DRAFT_STATUS.equals(watObject.get("status"))) {
            objectData.setName((String) watObject.get("name"));
            objectData.setOrg((String) watObject.get("deptName"));
            objectData.setType("");
        }
        event.setObject(objectData);
        event.setType(WorkAllocationConstants.EVENTS_NAME);
        return event;
    }
}
