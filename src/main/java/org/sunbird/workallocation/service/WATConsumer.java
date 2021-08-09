package org.sunbird.workallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.workallocation.model.PropertyFilterMixIn;
import org.sunbird.workallocation.model.WorkOrderCassandraModel;
import org.sunbird.workallocation.model.WorkOrderPrimaryKeyModel;
import org.sunbird.workallocation.model.telemetryEvent.*;
import org.sunbird.workallocation.repo.WorkOrderRepo;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class WATConsumer {

    @Autowired
    private WorkOrderRepo workOrderRepo;

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddXHH:mm:ss.ms", Locale.getDefault());

    private static final String[] ignorableFieldsForPublishedState = {"userName", "userEmail", "submittedFromName", "submittedFromEmail", "submittedToName", "submittedToEmail"};

    @KafkaListener(id = "id0", groupId = "watTelemetryTopic-consumer", topicPartitions = {
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
                if (WorkAllocationConstants.PUBLISHED_STATUS.equals(watObj.get("status"))) {
                    mapper.addMixIn(Object.class, PropertyFilterMixIn.class);
                    FilterProvider filters = new SimpleFilterProvider().addFilter("filter properties by name", SimpleBeanPropertyFilter.serializeAllExcept(ignorableFieldsForPublishedState));
                    ObjectWriter writer = mapper.writer(filters);
                    watObj = mapper.readValue(writer.writeValueAsString(watObj), Map.class);
                    Event event = getTelemetryEvent(watObj);
                    postTelemetryEvent(event);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void postTelemetryEvent(Event event) {
        outboundRequestHandlerService.fetchResultUsingPost(cbExtServerProperties.getTelemetryBaseUrl() + cbExtServerProperties.getTelemetryEndpoint(),
                event);
    }

    private Event getTelemetryEvent(Map<String, Object> watObject) {
        Event event = new Event();
        Actor actor = new Actor();
        actor.setId((String) watObject.get("id"));
        actor.setType(WorkAllocationConstants.MDO_ADMIN_CONST);
        event.setActor(actor);
        event.setEid(WorkAllocationConstants.AUDIT_CONST);
        event.setEdata(watObject);
        event.setVer(WorkAllocationConstants.VERSION);
        event.setTimestamp(dateFormat.format(new Date((Long) watObject.get("updatedAt"))));
        event.setEts((Long) watObject.get("updatedAt"));
        Context context = new Context();
        context.setChannel("");
        context.setEnv(WorkAllocationConstants.WAT_NAME);
        Pdata pdata = new Pdata();
        pdata.setId(WorkAllocationConstants.MDO_PORTAL_CONST);
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
        event.setObject(objectData);
        event.setType(WorkAllocationConstants.EVENTS_NAME);
        return event;
    }
}
