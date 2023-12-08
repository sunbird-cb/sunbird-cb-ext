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
import org.sunbird.cbp.model.CbPlan;
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
import java.util.*;

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
            requestMap.put(Constants.ID, UUIDs.timeBased());
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
            CbPlan cbPlan = new CbPlan();
            cbPlan.setCreatedBy(userId);
            cbPlan.setCreatedAt(new Date());
            cbPlan.setOrgId(userOrgId);
            CbPlanDto cbPlanDto = mapper.convertValue(request.getRequest(), CbPlanDto.class);
            List<String> validations = validateCbPlanRequest(cbPlanDto);
            if (CollectionUtils.isNotEmpty(validations)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(mapper.writeValueAsString(validations));
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            try {
                cbPlan.setDraftData(mapper.writeValueAsString(cbPlanDto));
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
            UUID cbPlanId = (UUID)requestData.get(Constants.ID);
            String comment = (String)requestData.get(Constants.COMMENT);
            if (cbPlanId == null) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("CbPlanId is missing.");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Optional<CbPlan> cbPlan = null;
            if (cbPlan.isPresent()) {
                CbPlanDto cbPlanDto = mapper.convertValue(cbPlan.get().getDraftData(), CbPlanDto.class);
                updateCbPlanData(cbPlan.get(), cbPlanDto);
                cbPlan.get().setPublishedBy(userId);
                if (StringUtils.isNoneBlank(comment)) {
                    cbPlan.get().setComment(comment);
                }
                cbPlan.get().setPublishedAt(new Date());
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
        return "fb0b3a03-d050-4b75-86ec-0354331a6b22";
        //return accessTokenValidator.fetchUserIdFromAccessToken(authUserToken);
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

    private void updateCbPlanData(CbPlan cbPlan, CbPlanDto planDto) {
        cbPlan.setName(planDto.getName());
        cbPlan.setAssignmentType(planDto.getAssignmentType());
        cbPlan.setAssignmentTypeInfo(planDto.getAssignmentTypeInfo());
        cbPlan.setDraftData(null);
        cbPlan.setContentType(planDto.getContentType());
        cbPlan.setContentList(planDto.getContentList());
        cbPlan.setEndDate(planDto.getEndDate());
        cbPlan.setStatus(Constants.LIVE);
    }
}
