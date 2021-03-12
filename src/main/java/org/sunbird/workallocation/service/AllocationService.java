package org.sunbird.workallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.common.model.Response;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.workallocation.model.SearchCriteria;
import org.sunbird.workallocation.model.WorkAllocation;
import org.sunbird.workallocation.util.Validator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private Logger logger = LoggerFactory.getLogger(AllocationService.class);

    final String[] includeFields = {"personalDetails.firstname", "personalDetails.surname", "personalDetails.primaryEmail", "id", "professionalDetails.name"};


    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private Validator validator;

    @Autowired
    private CbExtServerProperties configuration;

    ObjectMapper mapper = new ObjectMapper();

    public Response getUsers(SearchCriteria criteria) {
        validator.validateCriteria(criteria);
        final QueryBuilder query = QueryBuilders.termQuery("deptName.keyword", criteria.getDepartmentName());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.from(criteria.getPageNo());
        sourceBuilder.size(criteria.getPageSize());
        List<WorkAllocation> allocationSearchList = new ArrayList<>();
        List<Map<String, Object>> finalRes = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        try {
            SearchResponse searchResponse = getEsResult(configuration.getAllocationIndexName(), configuration.getAllocationIndexType(), sourceBuilder);
            for (SearchHit hit : searchResponse.getHits()) {
                allocationSearchList.add(mapper.convertValue(hit.getSourceAsMap(), WorkAllocation.class));
            }
            Set<String> userIds = allocationSearchList.stream().map(WorkAllocation::getUserId).collect(Collectors.toSet());
            Map<String, Object> usersMap = getUserDetails(userIds);
            for (WorkAllocation workAllocation : allocationSearchList) {
                result = new HashMap<>();
                result.put("allocationDetails", workAllocation);
                result.put("userDetails", usersMap.get(workAllocation.getUserId()));
                finalRes.add(result);
            }
        } catch (IOException e) {
            logger.error("Elastic Search Exception", e);
        }
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, finalRes);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    public Map<String, Object> getUserDetails(Set<String> userIds) throws IOException {
        List<Map<String, Object>> resultArray = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        final BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("id.keyword", userIds));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.fetchSource(includeFields, new String[]{});
        SearchResponse searchResponse = getEsResult(configuration.getEsProfileIndex(), configuration.getEsProfileIndexType(), sourceBuilder);
        for (SearchHit hit : searchResponse.getHits()) {
            Map<String, Object> userResult = extractUserDetails(hit.getSourceAsMap());
            if (!StringUtils.isEmpty(userResult.get("wid")))
                result.put((String) userResult.get("wid"), userResult);
        }
        return result;
    }

    public SearchResponse getEsResult(String indexName, String type, SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        if (!StringUtils.isEmpty(type))
            searchRequest.types(type);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;

    }

    public Map<String, Object> extractUserDetails(Map<String, Object> searObjectMap) {
        Map<String, Object> personalDetails = (Map<String, Object>) searObjectMap.get("personalDetails");
        List<Map<String, Object>> professionalDetails = (List<Map<String, Object>>) searObjectMap.get("professionalDetails");
        String depName = null;
        if (!CollectionUtils.isEmpty(professionalDetails)) {
            Map<String, Object> propDetails = professionalDetails.stream().findFirst().get();
            depName = CollectionUtils.isEmpty(propDetails) ? "" : (String) propDetails.get("name");
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("first_name", personalDetails.get("firstname"));
        result.put("last_name", personalDetails.get("surname"));
        result.put("email", personalDetails.get("primaryEmail"));
        result.put("wid", searObjectMap.get("id"));
        result.put("department_name", depName);
        return result;
    }
}
