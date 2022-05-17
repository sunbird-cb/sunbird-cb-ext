package org.sunbird.common.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.sunbird.common.model.SearchDTO;
import org.sunbird.common.service.ElasticSearchService;
import org.sunbird.common.util.*;
import org.sunbird.core.logger.CbExtLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * This class will provide all required operation for elastic search.
 *
 * @author github.com/iostream04
 */
@Service
@Configurable
public class ElasticSearchRestHighImpl implements ElasticSearchService {

    @Autowired
    CbExtServerProperties configuration;

    private static final String ERROR = "ERROR";
    private static CbExtLogger logger = new CbExtLogger(ElasticSearchRestHighImpl.class.getName());

    @Override
    public Map<String, Object> search(SearchDTO searchDTO, String index, RestHighLevelClient esClient) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();

            logger.debug("ElasticSearchRestHighImpl:search: method started at ==" + startTime);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            SearchRequest searchRequest = new SearchRequest(index);
            searchRequest.types(_DOC);

            // check mode and set constraints
            Map<String, Float> constraintsMap = ElasticSearchHelper.getConstraints(searchDTO);

            BoolQueryBuilder query = new BoolQueryBuilder();

            // add channel field as mandatory
            String channel = PropertiesCache.getInstance().getProperty(Constants.SUNBIRD_ES_CHANNEL);
            if (!(StringUtils.isBlank(channel) || Constants.SUNBIRD_ES_CHANNEL.equals(channel))) {
                query.must(ElasticSearchHelper.createMatchQuery(Constants.CHANNEL, channel, constraintsMap.get(Constants.CHANNEL)));
            }

            // apply simple query string
            if (!StringUtils.isBlank(searchDTO.getQuery())) {
                SimpleQueryStringBuilder sqsb = QueryBuilders.simpleQueryStringQuery(searchDTO.getQuery());
                query.must(sqsb);
                if (CollectionUtils.isNotEmpty(searchDTO.getQueryFields())) {
                    Map<String, Float> searchFields = searchDTO.getQueryFields().stream().collect(Collectors.<String, String, Float>toMap(s -> s, v -> 1.0f));
                    query.must(sqsb.fields(searchFields));
                }
            }
            // apply the sorting
            if (searchDTO.getSortBy() != null && searchDTO.getSortBy().size() > 0) {
                for (Map.Entry<String, Object> entry : searchDTO.getSortBy().entrySet()) {
                    if (!entry.getKey().contains(".")) {
                        searchSourceBuilder.sort(entry.getKey() + ElasticSearchHelper.RAW_APPEND, ElasticSearchHelper.getSortOrder((String) entry.getValue()));
                    } else {
                        Map<String, Object> map = (Map<String, Object>) entry.getValue();
                        Map<String, String> dataMap = (Map) map.get(Constants.TERM);
                        for (Map.Entry<String, String> dateMapEntry : dataMap.entrySet()) {
                            FieldSortBuilder mySort = new FieldSortBuilder(entry.getKey() + ElasticSearchHelper.RAW_APPEND).setNestedFilter(new TermQueryBuilder(dateMapEntry.getKey(), dateMapEntry.getValue())).sortMode(SortMode.MIN).order(ElasticSearchHelper.getSortOrder((String) map.get(Constants.ORDER)));
                            searchSourceBuilder.sort(mySort);
                        }
                    }
                }
            }

            // apply the fields filter
            searchSourceBuilder.fetchSource(searchDTO.getFields() != null ? searchDTO.getFields().stream().toArray(String[]::new) : null, searchDTO.getExcludedFields() != null ? searchDTO.getExcludedFields().stream().toArray(String[]::new) : null);

            // setting the offset
            if (searchDTO.getOffset() != null) {
                searchSourceBuilder.from(searchDTO.getOffset());
            }

            // setting the limit
            if (searchDTO.getLimit() != null) {
                searchSourceBuilder.size(searchDTO.getLimit());
            }
            // apply additional properties
            if (searchDTO.getAdditionalProperties() != null && searchDTO.getAdditionalProperties().size() > 0) {
                for (Map.Entry<String, Object> entry : searchDTO.getAdditionalProperties().entrySet()) {
                    ElasticSearchHelper.addAdditionalProperties(query, entry, constraintsMap);
                }
            }

            //apply multimatch on query
            if (searchDTO.getMultiSearchFields() != null && searchDTO.getMultiSearchFields().size() > 0) {
                for (Map.Entry<String, List<String>> entry : searchDTO.getMultiSearchFields().entrySet()) {
                    query.should(ElasticSearchHelper.createMultiMatchQuery(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]), null));
                }
            }

            // set final query to search request builder
            searchSourceBuilder.query(query);
            List finalFacetList = new ArrayList();

            if (null != searchDTO.getFacets() && !searchDTO.getFacets().isEmpty()) {
                searchSourceBuilder = addAggregations(searchSourceBuilder, searchDTO.getFacets());
            }
            logger.info("ElasticSearchRestHighImpl:search: calling search for index " + index + ", with query = " + searchSourceBuilder.toString());

            searchRequest.source(searchSourceBuilder);
            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            logger.debug("ElasticSearchRestHighImpl:search:onResponse  response1 = " + response);
            if (response.getHits() == null || response.getHits().getTotalHits() == 0) {
                List<Map<String, Object>> esSource = new ArrayList<>();
                responseMap.put(JsonKey.CONTENT, esSource);
                responseMap.put(JsonKey.COUNT, 0);
            } else {
                responseMap = ElasticSearchHelper.getSearchResponseMap(response, searchDTO, finalFacetList);
                logger.debug("ElasticSearchRestHighImpl:search: method end " + " ,Total time elapsed = " + calculateEndTime(startTime));
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return responseMap;
    }


    private static SearchSourceBuilder addAggregations(SearchSourceBuilder searchSourceBuilder, List<Map<String, String>> facets) {
        long startTime = System.currentTimeMillis();
        logger.debug(String.format("ElasticSearchRestHighImpl:addAggregations: method started at ==%d", startTime));
        Map<String, String> map = facets.get(0);
        for (Map.Entry<String, String> entry : map.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
            if (JsonKey.DATE_HISTOGRAM.equalsIgnoreCase(value)) {
                searchSourceBuilder.aggregation(AggregationBuilders.dateHistogram(key).field(key + ElasticSearchHelper.RAW_APPEND).dateHistogramInterval(DateHistogramInterval.days(1)));

            } else if (null == value) {
                searchSourceBuilder.aggregation(AggregationBuilders.terms(key).field(key + ElasticSearchHelper.RAW_APPEND));
            }
        }
        logger.debug("ElasticSearchRestHighImpl:addAggregations: method end ==" + " ,Total time elapsed = " + calculateEndTime(startTime));
        return searchSourceBuilder;
    }

    private static long calculateEndTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

}