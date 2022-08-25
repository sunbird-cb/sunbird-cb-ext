package org.sunbird.common.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class IndexerService {

    private Logger logger = LoggerFactory.getLogger(IndexerService.class);

    @Autowired
    @Qualifier("esClient")
    private RestHighLevelClient esClient;
    
    @Autowired
    @Qualifier("sbEsClient")
    private RestHighLevelClient sbEsClient;

    /**
     * @param index         name of index
     * @param indexType     index type
     * @param entityId      entity Id
     * @param indexDocument index Document
     * @return status
     */
    public RestStatus addEntity(String index, String indexType, String entityId, Map<String, Object> indexDocument) {
        logger.info("addEntity starts with index {} and entityId {}", index, entityId);
        IndexResponse response = null;
        try {
            if(!StringUtils.isEmpty(entityId)){
                response = esClient.index(new IndexRequest(index, indexType, entityId).source(indexDocument), RequestOptions.DEFAULT);
            }else{
                response = esClient.index(new IndexRequest(index, indexType).source(indexDocument), RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            logger.error("Exception in adding record to ElasticSearch", e);
        }
        if (null == response)
            return null;
        return response.status();
    }

    /**
     * @param index         name of index
     * @param indexType     index type
     * @param entityId      entity Id
     * @param indexDocument index Document
     * @return status
     */
    public RestStatus updateEntity(String index, String indexType, String entityId, Map<String, ?> indexDocument) {
        logger.info("updateEntity starts with index {} and entityId {}", index, entityId);
        UpdateResponse response = null;
        try {
            response = esClient.update(new UpdateRequest(index.toLowerCase(), indexType, entityId).doc(indexDocument), RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Exception in updating a record to ElasticSearch", e);
        }
        if (null == response)
            return null;
        return response.status();
    }

    /**
     * @param index         name of index
     * @param indexType     index type
     * @param entityId      entity Id
     * @return status
     */
    public Map<String, Object> readEntity(String index, String indexType, String entityId){
        logger.info("readEntity starts with index {} and entityId {}", index, entityId);
        GetResponse response = null;
        try {
        response = esClient.get(new GetRequest(index, indexType, entityId), RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Exception in getting the record from ElasticSearch", e);
        }
        if(null == response)
            return null;
        return response.getSourceAsMap();
    }

    /**
     * Search the document in es based on provided information
     *
     * @param indexName           es index name
     * @param type                index type
     * @param searchSourceBuilder source builder
     * @return es search response
     * @throws IOException
     */
    public SearchResponse getEsResult(String indexName, String type, SearchSourceBuilder searchSourceBuilder, boolean isSunbirdES) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        if (!StringUtils.isEmpty(type))
            searchRequest.types(type);
        searchRequest.source(searchSourceBuilder);
        return getEsResult(searchRequest, isSunbirdES);
    }

    public RestStatus BulkInsert(List<IndexRequest> indexRequestList) {
        BulkResponse restStatus = null;
        if (!CollectionUtils.isEmpty(indexRequestList)) {
            BulkRequest bulkRequest = new BulkRequest();
            indexRequestList.forEach(bulkRequest::add);
            try {
                restStatus = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                logger.error("Exception while doing the bulk operation in ElasticSearch", e);
            }
        }
        if(null == restStatus)
            return null;
        return restStatus.status();
    }
    
	public long getDocumentCount(String index, SearchSourceBuilder searchSourceBuilder) {
		try {
			CountRequest countRequest = new CountRequest().indices(index);
			countRequest.source(searchSourceBuilder);
			CountResponse countResponse = esClient.count(countRequest, RequestOptions.DEFAULT);
			return countResponse.getCount();
		} catch (Exception e) {
			logger.error(String.format("Exception in getDocumentCount: %s", e.getMessage()));
			return 0l;
		}
	}
	
	private SearchResponse getEsResult(SearchRequest searchRequest, boolean isSbES) throws IOException {
		if(isSbES) {
			return sbEsClient.search(searchRequest, RequestOptions.DEFAULT);
		} else {
			return esClient.search(searchRequest, RequestOptions.DEFAULT);
		}
	}
}
