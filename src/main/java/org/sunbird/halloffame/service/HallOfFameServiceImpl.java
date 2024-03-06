package org.sunbird.halloffame.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.Constants;

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
        resultMap.put(Constants.MDO_LIST, dptList);
        resultMap.put(Constants.TITLE, formattedDateLastMonth);
        return resultMap;
    }
}
