package org.sunbird.searchby.service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraConnectionManager;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.searchby.model.FracApiResponseV2;
import org.sunbird.searchby.model.FracCommonInfo;
import org.sunbird.searchby.model.MasterData;
import org.sunbird.workallocation.model.FracStatusInfo;

import java.util.*;

@Service
public class MasterDataServiceImpl implements MasterDataService {

    private CbExtLogger logger = new CbExtLogger(getClass().getName());
    @Autowired
    CbExtServerProperties cbExtServerProperties;
    @Autowired
    RedisCacheMgr redisCacheMgr;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Autowired
    CassandraOperation cassandraOperation;
    @Autowired
    CassandraConnectionManager cassandraConnectionManager;

    @Override
    public FracApiResponseV2 getListPositions(String userToken) {
        FracApiResponseV2 response = new FracApiResponseV2();
        response.setStatusInfo(new FracStatusInfo());
        response.getStatusInfo().setStatusCode(HttpStatus.OK.value());
        try {
            Map<String, List<MasterData>> positionMap = new HashMap<>();
            String strPositionMap = redisCacheMgr.getCache(Constants.POSITIONS_CACHE_NAME);
            if (!StringUtils.isEmpty(strPositionMap)) {
                positionMap = mapper.readValue(strPositionMap, new TypeReference<Map<String, List<FracCommonInfo>>>() {
                });
            }

            if (ObjectUtils.isEmpty(positionMap)
                    || CollectionUtils.isEmpty(positionMap.get(Constants.POSITIONS_CACHE_NAME))) {
                logger.info("Initializing / Refreshing the Cache value for key : " + Constants.POSITIONS_CACHE_NAME);
                try {
                    positionMap = updateDesignationDetails(userToken);
                    response.setResponseData(positionMap.get(Constants.POSITIONS_CACHE_NAME));
                } catch (Exception e) {
                    logger.error(e);
                    response.getStatusInfo().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.getStatusInfo().setErrorMessage(e.getMessage());
                }
            } else {
                response.setResponseData(positionMap.get(Constants.POSITIONS_CACHE_NAME));
            }
        } catch (Exception e) {
            response.getStatusInfo().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    private Map<String, List<MasterData>> updateDesignationDetails(String authUserToken) throws Exception {
        Map<String, String> headers = new HashMap<>();
        HashMap<String, Object> reqBody = new HashMap<>();
        headers = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, Constants.BEARER + authUserToken);
        reqBody = new HashMap<>();
        List<Map<String, Object>> searchList = new ArrayList<>();
        Map<String, Object> compSearchObj = new HashMap<>();
        compSearchObj.put(Constants.TYPE, Constants.POSITION.toUpperCase());
        compSearchObj.put(Constants.FIELD, Constants.NAME);
        compSearchObj.put(Constants.KEYWORD, StringUtils.EMPTY);
        searchList.add(compSearchObj);

        compSearchObj = new HashMap<String, Object>();
        compSearchObj.put(Constants.TYPE, Constants.POSITION.toUpperCase());
        compSearchObj.put(Constants.KEYWORD, Constants.VERIFIED);
        compSearchObj.put(Constants.FIELD, Constants.STATUS);
        searchList.add(compSearchObj);

        reqBody.put(Constants.SEARCHES, searchList);

        List<String> positionNameList = new ArrayList<String>();
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.CONTEXT_TYPE.toLowerCase(), Constants.POSITION);
        List<Map<String, Object>> listOfPosition = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_MASTER_DATA, propertyMap, new ArrayList<>());
        List<MasterData> positionList = mapper.convertValue(listOfPosition, new TypeReference<List<MasterData>>() {
        });
        Map<String, Object> fracSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
                cbExtServerProperties.getFracHost() + cbExtServerProperties.getFracSearchPath(), reqBody, headers);
        List<Map<String, Object>> fracResponseList = (List<Map<String, Object>>) fracSearchRes
                .get(Constants.RESPONSE_DATA);
        if (!CollectionUtils.isEmpty(fracResponseList)) {
            for (Map<String, Object> respObj : fracResponseList) {
                if (!positionNameList.contains((String) respObj.get(Constants.CONTEXT_NAME.toLowerCase()))) {
                    positionList.add(new MasterData((String) respObj.get(Constants.ID), Constants.POSITION,
                            (String) respObj.get(Constants.NAME), (String) respObj.get(Constants.DESCRIPTION)));
                    positionNameList.add((String) respObj.get(Constants.NAME));
                }
            }
        } else {
            Exception err = new Exception("Failed to get position info from FRAC API.");
            logger.error(err);
            try {
                logger.info("Received Response: " + (new ObjectMapper()).writeValueAsString(fracSearchRes));
            } catch (Exception e) {
            }
            throw err;
        }
        Map<String, List<MasterData>> positionMap = new HashMap<String, List<MasterData>>();
        positionMap.put(Constants.POSITIONS_CACHE_NAME, positionList);
        redisCacheMgr.putCache(Constants.POSITIONS_CACHE_NAME, positionMap);
        return positionMap;
    }

    @Override
    public SBApiResponse create(Map<String, Object> request) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_POSITION_CREATE);
        String errMsg = validateRequest(request);
        if (!StringUtils.isEmpty(errMsg)) {
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        try {
            Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
            Select select = QueryBuilder.select().max(Constants.ID).from(Constants.TABLE_MASTER_DATA)
                    .where(QueryBuilder.eq(Constants.CONTEXT_TYPE, requestBody.get(Constants.CONTEXT_TYPE))).allowFiltering();
            ResultSet result = cassandraConnectionManager.getSession(Constants.SUNBIRD_KEY_SPACE_NAME).execute(select);
            String currentMaxIdString = result.one().getString(0);
            long currentMaxId = Long.parseLong(currentMaxIdString);
            long nextId = currentMaxId + 1;
            requestBody.put(Constants.ID, Long.toString(nextId));
            response = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, requestBody);
            if (!Constants.SUCCESS.equalsIgnoreCase((String) response.get(Constants.RESPONSE))) {
                errMsg = String.format("Failed to Create position");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.getParams().setErrmsg(errMsg);
                return response;
            }
        } catch (Exception e) {
            logger.info("Exception occurred while performing create operation " + e.getMessage());
            errMsg = "Exception occurred while performing create operation";
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        response.setResponseCode(HttpStatus.OK);
        response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
        response.getParams().setStatus(Constants.SUCCESS);
        return response;
    }

    @Override
    public SBApiResponse update(Map<String, Object> request, String id, String type) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_POSITION_UPDATE);
        String errMsg = validateUpdateRequest(request, id, type);
        if (!StringUtils.isEmpty(errMsg)) {
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        Map<String, Object> requestBody = (Map<String, Object>) request.get(Constants.REQUEST);
        Map<String, Object> compositeKey = new HashMap<>();
        compositeKey.put(Constants.ID, id);
        compositeKey.put(Constants.CONTEXT_TYPE, type);
        Map<String, Object> updateResponse = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, requestBody, compositeKey);
        if (updateResponse != null
                && !Constants.SUCCESS.equalsIgnoreCase((String) updateResponse.get(Constants.RESPONSE))) {
            errMsg = String.format("Failed to update details for Id : %s", requestBody.get(Constants.ID));
            response.getParams().setErrmsg(errMsg);
            return response;
        }
        response.setResponseCode(HttpStatus.OK);
        response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
        response.getParams().setStatus(Constants.SUCCESS);
        return response;
    }

    private String validateRequest(Map<String, Object> requestData) {
        List<String> params = new ArrayList<String>();
        StringBuilder strBuilder = new StringBuilder();
        Map<String, Object> request = (Map<String, Object>) requestData.get(Constants.REQUEST);
        if (ObjectUtils.isEmpty(request)) {
            strBuilder.append("Request object is empty.");
            return strBuilder.toString();
        }
        if (StringUtils.isEmpty((String) request.get(Constants.CONTEXT_TYPE))) {
            params.add(Constants.CONTEXT_TYPE);
        }
        if (ObjectUtils.isEmpty(request.get(Constants.CONTEXT_NAME))) {
            params.add(Constants.CONTEXT_NAME);
        }
        if (ObjectUtils.isEmpty(request.get(Constants.CONTEXT_DATA))) {
            params.add(Constants.CONTEXT_DATA);
        }
        if (!params.isEmpty()) {
            strBuilder.append("Invalid Request. Missing params - " + params);
        }
        return strBuilder.toString();
    }

    private String validateUpdateRequest(Map<String, Object> requestData, String id, String type) {
        List<String> params = new ArrayList<String>();
        StringBuilder strBuilder = new StringBuilder();
        Map<String, Object> request = (Map<String, Object>) requestData.get(Constants.REQUEST);
        if (ObjectUtils.isEmpty(request)) {
            strBuilder.append("Request object is empty.");
            return strBuilder.toString();
        }
        if (StringUtils.isEmpty(id)) {
            params.add(Constants.ID);
        }
        if (ObjectUtils.isEmpty(type)) {
            params.add(Constants.TYPE);
        }
        if (!params.isEmpty()) {
            strBuilder.append("Missing request params - " + params);
        }
        return strBuilder.toString();
    }

    public SBApiResponse getMasterDataByType(String type) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_GET_MASTER_DATA);
        String errMsg = null;
        if (StringUtils.isEmpty(type)) {
            errMsg = "Type can not be empty";
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            response.getParams().setErrmsg(errMsg);
            return response;
        }
        List<Map<String, Object>> listOfMasterData = null;
        try {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.CONTEXT_TYPE.toLowerCase(), type);
            listOfMasterData = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_MASTER_DATA, propertyMap, new ArrayList<>());
            if (CollectionUtils.isEmpty(listOfMasterData)) {
                errMsg = String.format("No details available inside db!");
                response.getParams().setErrmsg(errMsg);
                return response;
            }
        } catch (Exception e) {
            logger.info("Exception occurred while fetching details from the database");
            errMsg = "Exception occurred while fetching details from the database";
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        response.setResponseCode(HttpStatus.OK);
        response.getResult().put(Constants.RESPONSE, Constants.SUCCESS);
        response.getResult().put(Constants.CONTENT, listOfMasterData);
        response.getParams().setStatus(Constants.SUCCESS);
        return response;
    }
}
