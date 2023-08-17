package org.sunbird.ratings.service;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.ratings.exception.ValidationException;
import org.sunbird.ratings.model.*;
import org.sunbird.ratings.responsecode.ResponseCode;
import org.sunbird.ratings.responsecode.ResponseMessage;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

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
        UUID timeBasedUuid;

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
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                Map<String, Object> ratingData = existingDataList.get(0);
                RatingModelInfo ratingModelInfo = new RatingModelInfo();
                ratingModelInfo.setActivityId((String) ratingData.get(Constants.ACTIVITY_ID));
                ratingModelInfo.setReview((String) ratingData.get(Constants.REVIEW));
                ratingModelInfo.setRating((Float) ratingData.get(Constants.RATING));
                ratingModelInfo.setComment(ratingData.get(Constants.COMMENT)!=null ?(String) ratingData.get(Constants.COMMENT) : null);
                ratingModelInfo.setCommentBy(ratingData.get(Constants.COMMENT_BY)!=null ?(String) ratingData.get(Constants.COMMENT_BY) : null);

                if(ratingData.get(Constants.COMMENT_UPDATED_ON)!=null){
                    UUID commentupdatedOn = (UUID) ratingData.get(Constants.COMMENT_UPDATED_ON);
                    Long CommentUpdatedTime = (commentupdatedOn.timestamp() - 0x01b21dd213814000L) / 10000L;
                    ratingModelInfo.setCommentUpdatedOn(new Timestamp(CommentUpdatedTime));
                }

                timeBasedUuid = (UUID) ratingData.get(Constants.UPDATED_ON);
                Long updatedTime = (timeBasedUuid.timestamp() - 0x01b21dd213814000L) / 10000L;
                ratingModelInfo.setUpdatedOn(new Timestamp(updatedTime));
                ratingModelInfo.setActivityType((String) ratingData.get(Constants.ACTIVITY_TYPE));
                ratingModelInfo.setUserId((String) ratingData.get(Constants.USER_ID));
                timeBasedUuid = (UUID) ratingData.get(Constants.CREATED_ON);
                Long createdTime = (timeBasedUuid.timestamp() - 0x01b21dd213814000L) / 10000L;
                ratingModelInfo.setCreatedOn(new Timestamp(createdTime));
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, ratingModelInfo);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, null);
                response.setResponseCode(HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error(e);
            processExceptionBody(response, e, "", HttpStatus.INTERNAL_SERVER_ERROR);
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
            List<SummaryModel.latestReviews> latest50Reviews = new ArrayList<>();
            validateRatingsInfo(validationBody, "getSummary");

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, activityId);
            request.put(Constants.ACTIVITY_TYPE, activityType);

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_SUMMARY, request, null);

            if (!CollectionUtils.isEmpty(existingDataList)) {
                Map<String, Object> summaryData = existingDataList.get(0);
                if(summaryData.get(Constants.LATEST50REVIEWS)!=null) {
                    String reviews = (String) summaryData.get(Constants.LATEST50REVIEWS);
                    JsonNode actualObj = mapper.readTree(reviews);

                    List<String> userList = new ArrayList<>();
                    Map<String, SummaryNodeModel> reviewMap = new HashMap<>();

                    for (JsonNode jsonNode : actualObj) {
                        final SummaryNodeModel summaryModel = mapper.convertValue(jsonNode, SummaryNodeModel.class);
                        reviewMap.put(summaryModel.getUser_id(), summaryModel);
                        userList.add(jsonNode.get("user_id").asText());
                    }

                    Map<String, Object> userRequest = new HashMap<>();
                    userRequest.put(Constants.USERID, userList);
                    List<String> fields = new ArrayList<>();
                    fields.add(Constants.ID);
                    fields.add(Constants.FIRSTNAME);

                    Map<String, Object> existingUserList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
                            Constants.TABLE_USER, userRequest, fields, Constants.ID);

                    for (String user : userList) {
                        final ObjectMapper mapper = new ObjectMapper();
                        final UserModel userModel = mapper.convertValue(existingUserList.get(user), UserModel.class);
                        final SummaryNodeModel summaryNodeModel = mapper.convertValue(reviewMap.get(user), SummaryNodeModel.class);
                        Long updatedTime = ((UUID.fromString(summaryNodeModel.getDate()).timestamp() - 0x01b21dd213814000L)) / 10000L;

                        latest50Reviews.add(new SummaryModel.latestReviews(Constants.REVIEW,
                                userModel.getId(),
                                new Timestamp(updatedTime),
                                summaryNodeModel.getRating().floatValue(),
                                summaryNodeModel.getReview(),
                                (userModel.getFirstName() != null) ? userModel.getFirstName() : ""
                        ));
                    }
                }
                SummaryModel summaryModel = new SummaryModel(
                        summaryData.get(Constants.ACTIVITY_ID).toString(),
                        summaryData.get(Constants.ACTIVITY_TYPE).toString(),
                        (Float) summaryData.get(Constants.TOTALCOUNT1STARS),
                        (Float) summaryData.get(Constants.TOTALCOUNT2STARS),
                        (Float) summaryData.get(Constants.TOTALCOUNT3STARS),
                        (Float) summaryData.get(Constants.TOTALCOUNT4STARS),
                        (Float) summaryData.get(Constants.TOTALCOUNT5STARS),
                        (Float) summaryData.get(Constants.TOTALNUMBEROFRATINGS),
                        (Float) summaryData.get(Constants.SUMOFTOTALRATINGS),
                        (summaryData.get(Constants.LATEST50REVIEWS)!=null) ? mapper.writeValueAsString(latest50Reviews) :null
                );
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, summaryModel);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, null);
                response.setResponseCode(HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error(e);
            processExceptionBody(response, e, "", HttpStatus.INTERNAL_SERVER_ERROR);
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


            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);

            if (!CollectionUtils.isEmpty(existingDataList)) {

                Map<String, Object> updateRequest = new HashMap<>();
                if(requestRating.getComment()==null) {
                    updateRequest.put(Constants.RATING, requestRating.getRating());
                    updateRequest.put(Constants.REVIEW, requestRating.getReview());
                    updateRequest.put(Constants.UPDATED_ON, timeBasedUuid);
                }
                if(requestRating.getComment()!=null && requestRating.getCommentBy()!=null) {
                    updateRequest.put(Constants.COMMENT, requestRating.getComment());
                    updateRequest.put(Constants.COMMENT_BY, requestRating.getCommentBy());
                    updateRequest.put(Constants.COMMENT_UPDATED_ON,timeBasedUuid);
                }
                Map<String, Object> prevInfo = existingDataList.get(0);
                cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_RATINGS, updateRequest,
                        request);
                ratingMessage = new RatingMessage("ratingUpdate", requestRating.getActivityId(), requestRating.getActivityType(),
                        requestRating.getUserId(), String.valueOf((prevInfo.get(Constants.CREATED_ON))));

                ratingMessage.setPrevValues(processEventMessage(String.valueOf(prevInfo.get(Constants.UPDATED_ON)),
                        (Float) prevInfo.get(Constants.RATING), (String) prevInfo.get(Constants.REVIEW)));
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
            if(requestRating.getComment()==null && requestRating.getCommentBy()==null) {
                System.out.println("Message "+mapper.writeValueAsString(ratingMessage));
                kafkaProducer.push(updateRatingTopicName, ratingMessage);
            }
        } catch (ValidationException ex) {
            logger.error(ex);
            processExceptionBody(response, ex, "", HttpStatus.BAD_REQUEST);
        } catch (KafkaException ex) {
            logger.error(ex);
            processExceptionBody(response, ex, Constants.KAFKA_RATING_EXCEPTION_MESSAGE, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error(ex);
            String errMsg = Constants.RATING_GENERIC_EXCEPTION_MESSAGE;
            processExceptionBody(response, ex, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Override
    public SBApiResponse ratingLookUp(LookupRequest lookupRequest) {
        List<String> listOfUserId = new ArrayList<>();
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_LOOKUP);
        String uuid;

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
            if(lookupRequest.getUpdateOn() !=null){
                uuid = lookupRequest.getUpdateOn();
             }
            else {
                uuid = String.valueOf(UUIDs.timeBased());
            }

            Map<String, Object> existingDataList = cassandraOperation.getRecordsByPropertiesWithPagination(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_LOOKUP, request, null, lookupRequest.getLimit(), uuid, Constants.USER_ID);
            List<LookupResponse> listOfLookupResponse = new ArrayList<>();

            if (!CollectionUtils.isEmpty(existingDataList)) {

                for (Map.Entry<String, Object> existingData : existingDataList.entrySet()) {

                    listOfUserId.add(existingData.getKey());
                }
                Map<String, Object> userRequest = new HashMap<>();
                userRequest.put(Constants.USERID, listOfUserId);
                List<String> fields = new ArrayList<>();
                fields.add(Constants.USERID);
                fields.add(Constants.FIRSTNAME);

                Map<String, Object> existingUserList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD,
                        Constants.TABLE_USER, userRequest, fields, Constants.ID);

                for (String user : listOfUserId) {
                    final ObjectMapper mapper = new ObjectMapper();
                    final UserModel userModel = mapper.convertValue(existingUserList.get(user), UserModel.class);
                    final LookupDataModel lookupModel = mapper.convertValue(existingDataList.get(user), LookupDataModel.class);
                    Long updatedTime= ((UUID.fromString(lookupModel.getUpdatedOn()).timestamp() - 0x01b21dd213814000L) )/ 10000L;
                    listOfLookupResponse.add(new LookupResponse(lookupModel.getActivityId(),
                            lookupModel.getReview(),
                            lookupModel.getRating().toString(),
                            updatedTime,
                            lookupModel.getUpdatedOn(),
                            lookupModel.getActivityType(),
                            lookupModel.getUserId(),
                            (userModel.getFirstName() != null) ? userModel.getFirstName() : ""
                    ));

                }
                Collections.sort(listOfLookupResponse, (l1, l2) -> {
                    if(l1.getUpdatedon() == l2.getUpdatedon())
                        return 0;
                    return l2.getUpdatedon() < l1.getUpdatedon() ? -1 : 1;
                });
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, listOfLookupResponse);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, null);
                response.setResponseCode(HttpStatus.OK);

            }
        } catch (ValidationException ex) {
            logger.error(ex);
            processExceptionBody(response, ex, "", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e);
            processExceptionBody(response, e, "", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;

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
            if (validationBody.getRequestRating().getReview()!=null){
                    if(!Pattern.matches("^[-A-Za-z0-9.!;_?@&\n\"\", ]++$", validationBody.getRequestRating().getReview())) {
                errObjList.add(ResponseMessage.Message.INVALID_REVIEW);
            }
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
            if ((validationBody.getLookupRequest().getRating() != null) && (validationBody.getLookupRequest().getRating() < 1.0
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

    public void processExceptionBody(SBApiResponse response, Exception ex,
                                              String exceptionMessage, HttpStatus status) {
        String errMsg = exceptionMessage + ex.getMessage();
        logger.info("Exception: " + errMsg);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(status);
    }

    public SBApiResponse readRatings(Map<String, Object> request) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_RATINGS_READ);
        String errMsg = "";
        String activityId = "";
        try {
            errMsg = validateRequest(request);
            if (StringUtils.isNotBlank(errMsg)) {
                updateErrorDetails(response, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
            Map<String, Object> compositeKey = new HashMap<String, Object>();

            activityId = (String) requestBody.get(Constants.ACTIVITY_ID);
            compositeKey.put(Constants.ACTIVITY_ID, activityId);
            compositeKey.put(Constants.ACTIVITY_TYPE, (String) requestBody.get(Constants.ACTIVITY_TYPE));
            compositeKey.put(Constants.RATINGS_USER_ID, (List<String>) requestBody.get(Constants.USER_ID));
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, compositeKey, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                response.put(Constants.COUNT, existingDataList.size());
                response.put(Constants.CONTENT, existingDataList);
            }
        } catch (Exception e) {
            errMsg = String.format("Failed to read rating for %s Course. Exception: %s", activityId, e.getMessage());
            logger.error("updateRatingTopicName", e);
        }
        return response;
    }

    private String validateRequest(Map<String, Object> request) {
        StringBuilder strBuilder = new StringBuilder();
        Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
        if (ObjectUtils.isEmpty(requestBody)) {
            strBuilder.append("Invalid Request Body.");
            return strBuilder.toString();
        }

        List<String> missingAttrib = new ArrayList<String>();
        if (!requestBody.containsKey(Constants.ACTIVITY_ID)) {
            missingAttrib.add(Constants.ACTIVITY_ID);
        }
        if (!requestBody.containsKey(Constants.ACTIVITY_TYPE)) {
            missingAttrib.add(Constants.ACTIVITY_TYPE);
        }

        if (!requestBody.containsKey(Constants.USER_ID)) {
            missingAttrib.add(Constants.USER_ID);
        }

        if (missingAttrib.size() > 0) {
            strBuilder.append("The following parameter(s) are missing. Missing params - [")
                    .append(missingAttrib.toString()).append("]");
        }

        return strBuilder.toString();
    }

    private void updateErrorDetails(SBApiResponse response, String errMsg, HttpStatus responseCode) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(responseCode);
    }

}
