package org.sunbird.halloffame.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author mahesh.vakkund
 */
@Service
public class HallOfFameServiceImpl implements HallOfFameService {
    @Autowired
    private CassandraOperation cassandraOperation;

    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Override
    public Map<String, Object> fetchHallOfFameData() {
        Map<String, Object> resultMap = new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate lastMonthDate = LocalDate.now().minusMonths(1);
        int lastMonthValue = lastMonthDate.getMonthValue();
        int lastMonthYearValue = lastMonthDate.getYear();

        String formattedDateLastMonth = lastMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.MONTH, lastMonthValue);
        propertyMap.put(Constants.YEAR, lastMonthYearValue);

        List<Map<String, Object>> dptList = new ArrayList<>();
        while (dptList.isEmpty()) {
            dptList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, propertyMap, null);
            if (dptList.isEmpty()) {
                lastMonthDate = lastMonthDate.minusMonths(1);
                lastMonthValue = lastMonthDate.getMonthValue();
                lastMonthYearValue = lastMonthDate.getYear();
                formattedDateLastMonth = lastMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                propertyMap.clear();
                propertyMap.put(Constants.MONTH, lastMonthValue);
                propertyMap.put(Constants.YEAR, lastMonthYearValue);
            }
        }
        resultMap.put(Constants.MDO_LIST, dptList);
        resultMap.put(Constants.TITLE, formattedDateLastMonth);
        return resultMap;
    }

    public SBApiResponse learnerLeaderBoard(String rootOrgId, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.LEARNER_LEADER_BOARD);
        try {
            if (StringUtils.isEmpty(rootOrgId)) {
                setBadRequestResponse(response, Constants.ORG_ID_MISSING);
                return response;
            }

            String userId = validateAuthTokenAndFetchUserId(authToken);
            if (StringUtils.isBlank(userId)) {
                setBadRequestResponse(response, Constants.USER_ID_DOESNT_EXIST);
                return response;
            }

            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put(Constants.USER_ID_LOWER, userId);

            List<Map<String, Object>> userRowNum = cassandraOperation.getRecordsByProperties(
                    Constants.SUNBIRD_KEY_SPACE_NAME,
                    Constants.TABLE_LEARNER_LEADER_BOARD_LOOK_UP,
                    propertiesMap,
                    null
            );

            if (userRowNum == null || userRowNum.isEmpty()) {
                setNotFoundResponse(response, Constants.USER_ID_DOESNT_EXIST);
                return response;
            }

            int res = (Integer) userRowNum.get(0).get(Constants.DB_COLUMN_ROW_NUM);
            List<Integer> ranksFilter = Arrays.asList(1, 2, 3, res - 1, res, res + 1);
            Map<String, Object> propMap = new HashMap<>();
            propMap.put(Constants.DB_COLUMN_ROW_NUM, ranksFilter);
            propMap.put(Constants.ORGID, rootOrgId);

            List<Map<String, Object>> result = cassandraOperation.getRecordsByProperties(
                    Constants.SUNBIRD_KEY_SPACE_NAME,
                    Constants.TABLE_LEARNER_LEADER_BOARD,
                    propMap,
                    null
            );

            response.put(Constants.RESULT, result);
            return response;

        } catch (Exception e) {
            setInternalServerErrorResponse(response);
        }

        return response;
    }

    @Override
    public SBApiResponse fetchingTop10Learners(String rootOrgId, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.TOP_10_LEARNERS);
        try {
            if (StringUtils.isEmpty(rootOrgId)) {
                setBadRequestResponse(response, Constants.ORG_ID_MISSING);
                return response;
            }
            String userId = validateAuthTokenAndFetchUserId(authToken);
            if (StringUtils.isBlank(userId)) {
                setBadRequestResponse(response, Constants.USER_ID_DOESNT_EXIST);
                return response;
            }
            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put(Constants.USER_ID_LOWER, userId);

            List<Map<String, Object>> userRowNum = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.SUNBIRD_KEY_SPACE_NAME,
                    Constants.TABLE_LEARNER_LEADER_BOARD_LOOK_UP,
                    propertiesMap,
                    null
            );
            if (CollectionUtils.isEmpty(userRowNum)) {
                setNotFoundResponse(response, Constants.USER_ID_DOESNT_EXIST);
                return response;
            }
            Map<String, Object> propMap = new HashMap<>();
            int res = (Integer) userRowNum.get(0).get(Constants.DB_COLUMN_ROW_NUM);
            List<Integer> ranksFilter = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            propMap.put(Constants.DB_COLUMN_ROW_NUM, ranksFilter);
            propMap.put(Constants.ORGID, rootOrgId);

            List<Map<String, Object>> result = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.SUNBIRD_KEY_SPACE_NAME,
                    Constants.TABLE_TOP_10_LEARNER,
                    propMap,
                    null
            );
            response.put(Constants.RESULT, result);
            return response;

        } catch (Exception e) {
            setInternalServerErrorResponse(response);
        }
        return response;
    }

    private void setBadRequestResponse(SBApiResponse response, String errMsg) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(HttpStatus.BAD_REQUEST);
    }

    private void setNotFoundResponse(SBApiResponse response, String errMsg) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(HttpStatus.NOT_FOUND);
    }

    private void setInternalServerErrorResponse(SBApiResponse response) {
        response.getParams().setStatus(Constants.FAILED);
        response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String validateAuthTokenAndFetchUserId(String authUserToken) {
        return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
    }
}
