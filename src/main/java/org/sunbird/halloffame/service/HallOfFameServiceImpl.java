package org.sunbird.halloffame.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.util.Constants;

import java.io.IOException;
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
        int monthValue = currentDate.getMonthValue();
        int yearValue = currentDate.getYear();
        int lastMonth = (monthValue - 1);
        int previousToLastMonth = (monthValue - 2);
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<Map<String, Object>> dptList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, null, null);

        List<Map<String, Object>> lastToPreviousMonthList = new ArrayList<>();
        List<Map<String, Object>> lastMonthList = new ArrayList<>();

        for (Map<String, Object> record : dptList) {
            int month = (int) record.get("month");
            int year = (int) record.get("year");

            if (month == previousToLastMonth && year == yearValue) {
                lastToPreviousMonthList.add(record);
            } else if (month == lastMonth && year == yearValue) {
                lastMonthList.add(record);
            }
        }
        List<Map<String, Object>> lastMonthWithRankList = processRankBasedOnKpPoints(lastMonthList);
        List<Map<String, Object>> lastToPreviousMonthWithRankList = processRankBasedOnKpPoints(lastToPreviousMonthList);

        for (Map<String, Object> lastMonthWithRank : lastMonthWithRankList) {
            String pvOrgId = (String) lastMonthWithRank.get("org_id");
            int pvRank = (int) lastMonthWithRank.get("rank");
            long pvKpPoints = (long) lastMonthWithRank.get("total_kp");

            for (Map<String, Object> lastToPreviousMonthWithRank : lastToPreviousMonthWithRankList) {
                String lastToPvOrgId = (String) lastToPreviousMonthWithRank.get("org_id");
                int lastToPvRank = (int) lastToPreviousMonthWithRank.get("rank");
                long lastToPvKpPoints = (long) lastToPreviousMonthWithRank.get("total_kp");
                if (pvOrgId.equals(lastToPvOrgId)) {
                    if (pvRank >= lastToPvRank) {
                        lastMonthWithRank.put("negtiveOrPositive", "negative");
                    } else {
                        lastMonthWithRank.put("negtiveOrPositive", "positive");
                    }
                    lastMonthWithRank.put("progress", Math.abs(pvRank - lastToPvRank));
                }
            }
        }

        resultMap.put("title", formattedDate);
        resultMap.put("mdoList", lastMonthWithRankList);
        return resultMap;
    }

    public static List<Map<String, Object>> processRankBasedOnKpPoints(List<Map<String, Object>> dptList) {
        List<Map<String, Object>> dptListWithRanks = dptList.stream()
                .sorted(Comparator.comparing(map -> (Long) map.get("total_kp"), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        Integer rank = 1;
        Long currentTotalKp = (Long) dptListWithRanks.get(0).get("total_kp");
        for (Map<String, Object> map : dptListWithRanks) {
            Long totalKp = (Long) map.get("total_kp");
            if (!totalKp.equals(currentTotalKp)) {
                rank++;
                currentTotalKp = totalKp;
            }
            map.put("rank", rank);
        }
        return dptListWithRanks;
    }
}
