package org.sunbird.cbp.service;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.cbp.model.dto.CbPlanDto;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CbPlanServiceImpl implements CbPlanService {

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Autowired
    CassandraOperation cassandraOperation;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public SBApiResponse createCbPlan(SunbirdApiRequest request, String userOrgId, String authUserToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CB_PLAN_CREATE);
        try {
            String userId = validateAuthTokenAndFetchUserId(authUserToken);
            if (StringUtils.isBlank(userId)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.USER_ID_DOESNT_EXIST);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put(Constants.CREATED_BY, userId);
            requestMap.put(Constants.CREATED_AT, new Date());
            requestMap.put(Constants.ORG_ID, userOrgId);
            UUID cbPlanId = UUIDs.timeBased();
            requestMap.put(Constants.ID, cbPlanId);
            CbPlanDto cbPlanDto = mapper.convertValue(request.getRequest(), CbPlanDto.class);
            List<String> validations = validateCbPlanRequest(cbPlanDto);
            if (CollectionUtils.isNotEmpty(validations)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(mapper.writeValueAsString(validations));
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            try {
                requestMap.put(Constants.DRAFT_DATA, mapper.writeValueAsString(cbPlanDto));
                requestMap.put(Constants.STATUS, Constants.DRAFT);
                cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, requestMap);
                response.getResult().put(Constants.ID, cbPlanId);
            } catch (JsonProcessingException e) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(e.getMessage());
                response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse updateCbPlan(SunbirdApiRequest request, String userOrgId, String authUserToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CB_PLAN_UPDATE);
        try {
            String userId = validateAuthTokenAndFetchUserId(authUserToken);
            if (StringUtils.isBlank(userId)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.USER_ID_DOESNT_EXIST);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> updatedCbPlan = (Map<String, Object>) request.getRequest();
            if (updatedCbPlan.get(Constants.ID) != null) {
                UUID cbPlanId = UUID.fromString((String) updatedCbPlan.get(Constants.ID));
                Map<String, Object> cbPlanInfo = new HashMap<>();
                cbPlanInfo.put(Constants.ID, cbPlanId);
                cbPlanInfo.put(Constants.ORG_ID, userOrgId);
                List<Map<String, Object>> cbPlanMapInfo = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanInfo, null);
                if (CollectionUtils.isNotEmpty(cbPlanMapInfo)) {
                    String draftInfo = updateDraftInfo(updatedCbPlan, cbPlanMapInfo.get(0));
                    Map<String, Object> updatedCbPlanData = new HashMap<>();
                    updatedCbPlanData.put(Constants.DRAFT_DATA, draftInfo);
                    updatedCbPlanData.put(Constants.UPDATED_BY, userId);
                    updatedCbPlanData.put(Constants.UPDATED_AT, new Date());
                    updatedCbPlanData.put(Constants.STATUS, Constants.DRAFT);

                    Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, updatedCbPlanData, cbPlanInfo);
                    if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                        response.getResult().put(Constants.STATUS, Constants.UPDATED);
                        response.getResult().put(Constants.MESSAGE, "updated cbPlan for cbPlanId: " + cbPlanId);
                    } else {
                        response.getParams().setStatus(Constants.FAILED);
                        response.getParams().setErrmsg((String) resp.get(Constants.ERROR_MESSAGE) + "for cbPlanId: " + cbPlanId);
                        response.setResponseCode(HttpStatus.BAD_REQUEST);
                    }
                } else {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("cbPlan is not found for id: " + cbPlanId);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                }
            } else {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("Required Param id is missing");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse publishCbPlan(SunbirdApiRequest request, String userOrgId, String authUserToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CB_PLAN_PUBLISH);
        Map<String, Object> requestData = (Map<String, Object>) request.getRequest();
        try {
            String userId = validateAuthTokenAndFetchUserId(authUserToken);
            if (StringUtils.isBlank(userId)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.USER_ID_DOESNT_EXIST);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            UUID cbPlanId = UUID.fromString((String) requestData.get(Constants.ID));
            String comment = (String) requestData.get(Constants.COMMENT);
            if (cbPlanId == null) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("CbPlanId is missing.");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> cbPlanInfo = new HashMap<>();
            cbPlanInfo.put(Constants.ID, cbPlanId);
            cbPlanInfo.put(Constants.ORG_ID, userOrgId);
            List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByPropertiesWithoutFiltering(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanInfo, null);

            if (CollectionUtils.isNotEmpty(cbPlanMap)) {
                Map<String, Object> cbPlan = cbPlanMap.get(0);
                if (Constants.LIVE.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))) {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("CbPlan is already published for ID: " + cbPlanId);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }
                CbPlanDto cbPlanDto = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA), CbPlanDto.class);
                updateCbPlanData(cbPlan, cbPlanDto);
                cbPlan.put(Constants.CB_PUBLISHED_BY, userId);
                if (StringUtils.isNoneBlank(comment)) {
                    cbPlan.put(Constants.COMMENT, comment);
                }
                cbPlan.remove(Constants.ID);
                cbPlan.remove(Constants.ORG_ID);
                cbPlan.put(Constants.CB_PUBLISHED_AT, new Date());
                Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlan, cbPlanInfo);
                if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                    updateCbPlanLookupInfo(cbPlanDto, userOrgId, cbPlanId);
                    response.getResult().put(Constants.STATUS, Constants.UPDATED);
                    response.getResult().put(Constants.MESSAGE, "Published cbPlan for cbPlanId: " + cbPlanId);
                } else {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg((String) resp.get(Constants.ERROR_MESSAGE) + "for cbPlanId: " + cbPlanId);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                }
            } else {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("CbPlan is not exist for ID: " + cbPlanId);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private String validateAuthTokenAndFetchUserId(String authUserToken) {
        //return "fb0b3a03-d050-4b75-86ec-0354331a6b22";
        return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
    }

    private List<String> validateCbPlanRequest(CbPlanDto cbPlanDto) {
        List<String> validationErrors = new ArrayList<>();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Set<ConstraintViolation<CbPlanDto>> violations = validator.validate(cbPlanDto);

        // Check for violations
        if (!violations.isEmpty()) {
            for (ConstraintViolation<CbPlanDto> violation : violations) {
                String errorMessage = "Validation Error: " + violation.getMessage();
                validationErrors.add(errorMessage);
            }
        }
        return validationErrors;
    }

    private void updateCbPlanData(Map<String, Object> cbPlan, CbPlanDto planDto) {
        cbPlan.put(Constants.NAME, planDto.getName());
        cbPlan.put(Constants.CB_ASSIGNMENT_TYPE, planDto.getAssignmentType());
        cbPlan.put(Constants.CB_ASSIGNMENT_TYPE_INFO, planDto.getAssignmentTypeInfo());
        cbPlan.put(Constants.DRAFT_DATA, null);
        cbPlan.put(Constants.CB_CONTENT_TYPE, planDto.getContentType());
        cbPlan.put(Constants.CB_CONTENT_LIST, planDto.getContentList());
        cbPlan.put(Constants.END_DATE, planDto.getEndDate());
        cbPlan.put(Constants.STATUS, Constants.LIVE);
    }

    private boolean updateCbPlanLookupInfo(CbPlanDto planDto, String orgId, UUID cbPlanId) {
        Map<String, Object> cbPlanInfo = new HashMap<>();
        cbPlanInfo.put(Constants.CB_PLAN_ID, cbPlanId);
        cbPlanInfo.put(Constants.ORG_ID, orgId);
        boolean isUpdatedLookup = false;
        List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN_LOOKUP, cbPlanInfo, null);
        List<String> cbPlanInfoInsertAssignmentKey = new ArrayList<>();
        List<String> cbPlanInfoRemoveAssignmentKey = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cbPlanMap)) {
            List<String> assignmentKeyInfoList = cbPlanMap.stream().map(c -> (String) c.get(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY)).collect(Collectors.toList());
            cbPlanInfoInsertAssignmentKey = planDto.getAssignmentTypeInfo().stream().filter(assignmentKeyInfo -> !assignmentKeyInfoList.contains(assignmentKeyInfo)).collect(Collectors.toList());
            cbPlanInfoRemoveAssignmentKey = assignmentKeyInfoList.stream().filter(key -> !planDto.getAssignmentTypeInfo().contains(key)).collect(Collectors.toList());
        } else {
            cbPlanInfoInsertAssignmentKey = planDto.getAssignmentTypeInfo();
        }
        for (String assignmentTypeInfo : cbPlanInfoInsertAssignmentKey) {
            Map<String, Object> lookupInfo = new HashMap<>();
            lookupInfo.put(Constants.ORG_ID, orgId);
            lookupInfo.put(Constants.CB_PLAN_ID, cbPlanId);
            lookupInfo.put(Constants.CB_ASSIGNMENT_TYPE, planDto.getAssignmentType());
            lookupInfo.put(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY, assignmentTypeInfo);
            lookupInfo.put(Constants.CB_CONTENT_TYPE, planDto.getContentType());
            lookupInfo.put(Constants.CB_CONTENT_LIST, planDto.getContentList());
            lookupInfo.put(Constants.END_DATE, planDto.getEndDate());
            lookupInfo.put(Constants.CB_IS_ACTIVE, true);
            SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN_LOOKUP, lookupInfo);
            if (!resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                isUpdatedLookup = false;
            } else {
                isUpdatedLookup = true;
            }
        }

        for (String assignmentTypeInfo : cbPlanInfoRemoveAssignmentKey) {
            Map<String, Object> compositeKey = new HashMap<>();
            compositeKey.put(Constants.ORG_ID, orgId);
            compositeKey.put(Constants.CB_PLAN_ID, cbPlanId);
            compositeKey.put(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY, assignmentTypeInfo);

            Map<String, Object> lookupInfoUpdated = new HashMap<>();
            lookupInfoUpdated.put(Constants.CB_IS_ACTIVE, false);
            Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN_LOOKUP, lookupInfoUpdated, compositeKey);
            if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                isUpdatedLookup = true;
            } else {
                isUpdatedLookup = false;
            }
        }
        return isUpdatedLookup;
    }

    private String updateDraftInfo(Map<String, Object> updatedCbPlan, Map<String, Object> cbPlan) throws IOException {
        String updatedDraftInfo = "";
        Map<String, Object> draftInfo = new HashMap<>();
        if (StringUtils.isBlank((String) cbPlan.get(Constants.DRAFT_DATA))) {
            draftInfo.put(Constants.NAME, updatedCbPlan.getOrDefault(Constants.NAME, cbPlan.get(Constants.NAME)));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE, updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE)));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE_INFO, updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO)));
            draftInfo.put(Constants.CB_CONTENT_TYPE, updatedCbPlan.getOrDefault(Constants.CB_CONTENT_TYPE, cbPlan.get(Constants.CB_CONTENT_TYPE)));
            draftInfo.put(Constants.CB_CONTENT_LIST, updatedCbPlan.getOrDefault(Constants.CB_CONTENT_LIST, cbPlan.get(Constants.CB_CONTENT_LIST)));
            draftInfo.put(Constants.END_DATE, updatedCbPlan.getOrDefault(Constants.END_DATE, cbPlan.get(Constants.END_DATE)));
        } else {
            CbPlanDto cbPlanDto = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA), CbPlanDto.class);
            draftInfo.put(Constants.NAME, updatedCbPlan.getOrDefault(Constants.NAME, cbPlanDto.getName()));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE, updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE, cbPlanDto.getAssignmentType()));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE_INFO, updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlanDto.getAssignmentTypeInfo()));
            draftInfo.put(Constants.CB_CONTENT_TYPE, updatedCbPlan.getOrDefault(Constants.CB_CONTENT_TYPE, cbPlanDto.getContentType()));
            draftInfo.put(Constants.CB_CONTENT_LIST, updatedCbPlan.getOrDefault(Constants.CB_CONTENT_LIST, cbPlanDto.getContentList()));
            draftInfo.put(Constants.END_DATE, updatedCbPlan.getOrDefault(Constants.END_DATE, cbPlanDto.getEndDate()));
        }
        return mapper.writeValueAsString(draftInfo);
    }
}