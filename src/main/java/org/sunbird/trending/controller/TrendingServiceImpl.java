package org.sunbird.trending.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.sunbird.common.util.Constants.*;
@Service
public class TrendingServiceImpl implements TrendingService {

    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    RedisCacheMgr redisCacheMgr;

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    public Map<String, Object> trendingSearch(Map<String, Object> requestBody, String token) throws Exception {

        // Read req params
        SBApiResponse response = ProjectUtil.createDefaultResponse(API_TRENDING_SEARCH);
        HashMap<String, Object> request = (HashMap<String, Object>) requestBody.get(Constants.REQUEST) ==null ? new HashMap<>() : (HashMap<String, Object>) requestBody.get(Constants.REQUEST);
        HashMap<String, Object> filter = ((HashMap<String, Object>) request.get(Constants.FILTERS)) ==null ? new HashMap<>() : ((HashMap<String, Object>) request.get(Constants.FILTERS));
        ArrayList<String> contextTypeList = ((ArrayList<String>) (filter).get(CONTEXT_TYPE)) == null ?  new ArrayList<>() : ((ArrayList<String>) (filter).get(CONTEXT_TYPE));

        String org = ((String) (filter).get(Constants.ORGANISATION)) == null ? "" : ((String) (filter).get(Constants.ORGANISATION))  ;
        String designation = ((String) filter.get(Constants.DESIGNATION));
        String redisKey = TRENDING_COURSES_REDIS_KEY;
        Map<String, String> redisKeyNameMap = new HashMap<>();
        if (StringUtils.isBlank(designation)) {
            designation = "";
        } else {
            designation = designation.toLowerCase();
        }

        boolean isAcbpEnabled = false;
        List<String> updatedContextTypeList = new ArrayList<>();
        for (String contextType : contextTypeList) {
            if (Constants.ACBP_KEY.equalsIgnoreCase(contextType)) {
                isAcbpEnabled = true;
                redisKey = CBP_MANUAL_COURSES_REDIS_KEY;
                redisKeyNameMap.put(org + COLON + Constants.ACBP_KEY + COLON + Constants.ALL_USER_KEY, contextType);
                if (StringUtils.isNotBlank(designation)) {
                    redisKeyNameMap.put(org + COLON + Constants.ACBP_KEY + COLON + designation, contextType);
                }
            } else {
                updatedContextTypeList.add(contextType);
                redisKeyNameMap.put(org + COLON + contextType, contextType);
            }
        }
        int limit = Optional.ofNullable(request.get(Constants.LIMIT)).map(l -> (Integer) l).orElse(0);
        String[] newFieldsArray = redisKeyNameMap.keySet().toArray(new String[0]);
        // Fetch trending Ids for requested type of courses
        List<String> trendingCoursesAndPrograms = redisCacheMgr.hget(redisKey, serverProperties.getRedisInsightIndex(),newFieldsArray);
        Map<String, List<String>> typeList = new HashMap<>();
        if  (CollectionUtils.isNotEmpty(trendingCoursesAndPrograms)) {
            for (int i = 0; i < newFieldsArray.length; i++) {
                String nameValue = redisKeyNameMap.get(newFieldsArray[i]);
                if (typeList.containsKey(nameValue)) {
                    List<String> existingList = typeList.get(nameValue);
                    List<String> newList = fetchIds(trendingCoursesAndPrograms.get(i), limit, newFieldsArray[i]); 
                    existingList.addAll(newList);
                } else {
                    typeList.put(nameValue, fetchIds(trendingCoursesAndPrograms.get(i), limit, newFieldsArray[i]));
                }
            }
        }
        List<String> searchIds = typeList.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Map<String, Object> compositeSearchRes ;
        List<Map<String, Object>> contentList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        if(searchIds != null && searchIds.size() > 0) {
             compositeSearchRes = compositeSearch(searchIds, token);
             if(null == compositeSearchRes)
                 compositeSearchRes = new HashMap<>();
            resultMap =   (Map<String, Object>) compositeSearchRes.get(RESULT) ==null ? new HashMap<>() :  (Map<String, Object>) compositeSearchRes.get(RESULT) ;
            contentList = (List<Map<String, Object>>) resultMap.get(CONTENT) ==null ? new ArrayList<>() :  (List<Map<String, Object>>) resultMap.get(CONTENT);
        }
        Map<String, Object> contentMap = new HashMap<>();
        Iterator<Map<String, Object>> iterator = contentList.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> content = iterator.next();
            String key = (String) content.get(IDENTIFIER);
            // Check for duplicates before putting into the map
            if (!contentMap.containsKey(key)) {
                if (isAcbpEnabled) {
                    content.put(CBP_MANUAL_COURSES_END_DATE, getEndDateFormat());
                }
                contentMap.put(key, content);
            } else {
                // Handle the case when there are duplicate keys
                // In this example, we are simply choosing the existing one
                // You might want to adapt this logic based on your requirements
                // For example, merge properties or choose the one with specific criteria
            }
        }
        Map<String, List<Object>> resultContentMap = typeList.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(contentMap::get).filter(value -> value != null).collect(Collectors.toList())

                ));
        resultMap.remove(CONTENT);
        resultMap.remove(COUNT);
        resultMap.put(RESPONSE, resultContentMap);
        return resultMap;
    }
    public List<String> fetchIds(String idStr, int limit, String type) {
        String[] idArray = Optional.ofNullable(idStr).filter(StringUtils::isNotBlank).map(str -> str.split(COMMA)).orElse(null);
        if (idArray == null || idArray.length == 0) {
            return new ArrayList<>();
        }
        List<String> idList = new ArrayList<>(Arrays.asList(idArray));
        if (idList.size() > limit) {
            idList = idList.subList(0, limit);
        }
        return idList;
    }
    public Map<String, Object> compositeSearch(List<String> searchIds, String token) {
        // Headers for Search API
        Map<String, String> headers = new HashMap<>();
        headers.put(USER_TOKEN, token);
        headers.put(AUTHORIZATION, serverProperties.getSbApiKey());
        // Search Req Body forming
        HashMap<String, Object> searchBody = new HashMap<>();
        HashMap<String, Object> searchReq = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put(IDENTIFIER, searchIds);
        searchReq.put(FILTERS, filters);
        searchBody.put(REQUEST, searchReq);
        return outboundRequestHandlerService.fetchResultUsingPost(
                serverProperties.getKmBaseHost() + serverProperties.getKmCompositeSearchPath(), searchBody,
                headers);
    }
    
    private String getEndDateFormat() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        
            Date inputDate = inputFormat.parse(serverProperties.getCbPlanEndDate());
            return dateFormat.format(inputDate);
        } catch (Exception e) {
            logger.error("Failed to get end date.", e);
        }
        return "";
    }
}