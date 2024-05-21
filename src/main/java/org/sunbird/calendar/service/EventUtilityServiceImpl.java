package org.sunbird.calendar.service;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.util.HashMap;
import java.util.Map;

import static org.sunbird.common.util.Constants.*;

@Service
public class EventUtilityServiceImpl implements EventUtilityService {

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Autowired
    CbExtServerProperties props;
    private Logger logger = LoggerFactory.getLogger(EventUtilityServiceImpl.class);
    @Override
    public Map<String, Object> createEvent(Map<String, Object> eventMap, String userToken) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> event = new HashMap<String, Object>();
        event.put(Constants.EVENT, eventMap);
        request.put(Constants.REQUEST, event);
        Map<String, String> headerValues = ProjectUtil.getDefaultHeaders();
        if (StringUtils.isNotEmpty(userToken)) {
            headerValues.put(Constants.X_AUTH_TOKEN, userToken);
        }
        try {
            Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
                    props.getContentHost() + props.getEventCreateAPI(), request, headerValues);
            if (readData != null && Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
                if (!MapUtils.isEmpty(result)) {
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to run the create event flow",
                    e);
        }
        return null;
    }

    @Override
    public Map<String, Object> publishEvent(Map<String, Object> eventMap, String userToken) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> event = new HashMap<String, Object>();
        event.put(Constants.EVENT, eventMap);
        request.put(Constants.REQUEST, event);
        Map<String, String> headerValues = ProjectUtil.getDefaultHeaders();
        if (StringUtils.isNotEmpty(userToken)) {
            headerValues.put(Constants.X_AUTH_TOKEN, userToken);
        }
        try {
            Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPost(
                    props.getContentHost() + props.getEventPublishAPI() + Constants.SLASH + eventMap.get(Constants.IDENTIFIER), request, headerValues);
            if (readData != null && Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
                if (!MapUtils.isEmpty(result)) {
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to run the create event flow",
                    e);
        }
        return null;
    }

    @Override
    public Map<String, Object> compositeSearchForEvent(Map<String, Object> filters, String userToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(USER_TOKEN, userToken);
        headers.put(AUTHORIZATION, props.getSbApiKey());
        HashMap<String, Object> searchBody = new HashMap<>();
        HashMap<String, Object> searchReq = new HashMap<>();
        searchReq.put(FILTERS, filters);
        searchBody.put(REQUEST, searchReq);
        Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(
                props.getKmBaseHost() + props.getKmCompositeSearchPath(), searchBody,
                headers);
        if (null != response && Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
            return (Map<String, Object>) response.get(Constants.RESULT);
        }
        return null;
    }

    @Override
    public Map<String, Object> updateEvent(Map<String, Object> eventMap, String userToken) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> event = new HashMap<String, Object>();
        event.put(Constants.EVENT, eventMap);
        request.put(Constants.REQUEST, event);
        Map<String, String> headerValues = ProjectUtil.getDefaultHeaders();
        if (StringUtils.isNotEmpty(userToken)) {
            headerValues.put(Constants.X_AUTH_TOKEN, userToken);
        }
        try {
            Map<String, Object> readData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
                    props.getContentHost() + props.getEventUpdateAPI() + Constants.SLASH + eventMap.get(Constants.IDENTIFIER), request, headerValues);
            if (readData != null && Constants.OK.equalsIgnoreCase((String) readData.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> result = (Map<String, Object>) readData.get(Constants.RESULT);
                if (!MapUtils.isEmpty(result)) {
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to run the update event flow",
                    e);
        }
        return null;
    }
}
