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
        LocalDate lastToPreviousMonthDate = LocalDate.now().minusMonths(2);
        int lastMonthValue = lastMonthDate.getMonthValue();
        int YearValue = lastMonthDate.getYear();

        int previousToLastMonth = lastToPreviousMonthDate.getMonthValue();
        int previousToLastMonthsYear = lastToPreviousMonthDate.getYear();

        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<Map<String, Object>> dptList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, null, null);

        List<Map<String, Object>> lastToPreviousMonthList = new ArrayList<>();
        List<Map<String, Object>> lastMonthList = new ArrayList<>();

        for (Map<String, Object> record : dptList) {
            int month = (int) record.get(Constants.MONTH);
            int year = (int) record.get(Constants.YEAR);

            if (month == previousToLastMonth && year == previousToLastMonthsYear) {
                lastToPreviousMonthList.add(record);
            } else if (month == lastMonthValue && year == YearValue) {
                lastMonthList.add(record);
            }
        }

        Map<String, Map<String, Object>> lastMonthWithRankList = processRankBasedOnKpPoints(lastMonthList);
        Map<String, Map<String, Object>> lastToPreviousMonthWithRankList = processRankBasedOnKpPoints(lastToPreviousMonthList);
        List<Map<String, Object>> trialmapList = lastMonthWithRankList.values().stream()
                .map(lastMonthWithRank -> {
                    String pvOrgId = (String) lastMonthWithRank.get(Constants.ORGID);
                    int pvRank = (int) lastMonthWithRank.get(Constants.RANK);

                    Map<String, Object> trialmap = new HashMap<>(lastMonthWithRank);

                    lastToPreviousMonthWithRankList.getOrDefault(pvOrgId, Collections.emptyMap()).forEach((key, value) -> {
                        if (Constants.RANK.equals(key)) {
                            int lastToPvRank = (int) value;
                            trialmap.put(Constants.NEGATIVE_OR_POSITIVE, (pvRank >= lastToPvRank) ? Constants.NEGATIVE : Constants.POSITIVE);
                            trialmap.put(Constants.PROGRESS, Math.abs(pvRank - lastToPvRank));
                        }
                    });
                    return trialmap;
                })
                .collect(Collectors.toList());

        resultMap.put(Constants.TITLE, formattedDate);
        resultMap.put(Constants.MDO_LIST, trialmapList);
        return resultMap;
    }

    public static Map<String, Map<String, Object>> processRankBasedOnKpPoints(List<Map<String, Object>> dptList) {
        List<Map<String, Object>> dptListWithRanks = new ArrayList<>(dptList);
        Collections.sort(dptListWithRanks, Comparator.comparing(map -> (Long) map.get(Constants.TOTAL_KP), Comparator.reverseOrder()));

        Map<String, Map<String, Object>> resultMap = dptListWithRanks.stream()
                .collect(Collectors.toMap(map -> (String) map.get(Constants.ORGID), map -> map));

        Integer rank = 1;
        Long currentTotalKp = (Long) dptListWithRanks.get(0).get(Constants.TOTAL_KP);

        for (Map<String, Object> map : dptListWithRanks) {
            Long totalKp = (Long) map.get(Constants.TOTAL_KP);
            if (!totalKp.equals(currentTotalKp)) {
                rank++;
                currentTotalKp = totalKp;
            }
            resultMap.get(map.get(Constants.ORGID)).put(Constants.RANK, rank);
        }
        return resultMap;
    }
}
