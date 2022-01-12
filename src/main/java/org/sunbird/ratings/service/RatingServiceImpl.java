package org.sunbird.ratings.service;

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
import org.sunbird.ratings.exception.ValidateRatingException;
import org.sunbird.ratings.exception.ValidateStringException;
import org.sunbird.ratings.model.RequestRating;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class RatingServiceImpl implements RatingService {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    CassandraOperation cassandraOperation;

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
    public SBApiResponse addRating(RequestRating requestRating) {

        SBApiResponse response = new SBApiResponse(Constants.API_RATINGS_ADD);
        try {
            validateRatingsInfo(requestRating);

            if (!isValidString(requestRating.getReview())) {
                throw new ValidateStringException("Invalid fields in Review.The string value should not be malicious");
            }

            Map<String, Object> request = new HashMap<>();
            request.put(Constants.ACTIVITY_ID, requestRating.getActivity_Id());
            request.put(Constants.ACTIVITY_TYPE, requestRating.getActivity_type());
            request.put(Constants.RATINGS_USER_ID, requestRating.getUserId());

            List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_RATINGS, request, null);
            if (!existingDataList.isEmpty()) {
                String errMsg = "Review exist for given course. Cannot insert duplicate ratings for course: "
                        + requestRating.getActivity_Id();
                logger.error(errMsg);
                response.getParams().setErr(errMsg);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            request.put(Constants.COMMENT, "GOOD COMMENT"); //To DO for separate call as discussed
            request.put(Constants.COMMENT_BY, "ELon Musk"); //To DO for separate call as discussed
            request.put(Constants.COMMENT_UPDATED_ON, new Timestamp(new Date().getTime()));
            request.put(Constants.CREATED_ON, new Timestamp(new Date().getTime()));
            request.put(Constants.RATING, requestRating.getRating());
            request.put(Constants.REVIEW, requestRating.getReview());
            request.put(Constants.UPDATED_ON, new Timestamp(new Date().getTime()));

            cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_RATINGS, request);
            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.put(Constants.DATA, request);
            response.setResponseCode(HttpStatus.CREATED);
        } catch (ValidateRatingException ex) {
            String errMsg = "Exception is : ";
            return processExceptionBody(response, ex, errMsg, HttpStatus.BAD_REQUEST);
        } catch (ValidateStringException ex) {
            String errMsg = "Exception is : ";
            return processExceptionBody(response, ex, errMsg, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            String errMsg = "Exception occurred while adding the course review. Exception: ";
            return processExceptionBody(response, ex, errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
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
        if (StringUtils.isEmpty(requestRating.getReview())) {
            errObjList.add(Constants.REVIEW);
        }
        if (StringUtils.isEmpty(requestRating.getUserId())) {
            errObjList.add(Constants.RATINGS_USER_ID);
        }

        if (!CollectionUtils.isEmpty(errObjList)) {
            throw new ValidateRatingException("One or more required fields are empty or incorrect. Empty fields " + errObjList.toString());
        }
    }

    private Boolean isValidString(String isValidField) {
        boolean validField = false;
        if (Pattern.matches("^[A-Za-z0-9, ]++$", isValidField)) {
            validField = true;
        } else {
            validField = false;
        }
        return validField;
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
