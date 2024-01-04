package org.sunbird.karmapoints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.producer.Producer;
import org.sunbird.karmapoints.model.ClaimKarmaPointsRequest;
import org.sunbird.karmapoints.model.KarmaPointsRequest;

import java.time.*;
import java.util.*;

@Service
public class KarmaPointsServiceImpl implements KarmaPointsService {
    @Autowired
    private CassandraOperation cassandraOperation;
    @Autowired
    CbExtServerProperties serverProperties;
    @Autowired
    Producer kafkaProducer;
    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public Map<String, Object> fetchKarmaPointsData(String userId, KarmaPointsRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.KARMA_POINTS_USER_ID, userId);
        Date formattedDateTime = new Date(request.getOffset());
        int limit;
        if (request.getLimit() != 0) {
            limit = request.getLimit();
        } else {
            limit = serverProperties.getKarmaPointsLimit();
        }

        if (request.getOffset() == 0L) {
            LocalDate currentDate = LocalDate.now();
            formattedDateTime = Date.from(currentDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        List<Map<String, Object>> result = cassandraOperation.getKarmaPointsRecordsByPropertiesWithPaginationList(
                Constants.KEYSPACE_SUNBIRD, Constants.USER_KARMA_POINTS, propertyMap, new ArrayList<>(), limit, formattedDateTime, Constants.CONTEXT_ID);
        resultMap.put(Constants.KARMA_POINTS_LIST, result);

        long count = cassandraOperation.getRecordCountWithUserId(Constants.KEYSPACE_SUNBIRD, Constants.USER_KARMA_POINTS, userId);
        resultMap.put(Constants.KARMA_POINTS_LIST, result);

        resultMap.put(Constants.COUNT, count);

        result.forEach(record -> {
            long dateAsLong = record.entrySet().stream()
                    .filter(entry -> Constants.DB_COLUMN_CREDIT_DATE.equals(entry.getKey()))
                    .map(entry -> ((Date) entry.getValue()).getTime())
                    .findFirst()
                    .orElse(0L);

            record.put(Constants.DB_COLUMN_CREDIT_DATE, dateAsLong);
        });
        return resultMap;
    }

    public Map<String, Object> fetchKarmaPointsUserCourse(String userId, Map<String, Object> requestBody) {
        Map<String, Object> req = (HashMap<String,Object>)requestBody.get("request");
        Map<String, Object> filters = (HashMap<String,Object>)req.get("filters");
        String cntxtId = (String) filters.get(Constants.CONTEXT_ID_CAMEL);
        String cntxType = (String) filters.get(Constants.CONTEXT_TYPE_CAMEL);
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> propertyMap = new HashMap<>();
        String key = userId +"|"+cntxType+"|"+ cntxtId;
        propertyMap.put(Constants.DB_COLUMN_USER_KARMA_POINTS_KEY, key);
        Map<String, Object> userCourseKpList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_KARMA_POINTS_LOOK_UP, propertyMap, null, Constants.DB_COLUMN_USER_KARMA_POINTS_KEY);
        if(userCourseKpList.size() < 1)
          return resultMap;
        long credit_date = ((Date)((Map<String, Object>)userCourseKpList.get(key)).get(Constants.DB_COLUMN_CREDIT_DATE)).getTime();
        Map<String, Object> whereMap = new HashMap<>();
        whereMap.put(Constants.KARMA_POINTS_USER_ID, userId);
        whereMap.put(Constants.DB_COLUMN_CREDIT_DATE, credit_date);
        whereMap.put(Constants.DB_CLOUMN_CONTEXT_TYPE, cntxType);
        whereMap.put(Constants.DB_COLUMN_CONTEXT_ID, cntxtId);
        whereMap.put(Constants.DB_COLUMN_OPERATION_TYPE, Constants.COURSE_COMPLETION);
        List<Map<String, Object>> userKpList = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_KARMA_POINTS, whereMap, null);
        Map<String, Object>  result = new HashMap<>();
        if(userKpList !=null && !userKpList.isEmpty())
            result = userKpList.get(0);
        resultMap.put(Constants.KARMA_POINTS_LIST, result);
        return resultMap;
    }
    public void claimKarmaPoints(ClaimKarmaPointsRequest request) {
        Map<String, Object> karmaPointsDataMap = new HashMap<>();
        Map<String, Object> edata = new HashMap<>();
        edata.put(Constants.USER_ID, request.getUserId());
        edata.put(Constants.COURSE_ID, request.getCourseId());
        karmaPointsDataMap.put("edata",edata);
        kafkaProducer.push(serverProperties.getClaimKarmaPointsTopic(), karmaPointsDataMap);
        logger.info("UserID and CourseId successfully Published to : " + serverProperties.getClaimKarmaPointsTopic());
    }

    public void fetchKarmaPoints(){

    }
}