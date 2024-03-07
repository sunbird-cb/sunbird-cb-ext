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
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.FracApiResponse;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.searchby.model.FracCommonInfo;
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
    @Autowired
    RedisCacheMgr redisCacheMgr;

    @Override
	public FracApiResponse getListPositions() {
		FracApiResponse response = new FracApiResponse();
		response.setStatusInfo(new FracStatusInfo());
		response.getStatusInfo().setStatusCode(HttpStatus.OK.value());
		try {
			Map<String, Object> propertyMap = new HashMap<>();
			propertyMap.put(Constants.CONTEXT_TYPE.toLowerCase(), Constants.POSITION);
			List<Map<String, Object>> listOfPosition = cassandraOperation.getRecordsByProperties(
					Constants.KEYSPACE_SUNBIRD, Constants.TABLE_MASTER_DATA, propertyMap, new ArrayList<>());
			List<MasterData> positionList = mapper.convertValue(listOfPosition, new TypeReference<List<MasterData>>() {
			});

			List<FracCommonInfo> positions = new ArrayList<>();
			if (!CollectionUtils.isEmpty(positionList)) {
				positionList.forEach(position -> {
					FracCommonInfo commonResponse = new FracCommonInfo(position.getId(), position.getContextName(),
							position.getContextData());
					positions.add(commonResponse);
				});
			}
			response.setResponseData(positions);
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
                    if (Constants.NATIONALITY.equalsIgnoreCase(type) || Constants.COUNTRIES.equalsIgnoreCase(type)) {
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
                    errMsg = "Failed to update details";
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
                    errMsg = "Failed to Create position";
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
        List<String> params = new ArrayList<>();
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
            Map<String, Object> properties = new HashMap<>();
            properties.put(Constants.CONTEXT_TYPE.toLowerCase(), Arrays.asList(contextTypes));
            listProperty.add(properties);

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
        degrees.put(Constants.GRADUATIONS, new ArrayList<>());
        degrees.put(Constants.POST_GRADUATIONS, new ArrayList<>());
        Map<String, List<Map<String, String>>> designations = new HashMap<>();
        designations.put(Constants.DESIGNATIONS, new ArrayList<>());
        designations.put(Constants.GRADE_PAY, new ArrayList<>());
        Map<String, List<Map<String, String>>> govtOrg = new HashMap<>();
        govtOrg.put(Constants.CADRE, new ArrayList<>());
        govtOrg.put(Constants.MINISTRIES, new ArrayList<>());
        govtOrg.put(Constants.SERVICE, new ArrayList<>());
        List<Map<String, String>> industries = new ArrayList<>();

        for (Map<String, Object> context : contexts) {
            String contextType = (String) context.get(Constants.CONTEXT_TYPE);
            String contextName = (String) context.get(Constants.CONTEXT_NAME);
            Map<String, String> contextMap = new HashMap<>();
            contextMap.put("name", contextName);

            switch (contextType) {
                case Constants.GRADUATIONS:
                    degrees.get(Constants.GRADUATIONS).add(contextMap);
                    break;
                case Constants.POST_GRADUATIONS:
                    degrees.get(Constants.POST_GRADUATIONS).add(contextMap);
                    break;
                case Constants.DESIGNATIONS:
                    designations.get(Constants.DESIGNATIONS).add(contextMap);
                    break;
                case Constants.GRADE_PAY:
                    designations.get(Constants.GRADE_PAY).add(contextMap);
                    break;
                case Constants.CADRE:
                    govtOrg.get(Constants.CADRE).add(contextMap);
                    break;
                case Constants.MINISTRIES:
                    govtOrg.get(Constants.MINISTRIES).add(contextMap);
                    break;
                case Constants.SERVICE:
                    govtOrg.get(Constants.SERVICE).add(contextMap);
                    break;
                case "industries":
                    industries.add(contextMap);
                    break;
                default:
                    break;
            }
        }
        transformed.put("degrees", degrees);
        transformed.put(Constants.DESIGNATIONS, designations);
        transformed.put("govtOrg", govtOrg);
        transformed.put("industries", industries);
        return transformed;
    }
    
	private void enrichFracPositions(List<MasterData> positionList, String userToken) {
		Map<String, String> headers = new HashMap<>();
		HashMap<String, Object> reqBody = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, Constants.BEARER + userToken);
        List<Map<String, Object>> searchList = new ArrayList<>();
		Map<String, Object> compSearchObj = new HashMap<>();
		compSearchObj.put(Constants.TYPE, Constants.POSITION.toUpperCase());
		compSearchObj.put(Constants.FIELD, Constants.NAME);
		compSearchObj.put(Constants.KEYWORD, StringUtils.EMPTY);
		searchList.add(compSearchObj);

		compSearchObj = new HashMap<>();
		compSearchObj.put(Constants.TYPE, Constants.POSITION.toUpperCase());
		compSearchObj.put(Constants.KEYWORD, Constants.VERIFIED);
		compSearchObj.put(Constants.FIELD, Constants.STATUS);
		searchList.add(compSearchObj);

		reqBody.put(Constants.SEARCHES, searchList);

		List<String> positionNameList = new ArrayList<>();
		Map<String, Object> fracSearchRes = outboundRequestHandlerService.fetchResultUsingPost(
				cbExtServerProperties.getFracHost() + cbExtServerProperties.getFracSearchPath(), reqBody, headers);
		List<Map<String, Object>> fracResponseList = (List<Map<String, Object>>) fracSearchRes
				.get(Constants.RESPONSE_DATA);
		if (!CollectionUtils.isEmpty(fracResponseList)) {
			for (Map<String, Object> respObj : fracResponseList) {
				if (!positionNameList.contains(respObj.get(Constants.CONTEXT_NAME.toLowerCase()))) {
					positionList.add(new MasterData((String) respObj.get(Constants.ID), Constants.POSITION,
							(String) respObj.get(Constants.NAME), (String) respObj.get(Constants.DESCRIPTION)));
					positionNameList.add((String) respObj.get(Constants.NAME));
				}
			}
		} else {
			logger.info("Failed to get position info from FRAC API");
		}
	}

    public SBApiResponse getDeptPositions(String userOrgId) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_V2_READ_DEPT_POSITION);
        if (userOrgId == null || userOrgId.isEmpty()) {
            String errMsg = "X-AUTH Org-Id is Empty";
            logger.info(errMsg);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        } else {
            List<String> deptPositionList = redisCacheMgr.hget(Constants.ORG_DESIGNATION,
                    cbExtServerProperties.getRedisInsightIndex(), userOrgId);
            if (!CollectionUtils.isEmpty(deptPositionList) && StringUtils.isNotBlank(deptPositionList.get(0))) {
                String deptPosition = deptPositionList.get(0);
                deptPositionList = Arrays.asList(deptPosition.split(","));
            } else {
                deptPositionList = Collections.emptyList();
            }
            List<Map<String, String>> resultList = new ArrayList<>();
            for (String position : deptPositionList) {
                if (StringUtils.isNotBlank(position)) {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", position);
                    resultList.add(map);
                }
            }

            Map<String, Object> responseData = new HashMap<>();

            responseData.put(Constants.COUNT, resultList.size());
            responseData.put(Constants.CONTENT, resultList);
            response.getResult().put(Constants.RESPONSE, responseData);
        }
        return response;
    }

    public SBApiResponse retrieveDeptPositionByAdmin(Map<String, Object> request) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_V2_READ_DEPT_POSITION);
        String errMsg = validateRequestForPosition(request);
        if (StringUtils.isNotBlank(errMsg)) {
            logger.error(errMsg);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }

        try {
            Map<String, Object> requestObj = (Map<String, Object>) request.get(Constants.REQUEST);
            List<String> orgIdList = (List<String>) requestObj.get(Constants.ORG_ID_LIST);
            Set<String> positionList = new HashSet<String>();
            List<Map<String, String>> resultList = new ArrayList<>();
            for (String orgId : orgIdList) {
                List<String> deptPositionList = redisCacheMgr.hget(Constants.ORG_DESIGNATION,
                        cbExtServerProperties.getRedisInsightIndex(), orgId);
                if (!CollectionUtils.isEmpty(deptPositionList) && StringUtils.isNotBlank(deptPositionList.get(0))) {
                    String deptPosition = deptPositionList.get(0);
                    deptPositionList = Arrays.asList(deptPosition.split(","));
                } else {
                    deptPositionList = Collections.emptyList();
                }

                for (String position : deptPositionList) {
                    if (StringUtils.isNotBlank(position)) {
                        positionList.add(position);
                    }
                }
            }

            for (String position : positionList) {
                Map<String, String> map = new HashMap<>();
                map.put("name", position);
                resultList.add(map);
            }

            Map<String, Object> responseData = new HashMap<>();

            responseData.put(Constants.COUNT, resultList.size());
            responseData.put(Constants.CONTENT, resultList);
            response.getResult().put(Constants.RESPONSE, responseData);
        } catch (Exception e) {
            errMsg = String.format("Failed to get positions for orgIdList. Exception: %s", e.getMessage());
            logger.error(errMsg);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private String validateRequestForPosition(Map<String, Object> request) {
        String errMsg = "";
        Map<String, Object> requestObj = (Map<String, Object>) request.get(Constants.REQUEST);
        if (ObjectUtils.isEmpty(requestObj)) {
            errMsg = "Request object is not proper.";
        } else {
            List<String> orgIdList = (List<String>) requestObj.get(Constants.ORG_ID_LIST);
            if (CollectionUtils.isEmpty(orgIdList)) {
                errMsg = "orgIdList is empty in request";
            }
        }
        return errMsg;
    }
}
