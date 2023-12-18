package org.sunbird.karmapoints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KarmaPointsServiceImpl implements KarmaPointsService {
    @Autowired
    private CassandraOperation cassandraOperation;
    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public Map<String, Object> fetchKarmaPointsData(String userOrgId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> propertyMap = new HashMap<>();
        List<Map<String, Object>> dptList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                Constants.KEYSPACE_SUNBIRD, Constants.USER_KARMA_POINTS, propertyMap, null);
        resultMap.put("kpList", dptList);

        return resultMap;

    }
}