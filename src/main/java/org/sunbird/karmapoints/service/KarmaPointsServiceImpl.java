package org.sunbird.karmapoints.service;

import com.datastax.driver.core.utils.UUIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.karmapoints.model.KarmaPointsRequest;

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
        String uuid;
        int limit;
        if (request.getOffset() != null) {
            uuid = request.getOffset();
        } else {
            uuid = String.valueOf(UUIDs.timeBased());
        }

        if (request.getLimit() != 0) {
            limit = request.getLimit();
        } else {
            limit = serverProperties.getKarmaPointsLimit();
        }
        List<Map<String, Object>> result = cassandraOperation.getKarmaPointsRecordsByPropertiesWithPaginationList(
                Constants.KEYSPACE_SUNBIRD, Constants.USER_KARMA_POINTS, propertyMap, new ArrayList<>(), limit, uuid, Constants.CONTEXT_ID);
        resultMap.put(Constants.KARMA_POINTS_LIST, result);

        return resultMap;
    }

}