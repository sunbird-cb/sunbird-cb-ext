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
        int lastMonthYearValue = lastMonthDate.getYear();
        int previousToLastMonth = lastToPreviousMonthDate.getMonthValue();
        int previousToLastMonthsYearValue = lastToPreviousMonthDate.getYear();

        String formattedDateLastMonth = currentDate.minusMonths(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        Map<String, Object> propertymap = new HashMap<>();
        List<Object> monthList = Arrays.asList(lastMonthValue, previousToLastMonth);
        propertymap.put(Constants.MONTH, monthList);
        List<Object> yearList = Arrays.asList(lastMonthYearValue, previousToLastMonthsYearValue);
        propertymap.put(Constants.YEAR, yearList);

        List<Map<String, Object>> dptList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, propertymap, null);

        List<Map<String, Object>> lastToPreviousMonthList = new ArrayList<>();
        List<Map<String, Object>> lastMonthList = new ArrayList<>();

        lastToPreviousMonthList = dptList.stream()
                .filter(record -> (int) record.get(Constants.MONTH) == previousToLastMonth
                        && (int) record.get(Constants.YEAR) == previousToLastMonthsYearValue)
                .collect(Collectors.toList());

        lastMonthList = dptList.stream()
                .filter(record -> (int) record.get(Constants.MONTH) == lastMonthValue
                        && (int) record.get(Constants.YEAR) == lastMonthYearValue)
                .collect(Collectors.toList());

        if (lastToPreviousMonthList.isEmpty() && !lastMonthList.isEmpty()) {
            resultMap.put(Constants.TITLE, formattedDateLastMonth);
            Map<String, Map<String, Object>> monthWithRankList = processRankBasedOnKpPoints(lastMonthList);

            List<Map<String, Object>> trialmapList = monthWithRankList.values().stream()
                    .map(map -> {
                        Map<String, Object> hashMap = new HashMap<>(map);
                        hashMap.put(Constants.PROGRESS, 0);
                        hashMap.put(Constants.NEGATIVE_OR_POSITIVE, Constants.POSITIVE);
                        return hashMap;
                    })
                    .collect(Collectors.toList());
            resultMap.put(Constants.MDO_LIST, trialmapList);
            return resultMap;
        }
        if (lastMonthList.isEmpty()) {
            String formattedDatePreviousToLastMonth = LocalDate.now().minusMonths(2).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            int pvsToLastMonth = LocalDate.now().minusMonths(2).getMonthValue();
            int pvsToLastMonthsYear = LocalDate.now().minusMonths(2).getYear();
            int previousToLastTwoMonths = LocalDate.now().minusMonths(3).getMonthValue();
            int previousToLastTwoMonthsYear = LocalDate.now().minusMonths(3).getYear();
            Map<String, Object> propertyMap = new HashMap<>();
            List<Object> monthlist = Arrays.asList(pvsToLastMonth, previousToLastTwoMonths);
            propertyMap.put(Constants.MONTH, monthlist);
            List<Object> yearLst = Arrays.asList(pvsToLastMonthsYear, previousToLastTwoMonthsYear);
            propertyMap.put(Constants.YEAR, yearLst);

            lastToPreviousMonthList.clear();
            lastMonthList.clear();
            List<Map<String, Object>> dpList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.MDO_KARMA_POINTS, propertyMap, null);
            lastToPreviousMonthList = dpList.stream()
                    .filter(record -> ((int) record.get(Constants.MONTH)) == previousToLastTwoMonths
                            && ((int) record.get(Constants.YEAR)) == previousToLastTwoMonthsYear)
                    .collect(Collectors.toList());
            lastMonthList = dpList.stream()
                    .filter(record -> ((int) record.get(Constants.MONTH)) == pvsToLastMonth
                            && ((int) record.get(Constants.YEAR)) == pvsToLastMonthsYear)
                    .collect(Collectors.toList());
            resultMap.put(Constants.TITLE,formattedDatePreviousToLastMonth);
        }

        Map<String, Map<String, Object>> lastMonthWithRankList = processRankBasedOnKpPoints(lastMonthList);
        Map<String, Map<String, Object>> lastToPreviousMonthWithRankList = processRankBasedOnKpPoints(lastToPreviousMonthList);
        List<Map<String, Object>> trialmapList = lastMonthWithRankList.values().stream()
                .map(lastMonthWithRank -> {
                    String pvOrgId = (String) lastMonthWithRank.get(Constants.ORGID);
                    int pvRank = (int) lastMonthWithRank.get(Constants.RANK);
                    Map<String, Object> trialmap = new HashMap<>(lastMonthWithRank);
                    Map<String, Object> lastToPreviousData = lastToPreviousMonthWithRankList.getOrDefault(pvOrgId, Collections.emptyMap());
                    if (lastToPreviousData.isEmpty()) {
                        trialmap.put(Constants.NEGATIVE_OR_POSITIVE, Constants.POSITIVE);
                        trialmap.put(Constants.PROGRESS, "0");
                    } else {
                        lastToPreviousData.forEach((key, value) -> {
                            if (Constants.RANK.equals(key)) {
                                int lastToPvRank = (int) value;
                                trialmap.put(Constants.NEGATIVE_OR_POSITIVE, (pvRank > lastToPvRank) ? Constants.NEGATIVE : Constants.POSITIVE);
                                trialmap.put(Constants.PROGRESS, Math.abs(pvRank - lastToPvRank));
                            }
                        });
                    }
                    return trialmap;
                })
                .collect(Collectors.toList());
        if (!resultMap.containsKey(Constants.TITLE)) {
            resultMap.put(Constants.TITLE, formattedDateLastMonth);
        }
        resultMap.put(Constants.MDO_LIST, trialmapList);
        return resultMap;
    }

    public static Map<String, Map<String, Object>> processRankBasedOnKpPoints(List<Map<String, Object>> dptList) {
        List<Map<String, Object>> dptListWithRanks = dptList.stream()
                .filter(map -> map.get(Constants.AVERAGE_KP) != null)
                .sorted(Comparator.comparing(map -> (Float) map.get(Constants.AVERAGE_KP), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        Map<String, Map<String, Object>> resultMap = dptListWithRanks.stream()
                .collect(Collectors.toMap(map -> (String) map.get(Constants.ORGID), map -> map));

        Integer rank = 1;
        Float currentAvegareKp = (Float) dptListWithRanks.get(0).get(Constants.AVERAGE_KP);

        for (Map<String, Object> map : dptListWithRanks) {
            Float averageKp = (Float) map.get(Constants.AVERAGE_KP);
            if (!averageKp.equals(currentAvegareKp)) {
                rank++;
                currentAvegareKp = averageKp;
            }
            resultMap.get(map.get(Constants.ORGID)).put(Constants.RANK, rank);
        }

        resultMap = resultMap.entrySet()
                .stream()
                .sorted(Comparator
                        .comparing((Map.Entry<String, Map<String, Object>> entry) -> (Integer) entry.getValue().get("rank"))
                        .thenComparing(entry -> (Float) entry.getValue().get("average_kp"))
                        .thenComparing(entry -> (Date) entry.getValue().get("latest_credit_date")))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
        int newRank = 1;
        for (Map<String, Object> entryValue : resultMap.values()) {
            entryValue.put("rank", newRank++);
        }
        return resultMap;
    }
}
