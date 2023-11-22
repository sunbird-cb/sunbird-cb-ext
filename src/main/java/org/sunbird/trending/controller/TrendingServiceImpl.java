package org.sunbird.trending.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.exception.InvalidDataInputException;
import java.util.*;
import java.util.function.Function;
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

    public Map<String, Object> trendingSearch(Map<String, Object> requestBody, String token) throws Exception {

        // Read req params
        SBApiResponse response = ProjectUtil.createDefaultResponse(API_USER_INSIGHTS);
        HashMap<String, Object> request = (HashMap<String, Object>) requestBody.get(Constants.REQUEST);
        HashMap<String, Object> filter = ((HashMap<String, Object>) request.get(Constants.FILTERS));
        ArrayList<String> primaryCategoryList = ((ArrayList<String>) (filter).get(Constants.PRIMARY_CATEGORY));
        String org = ((String) (filter).get(Constants.ORGANISATION));
        int limit = Optional.ofNullable(request.get(Constants.LIMIT)).map(l -> (Integer) l).orElse(0);
        List<String> fieldList = primaryCategoryList.stream()
                .map(type -> org + COLON + type)
                .collect(Collectors.toList());
        String[] fieldsArray = fieldList.toArray(new String[fieldList.size()]);
        // Fetch trending Ids for requested type of courses
        List<String> trendingCoursesAndPrograms = redisCacheMgr.hget(TRENDING_COURSES_REDIS_KEY, serverProperties.getRedisInsightIndex(),fieldsArray);
        Map<String, List<String>> typeList = new HashMap<>();
        for(int i=0;i<fieldsArray.length;i++){
            typeList.put(primaryCategoryList.get(i),fetchIds(trendingCoursesAndPrograms.get(i), limit, fieldList.get(i)));
        }
        List<String> searchIds = typeList.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Map<String, Object> compositeSearchRes = new HashMap<>();
        List<Map<String, Object>> contentList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        if(searchIds != null && searchIds.size() > 0) {
             compositeSearchRes = compositeSearch(searchIds, token);
            resultMap = (Map<String, Object>) compositeSearchRes.get(RESULT);
            contentList = (List<Map<String, Object>>) resultMap.get(CONTENT);
        }

        Map<String, Object> contentMap = contentList.stream()
                .collect(Collectors.toMap(content -> (String) content.get(IDENTIFIER), Function.identity()));
        Map<String, List<Object>> resultContentMap = typeList.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(contentMap::get).collect(Collectors.toList())
                ));
        resultMap.remove(CONTENT);
        resultMap.remove(COUNT);
        response.getResult().put(RESPONSE, resultContentMap);
        return response.getResult();
    }
    public List<String> fetchIds(String idStr, int limit, String type) {
        String[] idArray = Optional.ofNullable(idStr).filter(StringUtils::isNotBlank).map(str -> str.split(COMMA)).orElse(null);
        if (idArray == null || idArray.length == 0) {
            return new ArrayList<>();
        }
        List<String> idList = Arrays.asList(idArray);
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
}