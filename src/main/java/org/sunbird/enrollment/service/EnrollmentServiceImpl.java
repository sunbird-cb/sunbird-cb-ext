package org.sunbird.enrollment.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.producer.Producer;
import org.sunbird.enrollment.model.EnrollmentModel;
import java.util.*;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    Producer kafkaProducer;

    @Value("${kafka.topics.enrollment.sync}")
    public String enrollmentSyncTopicName;

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    public SBApiResponse generateEvent(Map<String, Object> request) {
        SBApiResponse response = new SBApiResponse(Constants.ENROLLMENT_EVENT);
        String errMsg = validateRequest(request);
        if (StringUtils.isNotEmpty(errMsg)) {
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        try {
            Map<String, Object> event = new HashMap<>();
            EnrollmentModel enrollmentModel = new EnrollmentModel();
            event.put(Constants.EID_VALUE, enrollmentModel.getEid());
            event.put(Constants.ETS, enrollmentModel.getEts());
            event.put(Constants.MID, enrollmentModel.getMid());
            Map<String, Object> actor = new HashMap<>();
            actor.put(Constants.TYPE, enrollmentModel.getActorType());
            actor.put(Constants.ID, enrollmentModel.getActorId());
            event.put(Constants.ACTOR, actor);
            Map<String, Object> context = new HashMap<>();
            Map<String, Object> pData = new HashMap<>();
            pData.put(Constants.VER_VALUE, enrollmentModel.getContextVer());
            pData.put(Constants.ID, enrollmentModel.getContextId());
            context.put(Constants.P_DATA, pData);
            event.put(Constants.CONTEXT, context);
            event.put(Constants.OBJECT, request.get(Constants.OBJECT));
            event.put(Constants.E_DATA, request.get(Constants.E_DATA));
            System.out.println(event);
            kafkaProducer.push(enrollmentSyncTopicName, event);
            response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
            response.getResult().put(Constants.VALUE, event);
            response.getParams().setStatus(Constants.SUCCESS);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to generate enrollment event. Exception: ", e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErr(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public String validateRequest(Map<String, Object> request) {
        StringBuffer str = new StringBuffer();
        List<String> errObjList = new ArrayList<String>();
        if (ObjectUtils.isEmpty(request)) {
            str.append("Request object is empty.");
            return str.toString();
        }
        if (ObjectUtils.isEmpty(request.get(Constants.OBJECT))) {
            errObjList.add(Constants.OBJECT);
        }
        if (ObjectUtils.isEmpty(request.get(Constants.E_DATA))) {
            errObjList.add(Constants.E_DATA);
        }
        if (!errObjList.isEmpty()) {
            str.append("Process Failed. Missing Params - [").append(errObjList.toString()).append("]");
        }
        return str.toString();

    }
}


