package org.sunbird.course.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.helper.cassandra.ServiceFactory;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.NotificationUtil;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.course.model.Content;
import org.sunbird.course.model.CoursesDataMap;
import org.sunbird.course.model.NewCourseData;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class LatestCoursesAlertNotificationService {

	@Autowired
	CbExtServerProperties serverProperties;

	@Autowired
	NotificationUtil notificationUtil;

	private static final CbExtLogger logger = new CbExtLogger(LatestCoursesAlertNotificationService.class.getName());
	private static final CassandraOperation cassandraOperation = ServiceFactory.getInstance();

	public void initiateLatestCourseAlertEmail() {
		logger.info("LatestCoursesAlertNotificationService:: initiateLatestCourseAlertEmail: Started");
		long duration = 0;
		if (serverProperties.isLatestCoursesAlertEnabled()) {
			try {
				long startTime = System.currentTimeMillis();
				NewCourseData newCourseData = getLatestAddedCourses();
				if (newCourseData != null && newCourseData.getResult().getContent().size() >= serverProperties
						.getLatestCoursesAlertContentMinLimit()) {
					if (sendNewCourseEmail(newCourseData)) {
						updateEmailRecordInTheDatabase();
					}
				} else {
					logger.info("There are no latest courses or number of latest courses are less than "
							+ serverProperties.getLatestCoursesAlertContentMinLimit());
				}
				duration = System.currentTimeMillis() - startTime;
			} catch (Exception e) {
				logger.error("Failed to run LatestCoursesAlertNotificationService. Exception: ", e);
			}
		}
		logger.info("LatestCoursesAlertNotificationService:: initiateLatestCourseAlertEmail: Completed. Time taken: "
				+ duration + " milli-seconds");
	}

	public boolean sendNewCourseEmail(NewCourseData newCourseData) {
		try {
			List<CoursesDataMap> coursesDataMapList = setCourseMap(newCourseData.getResult().getContent());
			logger.info("LatestCoursesAlertNotificationService::sendNewCourseEmail");
			Map<String, Object> params = new HashMap<>();
			params.put(Constants.NO_OF_COURSES, newCourseData.getResult().getCount());
			int i = 1;
			for (CoursesDataMap courseDataMap : coursesDataMapList) {
				params.put(Constants.COURSE_KEYWORD + i, true);
				params.put(Constants.COURSE_KEYWORD + i + Constants._URL, courseDataMap.getCourseUrl());
				params.put(Constants.COURSE_KEYWORD + i + Constants.THUMBNAIL, courseDataMap.getThumbnail());
				params.put(Constants.COURSE_KEYWORD + i + Constants._NAME, courseDataMap.getCourseName());
				params.put(Constants.COURSE_KEYWORD + i + Constants._DURATION,
						ProjectUtil.convertSecondsToHrsAndMinutes(courseDataMap.getDuration()));
				params.put(Constants.COURSE_KEYWORD + i + Constants._DESCRIPTION, courseDataMap.getDescription());
				i++;
			}

			Boolean isEmailSentToConfigMailIds = sendEmailsToConfigBasedMailIds(params);
			if (serverProperties.isLatestCoursesAlertSendToAllUser()) {
				List<Map<String, Object>> excludeEmails = cassandraOperation.getRecordsByProperties(
						Constants.SUNBIRD_KEY_SPACE_NAME, Constants.EXCLUDE_USER_EMAILS, null,
						Collections.singletonList(Constants.EMAIL));
				List<String> desiredKeys = Collections.singletonList(Constants.EMAIL);
				List<Object> excludeEmailList = excludeEmails.stream()
						.flatMap(x -> desiredKeys.stream().filter(x::containsKey).map(x::get))
						.collect(Collectors.toList());
				isEmailSentToConfigMailIds = fetchEmailIdsFromUserES(excludeEmailList, params);
			}
			return isEmailSentToConfigMailIds;
		} catch (Exception e) {
			logger.error(String.format("Error in the new courses email module %s", e.getMessage()), e);
		}
		return false;
	}

	private Boolean sendEmailsToConfigBasedMailIds(Map<String, Object> params) {
		try {
			if (!CollectionUtils.isEmpty(serverProperties.getLatestCoursesAlertUserEmailList())) {
				CompletableFuture.runAsync(() -> {
					notificationUtil.sendNotification(serverProperties.getLatestCoursesAlertUserEmailList(), params,
							serverProperties.getSenderEmailAddress(),
							serverProperties.getNotificationServiceHost()
									+ serverProperties.getNotificationEventEndpoint(),
							Constants.NEW_COURSES, serverProperties.getLatestCoursesAlertEmailSubject());
				});
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			logger.error(e);
		}
		return Boolean.FALSE;
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
			Map<String, Object> request = new HashMap<>();
			request.put(Constants.FILTERS, filters);
			request.put(Constants.OFFSET, 0);
			request.put(Constants.LIMIT, serverProperties.getLatestCoursesAlertContentLimit());
			request.put(Constants.SORT_BY, sortBy);
			request.put(Constants.FIELDS, serverProperties.getLatestCoursesAlertSearchContentFields());
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put(Constants.REQUEST, request);
			if (!lastUpdatedOn.get(Constants.MAX).toString()
					.equalsIgnoreCase(lastUpdatedOn.get(Constants.MIN).toString())) {
				String url = serverProperties.getKmBaseHost() + serverProperties.getKmBaseContentSearch();
				Object obj = fetchResultUsingPost(url, requestBody, new HashMap<>());
				return new ObjectMapper().convertValue(obj, NewCourseData.class);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.info("LatestCoursesAlertNotificationService:: getLatestAddedCourses: completed");
		}
		return null;
	}

	public Boolean fetchEmailIdsFromUserES(List<Object> excludeEmailList, Map<String, Object> params) {
		try {
			int count = 1;
			int limit = 45;
			Map<String, Object> filters = new HashMap<>();
			filters.put(Constants.STATUS, 1);
			filters.put(Constants.IS_DELETED, Boolean.FALSE);
			Map<String, Object> request = new HashMap<>();
			request.put(Constants.FILTERS, filters);
			request.put(Constants.LIMIT, limit);
			request.put(Constants.FIELDS, serverProperties.getLatestCoursesAlertSearchUserFields());
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put(Constants.REQUEST, request);
			HashMap<String, String> headersValue = new HashMap<>();
			headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			headersValue.put(Constants.AUTHORIZATION, serverProperties.getSbApiKey());
			Map<String, Object> response;

			String url = serverProperties.getSbUrl() + serverProperties.getUserSearchEndPoint();
			for (int offset = 0; offset < count; offset += limit) {
				List<String> emails = new ArrayList<>();
				request.put(Constants.OFFSET, offset);
				response = fetchResultUsingPost(url, requestBody, headersValue);
				if (response != null && Constants.OK.equalsIgnoreCase((String) response.get(Constants.RESPONSE_CODE))) {
					Map<String, Object> map = (Map<String, Object>) response.get(Constants.RESULT);
					if (map.get(Constants.RESPONSE) != null) {
						Map<String, Object> responseObj = (Map<String, Object>) map.get(Constants.RESPONSE);
						List<Map<String, Object>> contents = (List<Map<String, Object>>) responseObj
								.get(Constants.CONTENT);
						if (offset == 0) {
							count = (int) responseObj.get(Constants.COUNT);
						}
						for (Map<String, Object> content : contents) {
							if (content.containsKey(Constants.PROFILE_DETAILS)) {
								Map<String, Object> profileDetails = (Map<String, Object>) content
										.get(Constants.PROFILE_DETAILS);
								if (profileDetails.containsKey(Constants.PERSONAL_DETAILS)) {
									Map<String, Object> personalDetails = (Map<String, Object>) profileDetails
											.get(Constants.PERSONAL_DETAILS);

									if (MapUtils.isNotEmpty(personalDetails)) {
										String email = (String) personalDetails.get(Constants.PRIMARY_EMAIL);
										if (StringUtils.isNotBlank(email) && !excludeEmailList.contains(email)) {
											if (!CollectionUtils
													.isEmpty(serverProperties.getLatestCoursesAlertUserEmailList())
													&& !serverProperties.getLatestCoursesAlertUserEmailList()
															.contains(email))
												emails.add(email);
										}
									}
								}
							}
						}
					}
				}
				CompletableFuture.runAsync(() -> {
					notificationUtil.sendNotification(emails, params, serverProperties.getSenderEmailAddress(),
							serverProperties.getNotificationServiceHost()
									+ serverProperties.getNotificationEventEndpoint(),
							Constants.NEW_COURSES, serverProperties.getLatestCoursesAlertEmailSubject());
				});
			}
		} catch (Exception e) {
			logger.error(e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private List<CoursesDataMap> setCourseMap(List<Content> coursesList) {
		List<CoursesDataMap> coursesDataMapList = new ArrayList<>();
		for (int i = 0; i < coursesList.size(); i++) {
			try {
				Content content = coursesList.get(i);
				if (StringUtils.isNotBlank(content.getIdentifier()) && StringUtils.isNotBlank(content.getName())
						&& StringUtils.isNotBlank(content.getPosterImage())
						&& StringUtils.isNotBlank(content.getDuration())) {
					CoursesDataMap coursesDataMap = new CoursesDataMap();
					coursesDataMap.setCourseId(content.getIdentifier());
					coursesDataMap.setCourseName(ProjectUtil.firstLetterCapitalWithSingleSpace(content.getName()));
					coursesDataMap.setThumbnail(content.getPosterImage());
					coursesDataMap.setCourseUrl(serverProperties.getCourseLinkUrl() + content.getIdentifier());
					coursesDataMap.setDescription(content.getDescription());
					coursesDataMap.setDuration(Integer.parseInt(content.getDuration()));
					coursesDataMapList.add(coursesDataMap);
				}
			} catch (Exception e) {
				logger.info(String.format("Error in setting Course Map %s", e.getMessage()));
			}
		}
		return coursesDataMapList;
	}

	public static Map<String, Object> fetchResultUsingPost(String uri, Object request,
			Map<String, String> headersValues) {
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
		} catch (HttpClientErrorException hce) {
			try {
				response = (new ObjectMapper()).readValue(hce.getResponseBodyAsString(),
						new TypeReference<HashMap<String, Object>>() {
						});
			} catch (Exception e) {
				logger.error("Failed to process the error response from content search. Exception: ", e);
			}
			logger.error("Exception while fetching content Details. Response Body: " + hce.getResponseBodyAsString(),
					hce);
		}
		return response;
	}

	private String calculateMinValue(LocalDate maxValue) {
		String minValue = "";
		Map<String, Object> propertyMap = new HashMap<>();
		propertyMap.put(Constants.EMAIL_TYPE, Constants.NEW_COURSES_EMAIL);
		List<Map<String, Object>> emailRecords = cassandraOperation.getRecordsByProperties(
				Constants.SUNBIRD_KEY_SPACE_NAME, Constants.EMAIL_RECORD_TABLE, propertyMap,
				Collections.singletonList(Constants.LAST_SENT_DATE));
		if (!CollectionUtils.isEmpty(emailRecords)) {
			minValue = (String) emailRecords.get(0).get(Constants.LAST_SENT_DATE);
		}
		if (StringUtils.isEmpty(minValue)) {
			minValue = maxValue.minusDays(serverProperties.getLatestCoursesAlertSchedulerTimeGap() / 24).toString();
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