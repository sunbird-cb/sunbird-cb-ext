package org.sunbird.karmapoints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.karmapoints.model.KarmaPointsRequest;

import java.time.*;
import java.util.*;

@Service
public class KarmaPointsServiceImpl implements KarmaPointsService {
    @Autowired
    private CassandraOperation cassandraOperation;
    @Autowired
    CbExtServerProperties serverProperties;
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
        result.forEach(record -> {
            long dateAsLong = record.entrySet().stream()
                    .filter(entry -> Constants.CREDIT_DATE.equals(entry.getKey()))
                    .map(entry -> ((Date) entry.getValue()).getTime())
                    .findFirst()
                    .orElse(0L);

            record.put(Constants.CREDIT_DATE, dateAsLong);
        });
        return resultMap;
    }

}