package org.sunbird.calendar.service;

import org.sunbird.user.registration.model.UserRegistration;

import java.util.Map;

public interface EventUtilityService {
    Map<String, Object> createEvent(Map<String, Object> eventMap, String userToken);
    Map<String, Object> publishEvent(Map<String, Object> eventMap, String userToken);
    Map<String, Object> compositeSearchForEvent(Map<String, Object> filters, String userToken);
    Map<String, Object> updateEvent(Map<String, Object> eventMap, String userToken);

}
