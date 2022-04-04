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
import org.sunbird.ratings.model.LookupRequest;
import org.sunbird.ratings.model.RatingMessage;
import org.sunbird.ratings.model.RequestRating;
import org.sunbird.ratings.model.ValidationBody;
import org.sunbird.ratings.responsecode.ResponseCode;
import org.sunbird.ratings.responsecode.ResponseMessage;

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
    public SBApiResponse getRatings(String activity_id, String activity_type, String user_id) {
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_READ);

        try {
            validationBody = new ValidationBody();
            validationBody.setActivity_id(activity_id);
            validationBody.setActivity_type(activity_type);
            validationBody.setUser_id(user_id);
            validateRatingsInfo(validationBody, "getRating");


            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, activity_id);
            request.put(Constants.ACTIVITY_TYPE, activity_type);
            request.put(Constants.RATINGS_USER_ID, user_id);
            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, existingDataList);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errMsg = Constants.NO_RATING_EXCEPTION_MESSAGE + activity_id + ", activity_type: " + activity_type;
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
    public SBApiResponse getRatingSummary(String activity_id, String activity_type) {
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_SUMMARY);

        try {
            validationBody = new ValidationBody();
            validationBody.setActivity_id(activity_id);
            validationBody.setActivity_type(activity_type);

            validateRatingsInfo(validationBody, "getSummary");

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, activity_id);
            request.put(Constants.ACTIVITY_TYPE, activity_type);

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_SUMMARY, request, null);
            if (!CollectionUtils.isEmpty(existingDataList)) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, existingDataList);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errMsg = Constants.NO_REVIEW_EXCEPTION_MESSAGE + activity_id + ", activity_type: " + activity_type;
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
            request.put(Constants.ACTIVITY_ID, requestRating.getActivity_id());
            request.put(Constants.ACTIVITY_TYPE, requestRating.getActivity_type());
            request.put(Constants.RATINGS_USER_ID, requestRating.getUser_id());


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
                ratingMessage = new RatingMessage("ratingUpdate", requestRating.getActivity_id(), requestRating.getActivity_type(),
                        requestRating.getUser_id(), String.valueOf((prevInfo.get("createdon"))));

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

                ratingMessage = new RatingMessage("ratingAdd", requestRating.getActivity_id(), requestRating.getActivity_type(),
                        requestRating.getUser_id(), String.valueOf(timeBasedUuid));

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
        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_LOOKUP);

        try {
            validationBody = new ValidationBody();
            validationBody.setLookupRequest(lookupRequest);
            validateRatingsInfo(validationBody, "lookup");

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, lookupRequest.getActivity_id());
            request.put(Constants.ACTIVITY_TYPE, lookupRequest.getActivity_type());

            if (lookupRequest.getRating() != -1.0f) {
                request.put(Constants.RATING, lookupRequest.getRating());
            }

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByPropertiesWithPagination(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS_LOOKUP, request, null, lookupRequest.getLimit(), lookupRequest.getUpdateOn());
            if (!CollectionUtils.isEmpty(existingDataList)) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, existingDataList);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errMsg = Constants.NO_RATING_EXCEPTION_MESSAGE + lookupRequest.getActivity_id() + ", activity_type: " + lookupRequest.getActivity_type();
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
            if (StringUtils.isEmpty(validationBody.getRequestRating().getActivity_id())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty(validationBody.getRequestRating().getActivity_type())) {
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
            if (StringUtils.isEmpty(validationBody.getRequestRating().getUser_id())) {
                errObjList.add(ResponseMessage.Message.INVALID_USER);
            }
        } else if (flag == Constants.RATING_LOOKUP_RATING_OPERATION) {

            if (StringUtils.isEmpty(validationBody.getLookupRequest().getActivity_id())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty(validationBody.getLookupRequest().getActivity_type())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (validationBody.getLookupRequest().getRating() != -1.0f
                    && (validationBody.getLookupRequest().getRating() < 1.0
                    || validationBody.getLookupRequest().getRating() > 5.0)) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT + ResponseMessage.Message.INVALID_RATING);
            }
            if (validationBody.getLookupRequest().getLimit() < 1) {
                errObjList.add(ResponseMessage.Message.INVALID_LIMIT);
            }
        } else if (flag == Constants.RATING_GET_OPERATION || flag == Constants.RATING_SUMMARY_OPERATION) {
            if (StringUtils.isEmpty(validationBody.getActivity_id())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }
            if (StringUtils.isEmpty(validationBody.getActivity_type())) {
                errObjList.add(ResponseMessage.Message.INVALID_INPUT);
            }

            if (flag == Constants.RATING_GET_OPERATION && StringUtils.isEmpty(validationBody.getUser_id())) {
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
