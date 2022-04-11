package org.sunbird.ratings.service;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.ratings.exception.ValidationException;
import org.sunbird.ratings.model.*;
import org.sunbird.ratings.responsecode.ResponseCode;
import org.sunbird.ratings.responsecode.ResponseMessage;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RatingServiceImpl implements RatingService {
    private final ObjectMapper mapper = new ObjectMapper();

    private CbExtLogger logger = new CbExtLogger(getClass().getName());
    private ValidationBody validationBody;

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    Producer kafkaProducer;

    @Value("${kafka.topics.parent.rating.event}")
    public String updateRatingTopicName;

    @Override
    public SBApiResponse getRatings(String activityId, String activityType, String userId) {
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_READ);

        try {
            validationBody = new ValidationBody();
            validationBody.setActivityId(activityId);
            validationBody.setActivityType(activityType);
            validationBody.setUserId(userId);
            validateRatingsInfo(validationBody, "getRating");


            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, activityId);
            request.put(Constants.ACTIVITY_TYPE, activityType);
            request.put(Constants.RATINGS_USER_ID, userId);
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, existingDataList);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errMsg = Constants.NO_RATING_EXCEPTION_MESSAGE + activityId + ", activity_Type: " + activityType;
                response.put(Constants.MESSAGE, Constants.FAILED);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }

    @Override
    public SBApiResponse getRatingSummary(String activityId, String activityType) {
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_SUMMARY);

        try {
            validationBody = new ValidationBody();
            validationBody.setActivityId(activityId);
            validationBody.setActivityType(activityType);

            validateRatingsInfo(validationBody, "getSummary");

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, activityId);
            request.put(Constants.ACTIVITY_TYPE, activityType);

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_SUMMARY, request, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, existingDataList);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errMsg = Constants.NO_REVIEW_EXCEPTION_MESSAGE + activityId + ", activityType: " + activityType;
                response.put(Constants.MESSAGE, Constants.FAILED);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }

    @Override
    public SBApiResponse upsertRating(RequestRating requestRating) {
        UUID timeBasedUuid = UUIDs.timeBased();
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_UPDATE);

        RatingMessage ratingMessage;

        try {
            validationBody = new ValidationBody();
            validationBody.setRequestRating(requestRating);

            validateRatingsInfo(validationBody, "upsert");

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, requestRating.getActivityId());
            request.put(Constants.ACTIVITY_TYPE, requestRating.getActivityType());
            request.put(Constants.RATINGS_USER_ID, requestRating.getUserId());


            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);

            if (!CollectionUtils.isEmpty(existingDataList)) {

                Map<String, Object> updateRequest = new HashMap<>();
                updateRequest.put(Constants.RATING, requestRating.getRating());
                updateRequest.put(Constants.REVIEW, requestRating.getReview());
                updateRequest.put(Constants.UPDATED_ON, timeBasedUuid);

                Map<String, Object> prevInfo = existingDataList.get(0);
                cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_RATINGS, updateRequest,
                        request);
                ratingMessage = new RatingMessage("ratingUpdate", requestRating.getActivityId(), requestRating.getActivityType(),
                        requestRating.getUserId(), String.valueOf((prevInfo.get("createdon"))));

                ratingMessage.setPrevValues(processEventMessage(String.valueOf(prevInfo.get("createdon")),
                        (Float) prevInfo.get("rating"), (String) prevInfo.get("review")));
                ratingMessage.setUpdatedValues(processEventMessage(String.valueOf(updateRequest.get(Constants.UPDATED_ON)),
                        requestRating.getRating(), requestRating.getReview()));
            } else {
                request.put(Constants.CREATED_ON, timeBasedUuid);
                request.put(Constants.RATING, requestRating.getRating());
                request.put(Constants.REVIEW, requestRating.getReview());
                request.put(Constants.UPDATED_ON, timeBasedUuid);

                cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_RATINGS, request);

                ratingMessage = new RatingMessage("ratingAdd", requestRating.getActivityId(), requestRating.getActivityType(),
                        requestRating.getUserId(), String.valueOf(timeBasedUuid));

                ratingMessage.setUpdatedValues(processEventMessage(String.valueOf(request.get(Constants.CREATED_ON)),
                        requestRating.getRating(), requestRating.getReview()));
                response.put(Constants.DATA, request);

            }
            response.setResponseCode(HttpStatus.OK);
            response.getParams().setStatus(Constants.SUCCESSFUL);

            kafkaProducer.push(updateRatingTopicName, ratingMessage);
        } catch (ValidationException ex) {
            logger.error(ex);
            return processExceptionBody(response, ex, "", HttpStatus.BAD_REQUEST);
        } catch (KafkaException ex) {
            logger.error(ex);
            return processExceptionBody(response, ex, Constants.KAFKA_RATING_EXCEPTION_MESSAGE, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error(ex);
            String errMsg = Constants.RATING_GENERIC_EXCEPTION_MESSAGE;
            return processExceptionBody(response, ex, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Override
    public SBApiResponse ratingLookUp(LookupRequest lookupRequest) {
        List<String> listOfUserId = new ArrayList<>();
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_LOOKUP);

        try {
            validationBody = new ValidationBody();
            validationBody.setLookupRequest(lookupRequest);
            validateRatingsInfo(validationBody, "lookup");

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, lookupRequest.getActivityId());
            request.put(Constants.ACTIVITY_TYPE, lookupRequest.getActivityType());

            if (lookupRequest.getRating() != null) {
                request.put(Constants.RATING, lookupRequest.getRating());
            }

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithPagination(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_LOOKUP, request, null, lookupRequest.getLimit(), lookupRequest.getUpdateOn());
            if (!CollectionUtils.isEmpty(existingDataList)) {

                for (int i = 0; i < existingDataList.size(); i++) {
                    Map<String, Object> lookupData = existingDataList.get(i);
                    listOfUserId.add(lookupData.get(Constants.RATINGS_USER_ID).toString());
                }
                Map<String, Object> userRequest = new HashMap<>();
                userRequest.put(Constants.USERID, listOfUserId);
                List<String> fields = new ArrayList<>();
                fields.add(Constants.USERID);
                fields.add(Constants.FIRSTNAME);
                fields.add(Constants.LASTNAME);
                List<Map<String, Object>> existingUserList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                        Constants.TABLE_USER, userRequest, fields);

                Stream<Map<String, Object>> userStream = existingUserList.stream();
                List<Map<String, Object>> userSortedList = userStream.sorted(RatingServiceImpl::comparator).collect(Collectors.toList());
                Stream<Map<String, Object>> ratingLookupStream = existingDataList.stream();
                List<Map<String, Object>> ratingLookupSortedList = ratingLookupStream.sorted(RatingServiceImpl::comparator).collect(Collectors.toList());

                List<LookupResponse> listOfLookupResponse = new ArrayList<>();
                for (int k = 0; k < ratingLookupSortedList.size(); k++) {
                    listOfLookupResponse.add(new LookupResponse(ratingLookupSortedList.get(k).get(Constants.LOOKUP_ACTIVITY_ID).toString(),
                            ratingLookupSortedList.get(k).get(Constants.REVIEW).toString(),
                            ratingLookupSortedList.get(k).get(Constants.RATING).toString(),
                            ratingLookupSortedList.get(k).get(Constants.UPDATED_ON).toString(),
                            ratingLookupSortedList.get(k).get(Constants.LOOKUP_ACTIVITY_TYPE).toString(),
                            ratingLookupSortedList.get(k).get(Constants.USER_ID).toString(),
                            userSortedList.get(k).get(Constants.USER_FIRST_NAME).toString(),
                            userSortedList.get(k).get(Constants.USER_LAST_NAME).toString()));
                }

                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, listOfLookupResponse);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errMsg = Constants.NO_RATING_EXCEPTION_MESSAGE + lookupRequest.getActivityId() + ", activityType: " + lookupRequest.getActivityType();
                response.put(Constants.MESSAGE, Constants.FAILED);
                response.getParams().setErrmsg(errMsg);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (ValidationException ex) {
            logger.error(ex);
            return processExceptionBody(response, ex, "", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e);
        }
        return response;

    }

    public static int comparator(Map<String, Object> map1, Map<String, Object> map2) {
        if (map1 == null && map2 == null)
            return 0;

        if (map1 == null || map2 == null) {
            throw new NullPointerException();
        }
        String name1 = "";
        String name2 = "";
        if (map1.get("id") != null) {
            name1 = (String) map1.get("id");
            name2 = (String) map2.get("id");
        }
        if (map1.get("userId") != null) {
            name1 = (String) map1.get("userId");
            name2 = (String) map2.get("userId");
        }
        int c = name1.compareTo(name2);
        return c;
    }

    public RatingMessage.UpdatedValues processEventMessage(String date, Float rating, String review) {
        RatingMessage.UpdatedValues values = new RatingMessage.UpdatedValues();
        values.setUpdatedOn(date);
        values.setRating(rating);
        values.setReview(review);
        return values;
    }

    private void validateRatingsInfo(ValidationBody validationBody, String flag) throws Exception {

        List<String> errObjList = new ArrayList<>();

        if (flag == Constants.RATING_UPSERT_OPERATION) {
            if (StringUtils.isEmpty(validationBody.getRequestRating().getActivityId())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty(validationBody.getRequestRating().getActivityType())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty((String.valueOf(validationBody.getRequestRating().getRating())))
                    || validationBody.getRequestRating().getRating() < 1
                    || validationBody.getRequestRating().getRating() > 5) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT + ResponseMessage.Message.INVALID_RATING);
            }
            if (StringUtils.isEmpty(validationBody.getRequestRating().getReview())
                    || (!Pattern.matches("^[A-Za-z0-9, ]++$", validationBody.getRequestRating().getReview()))) {
                errObjList.add(ResponseMessage.Message.INVALID_REVIEW);
            }
            if (StringUtils.isEmpty(validationBody.getRequestRating().getUserId())) {
                errObjList.add(ResponseMessage.Message.INVALID_USER);
            }
        } else if (flag == Constants.RATING_LOOKUP_RATING_OPERATION) {

            if (StringUtils.isEmpty(validationBody.getLookupRequest().getActivityId())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty(validationBody.getLookupRequest().getActivityType())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if ((validationBody.getLookupRequest().getRating() != null) &&(validationBody.getLookupRequest().getRating() < 1.0
                    || validationBody.getLookupRequest().getRating() > 5.0)) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT + ResponseMessage.Message.INVALID_RATING);
            }
            if (validationBody.getLookupRequest().getLimit() < 1) {
                errObjList.add(ResponseMessage.Message.INVALID_LIMIT);
            }
        } else if (flag == Constants.RATING_GET_OPERATION || flag == Constants.RATING_SUMMARY_OPERATION) {
            if (StringUtils.isEmpty(validationBody.getActivityId())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty(validationBody.getActivityType())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }

            if (flag == Constants.RATING_GET_OPERATION && StringUtils.isEmpty(validationBody.getUserId())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
        }
        if (!CollectionUtils.isEmpty(errObjList)) {
            throw new ValidationException(errObjList, ResponseCode.BAD_REQUEST.getResponseCode());
        }

    }

    public SBApiResponse processExceptionBody(SBApiResponse response, Exception ex,
                                              String exceptionMessage, HttpStatus status) {
        String errMsg = exceptionMessage + ex.getMessage();
        logger.info("Exception: " + errMsg);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(status);
        return response;
    }

}
