package org.sunbird.searchby.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.searchby.model.PositionListResponse;
import org.sunbird.searchby.model.MasterData;
import org.sunbird.workallocation.model.FracStatusInfo;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterDataServiceImpl implements MasterDataService {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    CbExtServerProperties cbExtServerProperties;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;
    @Autowired
    CassandraOperation cassandraOperation;

    @Override
    public PositionListResponse getListPositions(String userToken) {
        PositionListResponse response = new PositionListResponse();
        response.setStatusInfo(new FracStatusInfo());
        response.getStatusInfo().setStatusCode(HttpStatus.OK.value());
        try {
            Map<String, String> headers = new HashMap<>();
            HashMap<String, Object> reqBody = new HashMap<>();
            headers = new HashMap<>();
            headers.put(Constants.AUTHORIZATION, Constants.BEARER + userToken);
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
                logger.info("Failed to get position info from FRAC API");
            }
            response.setResponseData(positionList);
        } catch (Exception e) {
            logger.error("Failed to get positions details");
            response.getStatusInfo().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public Map<String,Object> getMasterDataByType(String type) {
        Map<String,Object> response = new HashMap<>();
        String errMsg = null;
        List<Map<String, Object>> listOfMasterData = null;
        try {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(Constants.CONTEXT_TYPE.toLowerCase(), type);
            listOfMasterData = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_MASTER_DATA, propertyMap, new ArrayList<>());
            if (CollectionUtils.isEmpty(listOfMasterData)) {
                response.put(Constants.ERROR_MESSAGE, "No details available inside db!");
                response.put(Constants.RESPONSE_CODE, HttpStatus.BAD_REQUEST);
                return response;
            }
        } catch (Exception e) {
            errMsg = "Exception occurred while fetching details from the database";
            logger.error(errMsg);
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.put(Constants.ERROR_MESSAGE, errMsg);
            response.put(Constants.RESPONSE_CODE, HttpStatus.BAD_REQUEST);
            return response;
        }
        List<Map<String, Object>> enrichResponse = enrichResponseBasedOnType(type, listOfMasterData);
        if (!CollectionUtils.isEmpty(enrichResponse)) {
            response.put(type, enrichResponse);
        }
        return response;
    }

    private List<Map<String, Object>> enrichResponseBasedOnType(String type, List<Map<String, Object>> listOfMasterData) {
        return listOfMasterData.stream()
                .map(masterData -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put(Constants.NAME, masterData.get(Constants.CONTEXT_NAME));
                    if (Constants.NATIONALITIES.equalsIgnoreCase(type)) {
                        result.put(Constants.COUNTRY_CODE, masterData.get(Constants.CONTEXT_DATA));
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    public SBApiResponse upsertMasterData(Map<String,Object> requestObj) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_POSITION_CREATE);
        Map<String, Object> masterData = (Map<String, Object>) requestObj.get(Constants.REQUEST);
        String errMsg = validateUpsertRequest(masterData);
        if (!StringUtils.isEmpty(errMsg)) {
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        try {
            Map<String, Object> request = new HashMap<>();
            request.put(Constants.CONTEXT_TYPE.toLowerCase(), masterData.get(Constants.CONTEXT_TYPE));
            request.put(Constants.CONTEXT_NAME.toLowerCase(), masterData.get(Constants.CONTEXT_NAME));
            List<Map<String, Object>> listOfMasterData = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, request, new ArrayList<>());

            if (!CollectionUtils.isEmpty(listOfMasterData)) {
                Map<String, Object> updateRequest = new HashMap<>();
                updateRequest.put(Constants.CONTEXT_DATA, masterData.get(Constants.CONTEXT_DATA));
                Map<String, Object> updateResponse = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, updateRequest, request);
                if (updateResponse != null
                        && !Constants.SUCCESS.equalsIgnoreCase((String) updateResponse.get(Constants.RESPONSE))) {
                    errMsg = String.format("Failed to update details");
                    response.getParams().setErrmsg(errMsg);
                    return response;
                }
            } else {
                Map<String, Object> propertyMap = new HashMap<>();
                propertyMap.put(Constants.CONTEXT_TYPE.toLowerCase(), masterData.get(Constants.CONTEXT_TYPE));
                List<Map<String, Object>> totalMasterData = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, propertyMap, new ArrayList<>());
                request.put(Constants.ID, Long.toString(totalMasterData.size() + 1));
                request.put(Constants.CONTEXT_DATA, masterData.get(Constants.CONTEXT_DATA));
                response = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, request);
                if (!Constants.SUCCESS.equalsIgnoreCase((String) response.get(Constants.RESPONSE))) {
                    errMsg = String.format("Failed to Create position");
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    response.getParams().setErrmsg(errMsg);
                    return response;
                }
            }
        } catch (Exception e) {
            errMsg = "Exception occurred while performing upsert operation";
            logger.error(errMsg, e);
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

    private String validateUpsertRequest(Map<String,Object> masterData) {
        List<String> params = new ArrayList<String>();
        StringBuilder strBuilder = new StringBuilder();
        if (ObjectUtils.isEmpty(masterData)) {
            strBuilder.append("Model object is empty.");
            return strBuilder.toString();
        }
        if (StringUtils.isEmpty((String) masterData.get(Constants.CONTEXT_TYPE))) {
            params.add(Constants.CONTEXT_TYPE);
        }
        if (ObjectUtils.isEmpty(masterData.get(Constants.CONTEXT_NAME))) {
            params.add(Constants.CONTEXT_NAME);
        }
        if (!params.isEmpty()) {
            strBuilder.append("Invalid Request. Missing params - " + params);
        }
        return strBuilder.toString();
    }

    public Map<String,Object> getProfilePageMetaData() {
        Map<String,Object> response = new HashMap<>();
        String errMsg = null;
        Map<String, Object> transformed = null;
        try {
            String[] contextTypes = cbExtServerProperties.getContextTypes();
            List<Map<String, Object>> listProperty = new ArrayList<>();
            for (String contextType : contextTypes) {
                Map<String, Object> properties = new HashMap<>();
                properties.put(Constants.CONTEXT_TYPE.toLowerCase(), contextType);
                listProperty.add(properties);
            }
            List<Map<String, Object>> listOfMasterData = cassandraOperation.getRecordsWithInClause(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_MASTER_DATA, listProperty, new ArrayList<>());
            if (CollectionUtils.isEmpty(listOfMasterData)) {
                response.put(Constants.ERROR_MESSAGE, "No details available inside db!");
                response.put(Constants.RESPONSE_CODE, HttpStatus.BAD_REQUEST);
                return response;
            }
            transformed = createDesiredResponse(listOfMasterData);
        } catch (Exception e) {
            errMsg = "Exception occurred while fetching details from the database";
            logger.error(errMsg, e);
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.put(Constants.ERROR_MESSAGE, errMsg);
            response.put(Constants.RESPONSE_CODE, HttpStatus.BAD_REQUEST);
            return response;
        } else {
            return transformed;
        }
    }

    private Map<String, Object> createDesiredResponse(List<Map<String, Object>> contexts) {
        Map<String, Object> transformed = new HashMap<>();
        Map<String, List<Map<String, String>>> degrees = new HashMap<>();
        degrees.put("graduations", new ArrayList<>());
        degrees.put("postGraduations", new ArrayList<>());
        Map<String, List<Map<String, String>>> designations = new HashMap<>();
        designations.put("designations", new ArrayList<>());
        designations.put("gradePay", new ArrayList<>());
        Map<String, List<Map<String, String>>> govtOrg = new HashMap<>();
        govtOrg.put("cadre", new ArrayList<>());
        govtOrg.put("ministries", new ArrayList<>());
        govtOrg.put("service", new ArrayList<>());
        List<Map<String, String>> industries = new ArrayList<>();

        for (Map<String, Object> context : contexts) {
            String contextType = (String) context.get(Constants.CONTEXT_TYPE);
            String contextName = (String) context.get(Constants.CONTEXT_NAME);
            Map<String, String> contextMap = new HashMap<>();
            contextMap.put("name", contextName);

            switch (contextType) {
                case "graduations":
                    degrees.get("graduations").add(contextMap);
                    break;
                case "postGraduations":
                    degrees.get("postGraduations").add(contextMap);
                    break;
                case "designations":
                    designations.get("designations").add(contextMap);
                    break;
                case "gradePay":
                    designations.get("gradePay").add(contextMap);
                    break;
                case "cadre":
                    govtOrg.get("cadre").add(contextMap);
                    break;
                case "ministries":
                    govtOrg.get("ministries").add(contextMap);
                    break;
                case "service":
                    govtOrg.get("service").add(contextMap);
                    break;
                case "industries":
                    industries.add(contextMap);
                    break;
                default:
                    break;
            }
        }
        transformed.put("degrees", degrees);
        transformed.put("designations", designations);
        transformed.put("govtOrg", govtOrg);
        transformed.put("industries", industries);
        return transformed;
    }
}
