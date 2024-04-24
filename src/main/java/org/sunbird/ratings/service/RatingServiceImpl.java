package org.sunbird.ratings.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.ratings.exception.ValidationException;
import org.sunbird.ratings.model.LookupDataModel;
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.LookupResponse;
import org.sunbird.ratings.model.RatingMessage;
import org.sunbird.ratings.model.RatingModelInfo;
import org.sunbird.ratings.model.RequestRating;
import org.sunbird.ratings.model.SummaryModel;
import org.sunbird.ratings.model.SummaryNodeModel;
import org.sunbird.ratings.model.UserModel;
import org.sunbird.ratings.model.ValidationBody;
import org.sunbird.ratings.responsecode.ResponseCode;
import org.sunbird.ratings.responsecode.ResponseMessage;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.user.service.UserUtilityService;

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

    @Autowired
    ContentService contentService;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    CbExtServerProperties serverConfig;

    @Autowired
    RedisCacheMgr redisCacheMgr;

    @Autowired
    UserUtilityService userUtilityService;

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
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                Map<String, Object> ratingData = existingDataList.get(0);
                RatingModelInfo ratingModelInfo = new RatingModelInfo();
                ratingModelInfo.setActivityId((String) ratingData.get(Constants.ACTIVITY_ID));
                ratingModelInfo.setReview((String) ratingData.get(Constants.REVIEW));
                ratingModelInfo.setRating((Float) ratingData.get(Constants.RATING));
                ratingModelInfo.setComment(ratingData.get(Constants.COMMENT)!=null ?(String) ratingData.get(Constants.COMMENT) : null);
                ratingModelInfo.setCommentBy(ratingData.get(Constants.COMMENT_BY)!=null ?(String) ratingData.get(Constants.COMMENT_BY) : null);
                ratingModelInfo.setRecommended(ratingData.get(Constants.RECOMMENDED)!=null ?(String)ratingData.get(Constants.RECOMMENDED): null);
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

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD,
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

                    Map<String, Object> existingUserList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
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


            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD,
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
                if(requestRating.getRecommended()!=null){
                    updateRequest.put(Constants.RECOMMENDED, requestRating.getRecommended());
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
                request.put(Constants.RECOMMENDED,requestRating.getRecommended());
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

                Map<String, Object> existingUserList = cassandraOperation.getRecordsByPropertiesByKey(Constants.KEYSPACE_SUNBIRD,
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

    public SBApiResponse updateRatingsMetaData() {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_RATINGS_CONTENT_META_UPDATE);
        try {
            Map<String, Object> request = new HashMap<>();
            long startTime = System.currentTimeMillis();
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_SUMMARY, request, Arrays.asList(Constants.ACTIVITY_ID, Constants.TOTALNUMBEROFRATINGS, Constants.SUMOFTOTALRATINGS,
                            Constants.TOTALCOUNT1STARS, Constants.TOTALCOUNT2STARS, Constants.TOTALCOUNT3STARS, Constants.TOTALCOUNT4STARS, Constants.TOTALCOUNT5STARS));

            int totalNumberOfUpdatedContent = 0;
            int totalNumberOfErrorContent = 0;
            for (Map<String, Object> ratingSummary : existingDataList) {
                String contentId = (String) ratingSummary.get(Constants.ACTIVITY_ID);
                logger.info("Start Update Content Elastic for contentId: " + contentId);

                List<String> fields = Arrays.asList(Constants.VERSION_KEY, Constants.IDENTIFIER, Constants.ADDITIONAL_TAGS);
                Map<String, Object> contentResponse = contentService.readContent(contentId, fields);
                if (!ObjectUtils.isEmpty(contentResponse)) {
                    String versionKey = (String) contentResponse.get(Constants.VERSION_KEY);
                    Map<String, Object> updateRatingValues = new HashMap<>();
                    updateRatingValues.put(Constants.VERSION_KEY, versionKey);
                    Float totalNumberOfRating = (Float) ratingSummary.get(Constants.TOTALNUMBEROFRATINGS);
                    Float sumOfTotalRating = (Float) ratingSummary.get(Constants.SUMOFTOTALRATINGS);
                    BigDecimal result = BigDecimal.valueOf(sumOfTotalRating).divide(BigDecimal.valueOf(totalNumberOfRating), 1, RoundingMode.HALF_UP);
                    updateRatingValues.put(Constants.AVG_RATING, result.floatValue());
                    updateRatingValues.put(Constants.TOTAL_NO_OF_RATING, totalNumberOfRating.intValue());
                    updateRatingValues.put(Constants.COUNT_ONE_STAR_RATING, ((Float) ratingSummary.get(Constants.TOTALCOUNT1STARS)).intValue());
                    updateRatingValues.put(Constants.COUNT_TWO_STAR_RATING, ((Float) ratingSummary.get(Constants.TOTALCOUNT2STARS)).intValue());
                    updateRatingValues.put(Constants.COUNT_THREE_STAR_RATING, ((Float) ratingSummary.get(Constants.TOTALCOUNT3STARS)).intValue());
                    updateRatingValues.put(Constants.COUNT_FOUR_STAR_RATING, ((Float) ratingSummary.get(Constants.TOTALCOUNT4STARS)).intValue());
                    updateRatingValues.put(Constants.COUNT_FIVE_STAR_RATING, ((Float) ratingSummary.get(Constants.TOTALCOUNT5STARS)).intValue());

                    Map<String, Object> contentRequest = new HashMap<>();
                    contentRequest.put(Constants.CONTENT, updateRatingValues);
                    Map<String, Object> updateContent = new HashMap<>();
                    updateContent.put(Constants.REQUEST, contentRequest);
                    Map<String, Object> updateReadData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(
                            serverConfig.getLearningServiceBaseUrl() + serverConfig.getSystemUpdateAPI() + contentId, updateContent,
                            ProjectUtil.getDefaultHeaders());
                    if (Constants.OK.equalsIgnoreCase((String) updateReadData.get(Constants.RESPONSE_CODE))) {
                        totalNumberOfUpdatedContent = totalNumberOfUpdatedContent + 1;
                    } else {
                        totalNumberOfErrorContent = totalNumberOfErrorContent + 1;
                    }
                } else {
                    totalNumberOfErrorContent = totalNumberOfErrorContent + 1;
                }
            }
            logger.info("Update End at time in ms: " + (System.currentTimeMillis() - startTime));
            response.setResponseCode(HttpStatus.OK);
            response.getResult().put(Constants.TOTAL_NUMBER_UPDATED_COUNT, totalNumberOfUpdatedContent);
            response.getResult().put(Constants.TOTAL_NUMBER_ERROR_COUNT, totalNumberOfErrorContent);
            response.getParams().setStatus(Constants.SUCCESS);
        } catch (Exception e) {
            logger.error("updateRatingTopicName", e);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getResult().put(Constants.ERROR_MESSAGE, e.getMessage());
        }
        return response;
    }

    @Override
    public SBApiResponse updateAdditionalTag(String tag) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CONTENT_META_UPDATE);
        try {
            List<String> latestCourseList = getCourseListFromRedis(tag);

            List<Map<String, Object>> contentDataList = contentService.searchContent(tag);
            long startTime = System.currentTimeMillis();
            int totalNumberOfUpdatedContent = 0;
            int totalNumberOfErrorContent = 0;


            List<String> fields = Arrays.asList(Constants.VERSION_KEY, Constants.IDENTIFIER, Constants.ADDITIONAL_TAGS);
            List<String> contentListIds = new ArrayList<>();
            if(contentDataList != null) {
                contentListIds = contentDataList.stream().map(map -> (String) map.get(Constants.IDENTIFIER)).filter(value -> value != null).collect(Collectors.toList());
            }
            for (String contentId : latestCourseList) {
                if (!contentListIds.contains(contentId)) {
                    logger.info("Start Update Content Elastic for contentId: " + contentId);

                    Map<String, Object> contentResponse = contentService.readContent(contentId, fields);
                    //Adding the Content value to metaData for most Enrolled by checking through Redish
                    if (!ObjectUtils.isEmpty(contentResponse)) {
                        if (updateAdditionalTag(contentResponse, tag, false)) {
                            totalNumberOfUpdatedContent = totalNumberOfUpdatedContent + 1;
                        } else {
                            totalNumberOfErrorContent = totalNumberOfErrorContent + 1;
                        }
                    } else {
                        totalNumberOfErrorContent = totalNumberOfErrorContent + 1;
                    }
                }
            }
            contentListIds.removeAll(latestCourseList);
            for (String removeContentId : contentListIds) {
                logger.info("Start Update Content Elastic for Remove mostEnrolled Tags contentId: " + removeContentId);

                Map<String, Object> contentResponse = contentService.readContent(removeContentId, fields);
                //Remove the Content value to metaData for most Enrolled
                if (!ObjectUtils.isEmpty(contentResponse)) {
                    if (updateAdditionalTag(contentResponse, tag, true)) {
                        totalNumberOfUpdatedContent = totalNumberOfUpdatedContent + 1;
                    } else {
                        totalNumberOfErrorContent = totalNumberOfErrorContent + 1;
                    }
                } else {
                    totalNumberOfErrorContent = totalNumberOfErrorContent + 1;
                }
            }
            logger.info("Update End at time in ms: " + (System.currentTimeMillis() - startTime));
            response.setResponseCode(HttpStatus.OK);
            response.getResult().put(Constants.TOTAL_NUMBER_UPDATED_COUNT, totalNumberOfUpdatedContent);
            response.getResult().put(Constants.TOTAL_NUMBER_ERROR_COUNT, totalNumberOfErrorContent);
            response.getParams().setStatus(Constants.SUCCESS);
        } catch (Exception e) {
            logger.error("updateContentTopicName", e);

            response.getParams().setStatus(Constants.CLIENT_ERROR);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getResult().put(Constants.ERROR_MESSAGE, e.getMessage());
        }
        return response;
    }

    @Override
    public SBApiResponse getTopReviewsForUserByOrgID(String userOrgId) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_TOD_COMMENT_FOR_USER);
        try {
            List<String> topCommentRedis = redisCacheMgr.hget(Constants.TOP_COMMENTS_ORG_REDIS_KEY, serverConfig.getRedisInsightIndex(), userOrgId);
            List<Map<String, Object>> userRatingContentMap = new ArrayList<>();
            Map<String, Map<String, String>> userInfoMap = new HashMap<>();
            List<Map<String, Object>> topReviewMap = new ArrayList<>();
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(topCommentRedis)) {
                topReviewMap = mapper.readValue(topCommentRedis.get(0),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
            }
            List<String> userInfo = new ArrayList<>();
            for (Map<String, Object> commentInfo : topReviewMap) {
                Map<String, Object> userRatingContent = new HashMap<>();
                userInfo.add((String) commentInfo.get(Constants.USER_ID_KEY));
                userRatingContent.put(Constants.USER_DETAILS, commentInfo.get(Constants.USER_ID_KEY));
                Map<String, Object> contentResponse = contentService.readContentFromCache((String) commentInfo.get(Constants.COURSE_ID_KEY), null);
                if (MapUtils.isNotEmpty(contentResponse)) {
                    if (Constants.LIVE.equalsIgnoreCase((String) contentResponse.get(Constants.STATUS))) {
                        Map<String, Object> enrichContentMap = new HashMap<>();
                        enrichContentMap.put(Constants.NAME, contentResponse.get(Constants.NAME));
                        enrichContentMap.put(Constants.COMPETENCIES_V5, contentResponse.get(Constants.COMPETENCIES_V5));
                        enrichContentMap.put(Constants.AVG_RATING, contentResponse.get(Constants.AVG_RATING));
                        enrichContentMap.put(Constants.IDENTIFIER, contentResponse.get(Constants.IDENTIFIER));
                        enrichContentMap.put(Constants.DESCRIPTION, contentResponse.get(Constants.DESCRIPTION));
                        enrichContentMap.put(Constants.ADDITIONAL_TAGS, contentResponse.get(Constants.ADDITIONAL_TAGS));
                        enrichContentMap.put(Constants.CONTENT_TYPE_KEY, contentResponse.get(Constants.CONTENT_TYPE_KEY));
                        enrichContentMap.put(Constants.PRIMARY_CATEGORY, contentResponse.get(Constants.PRIMARY_CATEGORY));
                        enrichContentMap.put(Constants.DURATION, contentResponse.get(Constants.DURATION));
                        enrichContentMap.put(Constants.COURSE_APP_ICON, contentResponse.get(Constants.COURSE_APP_ICON));
                        enrichContentMap.put(Constants.POSTER_IMAGE, contentResponse.get(Constants.POSTER_IMAGE));
                        enrichContentMap.put(Constants.ORGANISATION, contentResponse.get(Constants.ORGANISATION));
                        enrichContentMap.put(Constants.CREATOR_LOGO, contentResponse.get(Constants.CREATOR_LOGO));
                        userRatingContent.put(Constants.CONTENT_INFO, enrichContentMap);
                    }
                    userRatingContentMap.add(userRatingContent);
                }
                commentInfo.remove(Constants.USER_ID_KEY);
                commentInfo.remove(Constants.COURSE_ID_KEY);
                userRatingContent.putAll(commentInfo);
            }
            userUtilityService.getUserDetailsFromDB(userInfo, Arrays.asList(Constants.FIRSTNAME, Constants.USER_ID, Constants.PROFILE_DETAILS_KEY),
                    userInfoMap);
            enrichUserInfo(userInfoMap);

            for (Map<String, Object> commentContentInfo : userRatingContentMap) {
                Map<String, String> userDetailsInfo = userInfoMap.get((String) commentContentInfo.get(Constants.USER_DETAILS));
                commentContentInfo.put(Constants.USER_DETAILS, userDetailsInfo);
            }
            response.getResult().put(Constants.CONTENT, userRatingContentMap);
        } catch (Exception e) {
            logger.error("Failed to Get Top Review of User for OrgId: " + userOrgId, e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private boolean updateAdditionalTag(Map<String, Object> contentResponse, String tag, boolean isRemove) {
        try {
            String versionKey = (String) contentResponse.get(Constants.VERSION_KEY);
            String contentId = (String) contentResponse.get(Constants.IDENTIFIER);
            List<String> additionalTags = (List<String>) contentResponse.get(Constants.ADDITIONAL_TAGS);
            if (additionalTags == null) {
                additionalTags = new ArrayList<>();
            }
            if (isRemove) {

                if (additionalTags.size() == 0)
                    return false;
                additionalTags.remove(tag);
            } else {
                if (additionalTags.contains(tag))
                    return true;
                additionalTags.add(tag);
            }
            Map<String, Object> updatedValues = new HashMap<>();
            updatedValues.put(Constants.VERSION_KEY, versionKey);
            updatedValues.put(Constants.ADDITIONAL_TAGS, additionalTags);
            Map<String, Object> contentRequest = new HashMap<>();
            contentRequest.put(Constants.CONTENT, updatedValues);
            Map<String, Object> updateContent = new HashMap<>();
            updateContent.put(Constants.REQUEST, contentRequest);
            Map<String, Object> updateReadData = (Map<String, Object>) outboundRequestHandlerService.fetchResultUsingPatch(serverConfig.getLearningServiceBaseUrl()
                    + serverConfig.getSystemUpdateAPI() + contentId, updateContent, ProjectUtil.getDefaultHeaders());
            if (Constants.OK.equalsIgnoreCase((String) updateReadData.get(Constants.RESPONSE_CODE))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    private List<String> getCourseListFromRedis(String tag) {
        if (Constants.MOST_ENROLLED.equalsIgnoreCase(tag)) {
            String latestCourseString = redisCacheMgr.getCacheFromDataRedish(Constants.REDIS_COURSE_MOST_ENROLLED_TAG, serverConfig.getRedisInsightIndex());
            return Arrays.asList(latestCourseString.split(","));
        } else if (Constants.MOST_TRENDING.equalsIgnoreCase(tag)) {
            List<String> latestTrendingCourseListRedis = redisCacheMgr.hget(Constants.REDIS_COURSE_MOST_TRENDING_TAG, serverConfig.getRedisInsightIndex(), Constants.ACROSS_COURSES, Constants.ACROSS_PROGRAMS);
            List<String> latestTrendingCourseList = new ArrayList<>();
            if (latestTrendingCourseListRedis != null && latestTrendingCourseListRedis.size() == 2) {
                latestTrendingCourseList.addAll(Arrays.asList(latestTrendingCourseListRedis.get(0).split(",")));
                latestTrendingCourseList.addAll(Arrays.asList(latestTrendingCourseListRedis.get(1).split(",")));
            }

            return latestTrendingCourseList.stream().filter(courseId -> !courseId.contains("_rc")).collect(Collectors.toList());
        }
        throw new BadRequestException("Please provide a valid Tag");
    }

    private void enrichUserInfo(Map<String, Map<String, String>> userInfoMap) {
        for (Map.Entry userEntry : userInfoMap.entrySet()) {
            Map<String, String> userInfo = (Map<String, String>) userEntry.getValue();
            String profileDetails = userInfo.get(Constants.PROFILE_DETAILS_KEY);
            String userProfileImageUrl = userInfo.get(Constants.PROFILE_IMAGE_URL) != null ? userInfo.get(Constants.PROFILE_IMAGE_URL) :
                    getProfileDetailsUrlForUser(profileDetails, (String) userEntry.getKey());
            userInfo.put(Constants.PROFILE_IMAGE_URL, userProfileImageUrl);
            userInfo.remove(Constants.PROFILE_DETAILS_KEY);
        }
    }

    private String getProfileDetailsUrlForUser(String profileDetails, String userId) {
        String profileImageUrl = "";
        try {
            Map<String, Object> profileDetailsMap = null;
            Map<String, Object> personDetails = null;
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(profileDetails)) {
                profileDetailsMap = mapper.readValue(profileDetails, new TypeReference<HashMap<String, Object>>() {
                });
            }
            if (MapUtils.isNotEmpty(profileDetailsMap)) {
                personDetails = (Map<String, Object>) profileDetailsMap.get(Constants.PERSONAL_DETAILS);
            }
            if (MapUtils.isNotEmpty(personDetails)) {
                profileImageUrl = (String) personDetails.get(Constants.PROFILE_IMAGE_URL);
            }
        } catch (Exception e) {
            logger.error("Not able to read the profile Details for userId: " + userId, e);
        }
        return profileImageUrl;
    }
}
