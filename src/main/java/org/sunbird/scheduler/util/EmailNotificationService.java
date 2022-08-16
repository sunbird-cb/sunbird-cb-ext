package org.sunbird.scheduler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.helper.cassandra.ServiceFactory;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.NotificationUtil;
import org.sunbird.common.util.PropertiesCache;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.scheduler.model.CourseDetails;
import org.sunbird.scheduler.model.IncompleteCourses;
import org.sunbird.scheduler.model.UserCourseProgressDetails;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EmailNotificationService implements Runnable {
    private static final CbExtLogger logger = new CbExtLogger(SchedulerManager.class.getName());
    private final CassandraOperation cassandraOperation = ServiceFactory.getInstance();
    Map<String, CourseDetails> courseIdAndCourseNameMap = new HashMap<>();

    public static void sendIncompleteCourseEmail(Map.Entry<String, UserCourseProgressDetails> userCourseProgressDetailsEntry) {
        try {
            if (!StringUtils.isEmpty(userCourseProgressDetailsEntry.getValue().getEmail()) && userCourseProgressDetailsEntry.getValue().getIncompleteCourses().size() > 0) {
                Map<String, Object> params = new HashMap<>();
                for (int i = 0; i < userCourseProgressDetailsEntry.getValue().getIncompleteCourses().size(); i++) {
                    int j=i+1;
                    params.put(Constants.COURSE_KEYWORD + j, true);
                    params.put(Constants.COURSE_KEYWORD + j + Constants._URL, userCourseProgressDetailsEntry.getValue().getIncompleteCourses().get(i).getCourseUrl());
                    params.put(Constants.COURSE_KEYWORD + j + Constants.THUMBNAIL, userCourseProgressDetailsEntry.getValue().getIncompleteCourses().get(i).getThumbnail());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._NAME, userCourseProgressDetailsEntry.getValue().getIncompleteCourses().get(i).getCourseName());
                    params.put(Constants.COURSE_KEYWORD + j + Constants._DURATION, String.valueOf(userCourseProgressDetailsEntry.getValue().getIncompleteCourses().get(i).getCompletionPercentage()));

                }
                new NotificationUtil().sendNotification(Collections.singletonList(userCourseProgressDetailsEntry.getValue().getEmail()), params, PropertiesCache.getInstance().getProperty(Constants.SENDER_MAIL), PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_HOST) + PropertiesCache.getInstance().getProperty(Constants.NOTIFICATION_ENDPOINT));
            }
        } catch (Exception e) {
            logger.info(String.format("Error in the incomplete courses email module %s", e.getMessage()));
        }
    }

    @Override
    public void run() {
        incompleteCourses();
    }

    public void incompleteCourses() {
        try {
            Date date = new Date(new Date().getTime() - Integer.parseInt(PropertiesCache.getInstance().getProperty(Constants.LAST_ACCESS_TIME_GAP)));
            List<Map<String, Object>> userCoursesList = cassandraOperation.searchByWhereClause(Constants.SUNBIRD_COURSES_KEY_SPACE_NAME, Constants.USER_CONTENT_CONSUMPTION, Arrays.asList(Constants.RATINGS_USER_ID, Constants.BATCH_ID_, Constants.COURSE_ID_, Constants.COMPLETION_PERCENTAGE_, Constants.LAST_ACCESS_TIME), date);
            if (!CollectionUtils.isEmpty(userCoursesList)) {
                fetchCourseIdsAndSetCourseNameAndThumbnail(userCoursesList);
                Map<String, UserCourseProgressDetails> userCourseMap = new HashMap<>();
                setUserCourseMap(userCoursesList, userCourseMap);
                getAndSetUserEmail(userCourseMap);
                for (Map.Entry<String, UserCourseProgressDetails> userCourseProgressDetailsEntry : userCourseMap.entrySet()) {
                    sendIncompleteCourseEmail(userCourseProgressDetailsEntry);
                }
            }
        } catch (IOException e) {
            logger.info(String.format("Error in the scheduler to send User Progress emails %s", e.getMessage()));
        }
    }

    private void fetchCourseIdsAndSetCourseNameAndThumbnail(List<Map<String, Object>> userCoursesList) throws IOException {
        Set<Object> courseIds = new HashSet<>();
        List<String> desiredKeys = Collections.singletonList(Constants.COURSE_ID);
        courseIds = userCoursesList.stream()
                .flatMap(x -> desiredKeys.stream()
                        .filter(x::containsKey)
                        .distinct()
                        .map(x::get)
                ).collect(Collectors.toSet());
        getAndSetCourseName(courseIds);
    }

    private void getAndSetCourseName(Set<Object> courseIds) {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.IDENTIFIER, courseIds.stream().collect(Collectors.toList()));
        List<Map<String, Object>> coursesDataList = cassandraOperation.getRecordsByProperties(Constants.DEV_HIERARCHY_STORE, Constants.CONTENT_HIERARCHY, propertyMap, Arrays.asList(Constants.IDENTIFIER, Constants.HIERARCHY));
        for (Map<String, Object> courseData : coursesDataList) {
            if (courseData.get(Constants.IDENTIFIER) != null && courseData.get(Constants.HIERARCHY) != null && courseIdAndCourseNameMap.get(courseData.get(Constants.IDENTIFIER)) == null) {
                if (courseData.get(Constants.IDENTIFIER) != null && courseData.get(Constants.HIERARCHY) != null && courseIdAndCourseNameMap.get(courseData.get(Constants.IDENTIFIER)) == null) {
                    String hierarchy = courseData.get(Constants.HIERARCHY).toString();
                    Map<String, Object> courseDataMap =  new Gson().fromJson(hierarchy, Map.class);
                    CourseDetails courseDetail = new CourseDetails();
                    if (courseDataMap.get(Constants.NAME) != null) {
                        courseDetail.setCourseName((String) courseDataMap.get(Constants.NAME));
                    }
                    if (courseDataMap.get(Constants.POSTER_IMAGE) != null) {
                        courseDetail.setThumbnail((String) courseDataMap.get(Constants.POSTER_IMAGE));
                    }
                    courseIdAndCourseNameMap.put((String) courseData.get(Constants.IDENTIFIER), courseDetail);
                }
            }
        }
    }

    private void getAndSetUserEmail(Map<String, UserCourseProgressDetails> userCourseMap) {
        ArrayList<String> userIds = new ArrayList<>(userCourseMap.keySet());
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.ID, userIds);
        propertyMap.put(Constants.IS_DELETED, Boolean.FALSE);
        propertyMap.put(Constants.STATUS, 1);
        List<Map<String, Object>> userDetails = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_USER, propertyMap, Arrays.asList(Constants.ID, Constants.PROFILE_DETAILS_KEY));
        List<Map<String, Object>> excludeEmails = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.EXCLUDE_USER_EMAILS, null, Collections.singletonList(Constants.EMAIL));
        List<String> desiredKeys = Collections.singletonList(Constants.EMAIL);
        List<Object> excludeEmailsList = excludeEmails.stream()
                .flatMap(x -> desiredKeys.stream()
                        .filter(x::containsKey)
                        .map(x::get)
                ).collect(Collectors.toList());

        for (Map<String, Object> userDetail : userDetails) {
            String profileDetails;
            try {
                if (userDetail.get(Constants.PROFILE_DETAILS_KEY) != null && userCourseMap.get(userDetail.get(Constants.ID)) != null) {
                    profileDetails = (String) userDetail.get(Constants.PROFILE_DETAILS_KEY);
                    HashMap<String, Object> hashMap = new ObjectMapper().readValue(profileDetails, HashMap.class);
                    HashMap<String, Object> personalDetailsMap = (HashMap<String, Object>) hashMap.get(Constants.PERSONAL_DETAILS);
                    if (personalDetailsMap.get(Constants.PRIMARY_EMAIL) != null && !excludeEmailsList.contains((String) personalDetailsMap.get(Constants.PRIMARY_EMAIL))) {
                        userCourseMap.get(userDetail.get(Constants.ID)).setEmail((String) personalDetailsMap.get(Constants.PRIMARY_EMAIL));
                    }
                }
            } catch (Exception e) {
                logger.info(String.format("Error in get and set user email %s", e.getMessage()));
            }
        }
    }

    private void setUserCourseMap(List<Map<String, Object>> userCoursesList, Map<String, UserCourseProgressDetails> userCourseMap) {
        for (Map<String, Object> userCourse : userCoursesList) {
            try {
                String courseId = (String) userCourse.get(Constants.COURSE_ID);
                String batchId = (String) userCourse.get(Constants.BATCH_ID);
                String userid = (String) userCourse.get(Constants.USER_ID);
                if (courseId != null && batchId != null && courseIdAndCourseNameMap.get(courseId) != null && courseIdAndCourseNameMap.get(courseId).getThumbnail() != null) {
                    IncompleteCourses i = new IncompleteCourses();
                    i.setCourseId(courseId);
                    i.setCourseName(courseIdAndCourseNameMap.get(courseId).getCourseName());
                    i.setCompletionPercentage((Float) userCourse.get(Constants.COMPLETION_PERCENTAGE));
                    i.setLastAccessedDate((Date) userCourse.get(Constants.LAST_ACCESS_TIME));
                    i.setBatchId(batchId);
                    i.setThumbnail(courseIdAndCourseNameMap.get(courseId).getThumbnail());
                    i.setCourseUrl(PropertiesCache.getInstance().getProperty(Constants.COURSE_URL) + courseId + PropertiesCache.getInstance().getProperty(Constants.OVERVIEW_BATCH_ID) + batchId);
                    if (userCourseMap.get(userid) != null) {
                        if (userCourseMap.get(userid).getIncompleteCourses().size() < 3) {
                            userCourseMap.get(userid).getIncompleteCourses().add(i);
                            if (userCourseMap.get(userid).getIncompleteCourses().size() == 3) {
                                userCourseMap.get(userid).getIncompleteCourses().sort(Comparator.comparing(IncompleteCourses::getLastAccessedDate).reversed());
                            }
                        }
                    } else {
                        UserCourseProgressDetails user = new UserCourseProgressDetails();
                        List<IncompleteCourses> incompleteCourses = new ArrayList<>();
                        incompleteCourses.add(i);
                        user.setIncompleteCourses(incompleteCourses);
                        userCourseMap.put(userid, user);
                    }
                }
            } catch (Exception e) {
                logger.info(String.format("Error in set User Course Map %s", e.getMessage()));
            }
        }
    }

//    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
//    {
//        Map<Object, Boolean> map = new ConcurrentHashMap<>();
//        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
//    }
}