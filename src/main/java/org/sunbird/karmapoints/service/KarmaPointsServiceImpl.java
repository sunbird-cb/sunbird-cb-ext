package org.sunbird.karmapoints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.Constants;

import java.util.*;

@Service
public class KarmaPointsServiceImpl implements KarmaPointsService {
    @Autowired
    private CassandraOperation cassandraOperation;
    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public Map<String, Object> fetchKarmaPointsData(String UserId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.KARMA_POINTS_USER_ID, UserId);
        Map<String, Object> result = cassandraOperation.getRecordsByPropertiesWithPagination(
                Constants.KEYSPACE_SUNBIRD, Constants.USER_KARMA_POINTS, propertyMap, new ArrayList<>(), 10, null, "context_id");

        resultMap.put("kpList", result);

        return resultMap;
    }
}