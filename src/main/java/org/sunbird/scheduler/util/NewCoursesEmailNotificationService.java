package org.sunbird.scheduler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.validator.EmailValidator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.helper.cassandra.ServiceFactory;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.NotificationUtil;
import org.sunbird.common.util.PropertiesCache;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.scheduler.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewCoursesEmailNotificationService implements Runnable {
    private static final CbExtLogger logger = new CbExtLogger(SchedulerManager.class.getName());
    private final CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    private EmailValidator emailValidator = EmailValidator.getInstance();

    public static boolean sendNewCourseEmail(List<CoursesDataMap> coursesDataMapList, List<String> mailList, int noOfCourses) {
        try {
            if (!coursesDataMapList.isEmpty()) {
                logger.info("Entering new courses email");
                logger.info(coursesDataMapList.toString());
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.NO_OF_COURSES, noOfCourses);
                for (int i = 0; i < coursesDataMapList.size(); i++) {
                    int j = i + 1;
                    params.put(Constants.COURSE_KEYWORD + j, true);
                    params.put(Constants.COURSE_KEYWORD + j + Constants._URL, coursesDataMapList.get(i).getCourseUrl());
                    params.put(Constants.COURSE_KEYWORD + j + Constants.THUMBNAIL, coursesDataMapList.get(i).getThumbnail());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._NAME, coursesDataMapList.get(i).getCourseName());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._DURATION, coursesDataMapList.get(i).getDuration());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._DESCRIPTION, coursesDataMapList.get(i).getDescription());
                }
//                String extraEmails = PropertiesCache.getInstance().getProperty(Constants.RECIPIENT_NEW_COURSE_EMAILS);
//                mailList.addAll(Arrays.asList(extraEmails.split(",", -1)));
                int chunkSize = 20;
                List<String> emailList;
                for (int i = 0; i < mailList.size(); i += chunkSize) {
                    if ((i + chunkSize) >= mailList.size()) {
                        emailList = mailList.subList(i, mailList.size());
                    } else {
                        emailList = mailList.subList(i, i + chunkSize);
                    }
                    logger.info(emailList.toString());
                   // new NotificationUtil().sendNotification(Arrays.asList("nitin.raj@tarento.com", "juhi.agarwal@tarento.com"), params, "support@igot-dev.in", PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST) + PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT), Constants.NEW_COURSES, Constants.NEW_COURSES_MAIL_SUBJECT);

                }
                new NotificationUtil().sendNotification(Arrays.asList("nitin.raj@tarento.com", "juhi.agarwal@tarento.com"), params, "support@igot-dev.in", PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST) + PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT), Constants.NEW_COURSES, Constants.NEW_COURSES_MAIL_SUBJECT);

                return true;
            }
        } catch (Exception e) {
            logger.info(String.format("Error in the new courses email module %s", e.getMessage()));
        }
        return false;
    }

    @Override
    public void run() {
        newCourses();
    }

    public void newCourses() {
        NewCourseData newCourseData = getLatestAddedCourses();
        if (newCourseData != null) {
            List<CoursesDataMap> coursesDataMapList = setCourseMap(newCourseData);
            int noOfCourses = coursesDataMapList.size();
            List<String> mailList = getFinalMailingList();
            boolean isEmailSent = sendNewCourseEmail(coursesDataMapList, mailList, noOfCourses);
            if (isEmailSent)
                updateEmailRecordInTheDatabase();
        }
    }

    public NewCourseData getLatestAddedCourses() {
        try {
            RequestData requestData = new RequestData();
            Filters filter = new Filters();
            filter.setPrimaryCategory(Collections.singletonList(Constants.COURSE));
            filter.setContentType(Collections.singletonList(Constants.COURSE));
            LastUpdatedOn lastUpdatedOn = new LastUpdatedOn();
            LocalDate maxValue = LocalDate.now();
            lastUpdatedOn.setMin(calculateMinValue(maxValue));
            lastUpdatedOn.setMax(maxValue.toString());
            if (!lastUpdatedOn.getMax().equalsIgnoreCase(lastUpdatedOn.getMin())) {
                filter.setLastUpdatedOn(lastUpdatedOn);
                Request request = new Request();
                request.setFilters(filter);
                request.setOffset(0);
                request.setLimit(Integer.parseInt(PropertiesCache.getInstance().getProperty(Constants.NEW_COURSES_EMAIL_LIMIT)));
                SortBy sortBy = new SortBy();
                sortBy.setLastUpdatedOn(Constants.DESCENDING_ORDER);
                request.setSortBy(sortBy);
                requestData.setRequest(request);
                String searchFields = PropertiesCache.getInstance().getProperty(Constants.SEARCH_FIELDS);
                requestData.getRequest().setFields(Arrays.asList(searchFields.split(",", -1)));
                Map requestBody = new ObjectMapper().convertValue(requestData, Map.class);
                String url = PropertiesCache.getInstance().getProperty(Constants.KM_BASE_HOST) + PropertiesCache.getInstance().getProperty(Constants.CONTENT_SEARCH);
                Object o = fetchResultUsingPost(url, requestBody, new HashMap<>());
                return new ObjectMapper().convertValue(o, NewCourseData.class);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    private List<CoursesDataMap> setCourseMap(NewCourseData newCourseData) {
        List<Content> coursesList = newCourseData.getResult().getContent();
        List<CoursesDataMap> coursesDataMapList = new ArrayList<>();
        for (Content course : coursesList) {
            try {
                String courseId = course.getIdentifier();
                if (!StringUtils.isEmpty(course.getIdentifier()) && !StringUtils.isEmpty(course.getName()) && !StringUtils.isEmpty(course.getPosterImage()) && !StringUtils.isEmpty(course.getDuration())) {
                    CoursesDataMap coursesDataMap = new CoursesDataMap();
                    coursesDataMap.setCourseId(courseId);
                    coursesDataMap.setCourseName(firstLetterCapitalWithSingleSpace(course.getName()));
                    coursesDataMap.setThumbnail(course.getPosterImage());
                    coursesDataMap.setCourseUrl(PropertiesCache.getInstance().getProperty(Constants.COURSE_URL) + courseId);
                    coursesDataMap.setDescription(course.getDescription());
                    coursesDataMap.setDuration(Integer.parseInt(course.getDuration()));
                    coursesDataMapList.add(coursesDataMap);
                }
            } catch (Exception e) {
                logger.info(String.format("Error in setting Course Map %s", e.getMessage()));
            }
        }
        return coursesDataMapList;
    }

    public String firstLetterCapitalWithSingleSpace(final String words) {
        return Stream.of(words.trim().split("\\s"))
                .filter(word -> word.length() > 0)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public Map<String, Object> fetchResultUsingPost(String uri, Object request, Map<String, String> headersValues) {
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
            logger.info(String.format("Error while hitting the search api %s", e.getMessage()));
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
            minValue = maxValue.minusDays(Long.valueOf(PropertiesCache.getInstance().getProperty(Constants.NEW_COURSES_SCHEDULER_TIME_GAP)) / 24).toString();
        }
        return minValue;
    }

    private List<String> getFinalMailingList() {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.IS_DELETED, Boolean.FALSE);
        propertyMap.put(Constants.STATUS, 1);
        List<String> finalEmailList = new ArrayList<>();
        List<Map<String, Object>> userDetails = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_USER, propertyMap, Arrays.asList(Constants.ID, Constants.PROFILE_DETAILS_KEY));
        List<Map<String, Object>> excludeEmails = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.EXCLUDE_USER_EMAILS, null, Collections.singletonList(Constants.EMAIL));
        List<String> desiredKeys = Collections.singletonList(Constants.EMAIL);
        List<Object> excludeEmailList = excludeEmails.stream()
                .flatMap(x -> desiredKeys.stream()
                        .filter(x::containsKey)
                        .map(x::get)
                ).collect(Collectors.toList());
        for (Map<String, Object> userDetail : userDetails) {
            try {
                if (userDetail.get(Constants.PROFILE_DETAILS_KEY) != null) {
                    Map profileDetails = new ObjectMapper().readValue((String) userDetail.get(Constants.PROFILE_DETAILS_KEY), Map.class);
                    if (!profileDetails.isEmpty()) {
                        Map<String, Object> personalDetailsMap = (Map<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
                        if (!personalDetailsMap.isEmpty() && personalDetailsMap.get(Constants.PRIMARY_EMAIL) != null && !excludeEmailList.contains(personalDetailsMap.get(Constants.PRIMARY_EMAIL))) {
                            String email = (String) personalDetailsMap.get(Constants.PRIMARY_EMAIL);
                            if (emailValidator.isValid(email))
                                finalEmailList.add(email);
                        }
                    }
                }
            } catch (Exception e) {
                logger.info(String.format("Error in curating the final email list %s", e.getMessage()));
            }
        }
        return finalEmailList;
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