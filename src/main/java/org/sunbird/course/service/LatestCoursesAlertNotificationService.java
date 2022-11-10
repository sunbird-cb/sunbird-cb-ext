package org.sunbird.course.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.validator.EmailValidator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.helper.cassandra.ServiceFactory;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.NotificationUtil;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.common.util.PropertiesCache;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.course.model.Content;
import org.sunbird.course.model.CoursesDataMap;
import org.sunbird.course.model.NewCourseData;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LatestCoursesAlertNotificationService {
    private static final CbExtLogger logger = new CbExtLogger(LatestCoursesAlertNotificationService.class.getName());
    private static final CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    private static EmailValidator emailValidator = EmailValidator.getInstance();

    public static boolean sendNewCourseEmail(List<CoursesDataMap> coursesDataMapList) {
        try {
            if (!coursesDataMapList.isEmpty() && coursesDataMapList.size() >= 2) {
                logger.info("Entering new courses email");
                logger.info(coursesDataMapList.toString());
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.NO_OF_COURSES, coursesDataMapList.size());
                for (int i = 0; i < coursesDataMapList.size() && i < 8; i++) {
                    int j = i + 1;
                    params.put(Constants.COURSE_KEYWORD + j, true);
                    params.put(Constants.COURSE_KEYWORD + j + Constants._URL, coursesDataMapList.get(i).getCourseUrl());
                    params.put(Constants.COURSE_KEYWORD + j + Constants.THUMBNAIL, coursesDataMapList.get(i).getThumbnail());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._NAME, coursesDataMapList.get(i).getCourseName());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._DURATION, ProjectUtil.convertSecondsToHrsAndMinutes(coursesDataMapList.get(i).getDuration()));
                    params.put(Constants.COURSE_KEYWORD + j + Constants._DESCRIPTION, coursesDataMapList.get(i).getDescription());
                }
                Boolean isEmailSentToConfigMailIds = sendEmailsToConfigBasedMailIds(params);
                Boolean isEmailSentToESMailIds = Boolean.FALSE;
                if (Boolean.parseBoolean(PropertiesCache.getInstance().getProperty(Constants.GET_USER_EMAIL_LIST_FROM_ES))) {
                    List<Map<String, Object>> excludeEmails = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.EXCLUDE_USER_EMAILS, null, Collections.singletonList(Constants.EMAIL));
                    List<String> desiredKeys = Collections.singletonList(Constants.EMAIL);
                    List<Object> excludeEmailList = excludeEmails.stream()
                            .flatMap(x -> desiredKeys.stream()
                                    .filter(x::containsKey)
                                    .map(x::get)
                            ).collect(Collectors.toList());
                    isEmailSentToESMailIds = fetchEmailIdsFromUserES(excludeEmailList);
                }
                return (isEmailSentToConfigMailIds && isEmailSentToESMailIds);
            }
        } catch (Exception e) {
            logger.info(String.format("Error in the new courses email module %s", e.getMessage()));
        }
        return false;
    }

    private static Boolean sendEmailsToConfigBasedMailIds(Map<String, Object> params) {
        try {
            List<String> mailList = new ArrayList<>();
            String extraEmails = PropertiesCache.getInstance().getProperty(Constants.RECIPIENT_NEW_COURSE_EMAILS);
            mailList.addAll(Arrays.asList(extraEmails.split(",", -1)));
            new NotificationUtil().sendNotification(mailList, params, PropertiesCache.getInstance().getProperty(Constants.SENDER_MAIL), PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST) + PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT), Constants.NEW_COURSES, Constants.NEW_COURSES_MAIL_SUBJECT);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error(e);

        }
        return Boolean.FALSE;
    }

    public void initiateLatestCourseAlertEmail() {
        if (Boolean.parseBoolean(PropertiesCache.getInstance().getProperty(Constants.SEND_LATEST_COURSES_ALERT))) {
            NewCourseData newCourseData = getLatestAddedCourses();
            if (newCourseData != null) {
                List<CoursesDataMap> coursesDataMapList = setCourseMap(newCourseData.getResult().getContent());
                boolean isEmailSent = sendNewCourseEmail(coursesDataMapList);
                if (isEmailSent)
                    updateEmailRecordInTheDatabase();
            }
        }
    }

    public NewCourseData getLatestAddedCourses() {
        try {
            Map<String, Object> lastUpdatedOn = new HashMap<>();
            LocalDate maxValue = LocalDate.now();
            lastUpdatedOn.put(Constants.MIN, calculateMinValue(maxValue));
            lastUpdatedOn.put(Constants.MAX, maxValue.toString());
            Map<String, Object> filters = new HashMap<>();
            filters.put(Constants.PRIMARY_CATEGORY, Collections.singletonList(Constants.COURSE));
            filters.put(Constants.CONTENT_TYPE_SEARCH, Collections.singletonList(Constants.COURSE));
            filters.put(Constants.LAST_UPDATED_ON, lastUpdatedOn);
            Map<String, Object> sortBy = new HashMap<>();
            sortBy.put(Constants.LAST_UPDATED_ON, Constants.DESCENDING_ORDER);
            String searchFields = PropertiesCache.getInstance().getProperty(Constants.SEARCH_FIELDS);
            Map<String, Object> request = new HashMap<>();
            request.put(Constants.FILTERS, filters);
            request.put(Constants.OFFSET, 0);
            request.put(Constants.LIMIT, 1000);
            request.put(Constants.SORT_BY, sortBy);
            request.put(Constants.FIELDS, Arrays.asList(searchFields.split(",", -1)));
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put(Constants.REQUEST, request);
            if (!lastUpdatedOn.get(Constants.MAX).toString().equalsIgnoreCase(lastUpdatedOn.get(Constants.MIN).toString())) {
                String url = PropertiesCache.getInstance().getProperty(Constants.KM_BASE_HOST) + PropertiesCache.getInstance().getProperty(Constants.CONTENT_SEARCH);
                Object o = fetchResultUsingPost(url, requestBody, new HashMap<>());
                return new ObjectMapper().convertValue(o, NewCourseData.class);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public static Boolean fetchEmailIdsFromUserES(List<Object> excludeEmailList) {
        try {
            int count = 1;
            int limit = 45;
            Map<String, Object> filters = new HashMap<>();
            filters.put(Constants.STATUS, 1);
            filters.put(Constants.IS_DELETED, Boolean.FALSE);
            String searchFields = PropertiesCache.getInstance().getProperty(Constants.EMAIL_SEARCH_FIELDS);
            Map<String, Object> request = new HashMap<>();
            request.put(Constants.FILTERS, filters);
            request.put(Constants.LIMIT, limit);
            request.put(Constants.FIELDS, Arrays.asList(searchFields.split(",", -1)));
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put(Constants.REQUEST, request);
            HashMap<String, String> headersValue = new HashMap<>();
            headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            headersValue.put(Constants.AUTHORIZATION, PropertiesCache.getInstance().getProperty(Constants.SB_API_KEY));
            Map<String, Object> response;
            String url = PropertiesCache.getInstance().getProperty(Constants.SB_URL) + PropertiesCache.getInstance().getProperty(Constants.USER_SEARCH_END_POINT);
            for (int offset = 0; offset < count; offset += limit) {
                List<String> emails = new ArrayList<>();
                request.put(Constants.OFFSET, offset);
                response = fetchResultUsingPost(url, requestBody, headersValue);
                if (response != null && Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
                    Map<String, Object> map = (Map<String, Object>) response.get(Constants.RESULT);
                    if (map.get(Constants.RESPONSE) != null) {
                        Map<String, Object> responseObj = (Map<String, Object>) map.get(Constants.RESPONSE);
                        List<Map<String, Object>> contents = (List<Map<String, Object>>) responseObj.get(Constants.CONTENT);
                        if (offset == 0) {
                            count = (int) responseObj.get(Constants.COUNT);
                        }
                        for (Map<String, Object> content : contents) {
                            if (content.containsKey(Constants.PROFILE_DETAILS)) {
                                Map<String, Object> profileDetails = (Map<String, Object>) content.get(Constants.PROFILE_DETAILS);
                                if (profileDetails.containsKey(Constants.PERSONAL_DETAILS)) {
                                    Map<String, Object> personalDetails = (Map<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
                                    if (personalDetails.containsKey(Constants.PRIMARY_EMAIL) && !excludeEmailList.contains(personalDetails.get(Constants.PRIMARY_EMAIL)) && !StringUtils.isEmpty(personalDetails.get(Constants.PRIMARY_EMAIL))) {
                                        if (emailValidator.isValid((String) personalDetails.get(Constants.PRIMARY_EMAIL))) {
                                            emails.add((String) personalDetails.get(Constants.PRIMARY_EMAIL));
                                        } else {
                                            logger.info("Invalid Email :" + personalDetails.get(Constants.PRIMARY_EMAIL));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                logger.info("List of Emails: " + emails);
                // new NotificationUtil().sendNotification(emailList, params, PropertiesCache.getInstance().getProperty(Constants.SENDER_MAIL), PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST) + PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT), Constants.NEW_COURSES, Constants.NEW_COURSES_MAIL_SUBJECT);
            }
        } catch (Exception e) {
            logger.error(e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private List<CoursesDataMap> setCourseMap(List<Content> coursesList) {
        List<CoursesDataMap> coursesDataMapList = new ArrayList<>();
        for (int i = 0; i < coursesList.size() && i < Integer.parseInt(PropertiesCache.getInstance().getProperty(Constants.NEW_COURSES_EMAIL_LIMIT)); i++) {
            try {
                String courseId = coursesList.get(i).getIdentifier();
                if (!StringUtils.isEmpty(coursesList.get(i).getIdentifier()) && !StringUtils.isEmpty(coursesList.get(i).getName()) && !StringUtils.isEmpty(coursesList.get(i).getPosterImage()) && !StringUtils.isEmpty(coursesList.get(i).getDuration())) {
                    CoursesDataMap coursesDataMap = new CoursesDataMap();
                    coursesDataMap.setCourseId(courseId);
                    coursesDataMap.setCourseName(ProjectUtil.firstLetterCapitalWithSingleSpace(coursesList.get(i).getName()));
                    coursesDataMap.setThumbnail(coursesList.get(i).getPosterImage());
                    coursesDataMap.setCourseUrl(PropertiesCache.getInstance().getProperty(Constants.COURSE_URL) + courseId);
                    coursesDataMap.setDescription(coursesList.get(i).getDescription());
                    coursesDataMap.setDuration(Integer.parseInt(coursesList.get(i).getDuration()));
                    coursesDataMapList.add(coursesDataMap);
                }
            } catch (Exception e) {
                logger.info(String.format("Error in setting Course Map %s", e.getMessage()));
            }
        }
        return coursesDataMapList;
    }

    public static Map<String, Object> fetchResultUsingPost(String uri, Object request, Map<String, String> headersValues) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Map<String, Object> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            if (!CollectionUtils.isEmpty(headersValues)) {
                headersValues.forEach(headers::set);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);
            response = new RestTemplate().postForObject(uri, entity, Map.class);
        } catch (HttpClientErrorException e) {
            try {
                response = (new ObjectMapper()).readValue(e.getResponseBodyAsString(),
                        new TypeReference<HashMap<String, Object>>() {
                        });
            } catch (Exception e1) {
                logger.error("Error received: " + e1.getMessage(), e1);
            }
            logger.error("Error received: " + e.getResponseBodyAsString(), e);
        }
        return response;
    }

    private String calculateMinValue(LocalDate maxValue) {
        String minValue = "";
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.EMAIL_TYPE, Constants.NEW_COURSES_EMAIL);
        List<Map<String, Object>> emailRecords = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.EMAIL_RECORD_TABLE, propertyMap, Collections.singletonList(Constants.LAST_SENT_DATE));
        if (!emailRecords.isEmpty()) {
            minValue = !StringUtils.isEmpty(emailRecords.get(0).get(Constants.LAST_SENT_DATE)) ? (String) emailRecords.get(0).get(Constants.LAST_SENT_DATE) : "";
        }
        if (StringUtils.isEmpty(minValue)) {
            minValue = maxValue.minusDays(Long.parseLong(PropertiesCache.getInstance().getProperty(Constants.NEW_COURSES_SCHEDULER_TIME_GAP)) / 24).toString();
        }
        return minValue;
    }

    private void updateEmailRecordInTheDatabase() {
        try {
            Map<String, Object> primaryKeyMap = new HashMap<>();
            primaryKeyMap.put(Constants.EMAIL_TYPE, Constants.NEW_COURSES_EMAIL);
            cassandraOperation.deleteRecord(Constants.KEYSPACE_SUNBIRD, Constants.EMAIL_RECORD_TABLE, primaryKeyMap);
            primaryKeyMap.put(Constants.LAST_SENT_DATE, LocalDate.now().toString());
            cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.EMAIL_RECORD_TABLE, primaryKeyMap);
        } catch (Exception e) {
            logger.info(String.format("Error while updating the database with the email record %s", e.getMessage()));
        }
    }
}