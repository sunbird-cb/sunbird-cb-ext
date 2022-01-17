package org.sunbird.ratings.service;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.producer.Producer;
import org.sunbird.ratings.exception.ValidateRatingException;
import org.sunbird.ratings.model.RatingMessage;
import org.sunbird.ratings.model.RequestRating;
import org.sunbird.ratings.model.UpdatedValues;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class RatingServiceImpl implements RatingService {
    private ObjectMapper mapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    Producer producer;

    //TO DO
    @Override
    public SBApiResponse getRatings(String activity_Id, String activity_Type, String userId) {
        return null;
    }

    //TO DO
    @Override
    public SBApiResponse getUsers(String activity_Id, String activity_Type, String userId) {
        return null;
    }

    //TO DO
    @Override
    public SBApiResponse upsertRating(String activity_Id, String activity_Type, String userId) {
        return null;
    }

    //TO DO
    @Override
    public SBApiResponse getRatingSummary(String activity_Id, String activity_Type) {
        return null;
    }

    @Override
    public SBApiResponse upsert(RequestRating requestRating) {
        UUID timeBasedUuid = UUIDs.timeBased();

        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_ADD);
        RatingMessage msg = new RatingMessage();

        try {
            validateRatingsInfo(requestRating);

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, requestRating.getActivity_Id());
            request.put(Constants.ACTIVITY_TYPE, requestRating.getActivity_type());
            request.put(Constants.RATINGS_USER_ID, requestRating.getUserId());


            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);

            if (!existingDataList.isEmpty()) {

                Map<String, Object> updateRequest = new HashMap<>();
                updateRequest.put(Constants.RATING, requestRating.getRating());
                updateRequest.put(Constants.REVIEW, requestRating.getReview());
                updateRequest.put(Constants.UPDATED_ON, timeBasedUuid);

                Map<String, Object> prevInfo = existingDataList.get(0);
                cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_RATINGS, updateRequest,
                        request);

                msg.setAction("ratingUpdate");
                msg.setActivity_id(requestRating.getActivity_Id());
                msg.setActivity_Type(requestRating.getActivity_type());
                msg.setUser_id(requestRating.getUserId());
                msg.setCreated_Date(String.valueOf((prevInfo.get("createdon"))));

                UpdatedValues prevValues = processEventMessage(String.valueOf(prevInfo.get("createdon")),
                        (Float) prevInfo.get("rating"), (String) prevInfo.get("review"));

                UpdatedValues updatedValues = processEventMessage(String.valueOf(updateRequest.get(Constants.UPDATED_ON)),
                        requestRating.getRating(), requestRating.getReview());

                msg.setPrevValues(prevValues);
                msg.setUpdatedValues(updatedValues);

                producer.push(Constants.RATINGS_UPDATE_EVENT, msg);
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.setResponseCode(HttpStatus.CREATED);
                response.setResponseCode(HttpStatus.OK);
                return response;
            }

            request.put(Constants.CREATED_ON, timeBasedUuid);
            request.put(Constants.RATING, requestRating.getRating());
            request.put(Constants.REVIEW, requestRating.getReview());
            request.put(Constants.UPDATED_ON, timeBasedUuid);

            cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_RATINGS, request);

            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.put(Constants.DATA, request);
            response.setResponseCode(HttpStatus.CREATED);
        } catch (ValidateRatingException ex) {
            String errMsg = "Exception is : ";
            return processExceptionBody(response, ex, errMsg, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            String errMsg = "Exception occurred while adding the course review. Exception: ";
            return processExceptionBody(response, ex, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public UpdatedValues processEventMessage(String date, Float rating, String review) {
        UpdatedValues values = new UpdatedValues();
        values.setUpdatedOn(date);
        values.setRating(rating);
        values.setReview(review);
        return values;
    }

    private void validateRatingsInfo(RequestRating requestRating) throws Exception {
        List<String> errObjList = new ArrayList<String>();
        if (StringUtils.isEmpty(requestRating.getActivity_Id())) {
            errObjList.add(Constants.ACTIVITY_ID);
        }
        if (StringUtils.isEmpty(requestRating.getActivity_type())) {
            errObjList.add(Constants.ACTIVITY_TYPE);
        }
        if (StringUtils.isEmpty((String.valueOf(requestRating.getRating()))) || requestRating.getRating() < 1
                || requestRating.getRating() > 5) {
            errObjList.add(Constants.RATING);
        }
        if (StringUtils.isEmpty(requestRating.getReview()) || (!Pattern.matches("^[A-Za-z0-9, ]++$", requestRating.getReview()))) {
            errObjList.add(Constants.REVIEW);
        }
        if (StringUtils.isEmpty(requestRating.getUserId())) {
            errObjList.add(Constants.RATINGS_USER_ID);
        }
        if (!CollectionUtils.isEmpty(errObjList)) {
            throw new ValidateRatingException("One or more required fields are empty or incorrect. Empty fields " + errObjList.toString());
        }
    }

    public SBApiResponse processExceptionBody(SBApiResponse response, Exception ex,
                                              String exceptionMessage, HttpStatus status) {

        String errMsg = exceptionMessage + ex.getMessage();
        logger.error(errMsg, ex);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(status);
        return response;
    }

}
