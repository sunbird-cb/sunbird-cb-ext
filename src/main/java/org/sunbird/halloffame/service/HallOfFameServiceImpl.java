package org.sunbird.halloffame.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mahesh.vakkund
 */
@Service
public class HallOfFameServiceImpl implements HallOfFameService {
    @Autowired
    private CassandraOperation cassandraOperation;

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public Map<String, Object> fetchHallOfFameData() {
        Map<String, Object> resultMap = new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate lastMonthDate = LocalDate.now().minusMonths(1);
        int lastMonthValue = lastMonthDate.getMonthValue();
        int lastMonthYearValue = lastMonthDate.getYear();

        String formattedDateLastMonth = currentDate.minusMonths(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        Map<String, Object> propertymap = new HashMap<>();
        propertymap.put(Constants.MONTH, lastMonthValue);
        propertymap.put(Constants.YEAR, lastMonthYearValue);

        List<Map<String, Object>> dptList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, propertymap, null);
        if(dptList.isEmpty()){
            int previousToLastMonth = currentDate.minusMonths(2).getMonthValue();
            int previousToLastMonthsYearValue = currentDate.minusMonths(2).getYear();
            String formattedDatePreviousLastMonth = currentDate.minusMonths(2).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.MONTH, previousToLastMonth);
            propertyMap.put(Constants.YEAR, previousToLastMonthsYearValue);
            List<Map<String, Object>> departmentList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, propertyMap, null);
            resultMap.put(Constants.MDO_LIST, departmentList);
            resultMap.put(Constants.TITLE, formattedDatePreviousLastMonth);
            return resultMap;
        }
        resultMap.put(Constants.MDO_LIST, dptList);
        resultMap.put(Constants.TITLE, formattedDateLastMonth);
        return resultMap;
    }

    public SBApiResponse learnerLeaderBoard(String rootOrgId, String authToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.LEARNER_LEADER_BOARD);
        try {
            if (StringUtils.isEmpty(rootOrgId)) {
                response.getParams().setErrmsg("Invalid Root Org Id");
                response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put("rootOrgId", rootOrgId);
            List<Map<String, Object>> result = cassandraOperation.getRecordsByProperties(Constants.SUNBIRD_KEY_SPACE_NAME,
                    "", propertiesMap , Arrays.asList(""));
            response.put("result", result);
            return response;
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

}
