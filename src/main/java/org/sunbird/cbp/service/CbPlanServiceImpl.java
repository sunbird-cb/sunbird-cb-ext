package org.sunbird.cbp.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.cbp.model.CbPlan;
import org.sunbird.cbp.model.CbPlanSearch;
import org.sunbird.cbp.model.dto.CbPlanDto;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.service.ContentService;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.producer.Producer;
import org.sunbird.user.service.UserUtilityService;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CbPlanServiceImpl implements CbPlanService {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    UserUtilityService userUtilityService;

    @Autowired
    ContentService contentService;

    @Autowired
    CbExtServerProperties serverProperties;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    Producer kafkaProducer;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

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
            requestMap.put(Constants.UPDATED_AT, new Date());
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
                SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, requestMap);
                if (Constants.SUCCESS.equals(resp.get(Constants.RESPONSE))) {
                    response.getResult().put(Constants.STATUS, Constants.CREATED);
                    response.getResult().put(Constants.ID, cbPlanId);
                } else {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg(Constants.FAILED_TO_CREATE_CBLAN_FOR_ORGID + userOrgId + " message: " + resp.getParams().getErrmsg());
                    response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (JsonProcessingException e) {
                logger.error(Constants.FAILED_TO_CREATE_CBLAN_FOR_ORGID + userOrgId, e);
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(e.getMessage());
                response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error(Constants.FAILED_TO_CREATE_CBLAN_FOR_ORGID + userOrgId, e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse updateCbPlan(SunbirdApiRequest request, String userOrgId, String authUserToken, List<String> userRoles) {
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
                List<Map<String, Object>> cbPlanMapInfo = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                        Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanInfo, null);
                if (CollectionUtils.isNotEmpty(cbPlanMapInfo)) {
                    Map<String, Object> cbPlanInfoMap = cbPlanMapInfo.get(0);
                    if (!(userId.equals(cbPlanInfoMap.get(Constants.CREATED_BY)) ||
                            serverProperties.getCbPlanUpdatePublishAuthorizedRoles().stream().anyMatch(roles -> CollectionUtils.isNotEmpty(userRoles) && userRoles.contains(roles)))) {
                        response.getParams().setStatus(Constants.FAILED);
                        response.getParams().setErrmsg("Not Authorized to update cbp Plan");
                        response.setResponseCode(HttpStatus.BAD_REQUEST);
                        return response;
                    }
                    String draftInfo = null;
                    if (Constants.LIVE.equalsIgnoreCase((String) cbPlanInfoMap.get(Constants.STATUS))
                            && cbPlanInfoMap.get(Constants.CB_PUBLISHED_BY) != null) {
                        // check when the cbPlan is published, need to check only few field need to be
                        // modified.
                        List<String> allowedFieldForUpdate = Arrays.asList(Constants.NAME,
                                Constants.CB_ASSIGNMENT_TYPE_INFO, Constants.END_DATE, Constants.ID);
                        long keyNotAllowedCount = updatedCbPlan.keySet().stream()
                                .filter(key -> !allowedFieldForUpdate.contains(key)).count();
                        if (keyNotAllowedCount > 0) {
                            response.getParams().setStatus(Constants.FAILED);
                            response.getParams().setErrmsg("Allowed Field for update cbPlan are: " + Constants.NAME
                                    + ", " + Constants.CB_ASSIGNMENT_TYPE_INFO + ", " + Constants.END_DATE);
                            response.setResponseCode(HttpStatus.BAD_REQUEST);
                            return response;
                        }
                    } else {
                        draftInfo = updateDraftInfo(updatedCbPlan, cbPlanMapInfo.get(0));
                    }
                    Map<String, Object> updatedCbPlanData = new HashMap<>();
                    draftInfo = mapper.writeValueAsString(updatedCbPlan);
                    updatedCbPlanData.put(Constants.DRAFT_DATA, draftInfo);
                    updatedCbPlanData.put(Constants.UPDATED_BY, userId);
                    updatedCbPlanData.put(Constants.UPDATED_AT, new Date());

                    Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
                            Constants.TABLE_CB_PLAN, updatedCbPlanData, cbPlanInfo);
                    if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                        response.getResult().put(Constants.STATUS, Constants.UPDATED);
                        response.getResult().put(Constants.MESSAGE, "updated cbPlan for cbPlanId: " + cbPlanId);
                    } else {
                        response.getParams().setStatus(Constants.FAILED);
                        response.getParams()
                                .setErrmsg((String) resp.get(Constants.ERROR_MESSAGE) + Constants.FOR_CBPLANID + cbPlanId);
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
            logger.error("Failed to Update CB Plan for OrgId: " + userOrgId, e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse publishCbPlan(SunbirdApiRequest request, String userOrgId, String authUserToken, List<String> userRoles) {
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
                response.getParams().setErrmsg(Constants.CB_PLANID_MISSING);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> cbPlanInfo = new HashMap<>();
            cbPlanInfo.put(Constants.ID, cbPlanId);
            cbPlanInfo.put(Constants.ORG_ID, userOrgId);
            List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanInfo, null);

            if (CollectionUtils.isNotEmpty(cbPlanMap)) {
                Map<String, Object> cbPlan = cbPlanMap.get(0);
                if (!(userId.equals(cbPlan.get(Constants.CREATED_BY)) ||
                        serverProperties.getCbPlanUpdatePublishAuthorizedRoles().stream().anyMatch(roles -> CollectionUtils.isNotEmpty(userRoles) && userRoles.contains(roles)))) {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("Not Authorized to publish cbp Plan");
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }
                Map<String, Object> publishCbPlan = new HashMap<>();
                publishCbPlan.putAll(cbPlan);
                if ((Constants.LIVE.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))
                        && cbPlan.get(Constants.DRAFT_DATA) == null)
                        || Constants.CB_RETIRE.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))) {
                    response.getParams().setStatus(Constants.FAILED);
                    String errMsg = "CbPlan is already published for ID: " + cbPlanId;
                    if (Constants.CB_RETIRE.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))) {
                        errMsg = "CbPlan is already retired for ID: " + cbPlanId;
                    }
                    response.getParams().setErrmsg(errMsg);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }
                if (Constants.DRAFT.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))) {
                    CbPlanDto cbPlanDto = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA), CbPlanDto.class);
                    updateCbPlanData(cbPlan, cbPlanDto);
                } else {
                    Map<String, Object> cbPlanDtoMap = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA),
                            new TypeReference<Map<String, Object>>() {
                            });
                    cbPlan.put(Constants.NAME,
                            cbPlanDtoMap.getOrDefault(Constants.NAME, publishCbPlan.get(Constants.NAME)));
                    cbPlan.put(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlanDtoMap.getOrDefault(
                            Constants.CB_ASSIGNMENT_TYPE_INFO, publishCbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO)));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    Date endDate = null;
                    try {
                        endDate = dateFormat.parse(String.valueOf(
                                cbPlanDtoMap.getOrDefault(Constants.END_DATE, publishCbPlan.get(Constants.END_DATE))));
                    } catch (ParseException e) {
                        e.printStackTrace(); // Handle the exception appropriately
                    }
                    cbPlan.put(Constants.END_DATE, endDate);
                    cbPlan.put(Constants.DRAFT_DATA, null);
                }

                cbPlan.put(Constants.CB_PUBLISHED_BY, userId);
                if (StringUtils.isNoneBlank(comment)) {
                    cbPlan.put(Constants.COMMENT, comment);
                }
                CbPlan cbPlanDto = mapper.convertValue(cbPlan, CbPlan.class);
                cbPlan.remove(Constants.ID);
                cbPlan.remove(Constants.ORG_ID);
                cbPlan.put(Constants.CB_PUBLISHED_AT, new Date());
                cbPlan.put(Constants.UPDATED_AT, new Date());
                Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
                        Constants.TABLE_CB_PLAN, cbPlan, cbPlanInfo);
                if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                    updateCbPlanLookupInfo(cbPlanDto, userOrgId, cbPlanId);
                    response.getResult().put(Constants.STATUS, Constants.UPDATED);
                    response.getResult().put(Constants.MESSAGE, "Published cbPlan for cbPlanId: " + cbPlanId);
                } else {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams()
                            .setErrmsg((String) resp.get(Constants.ERROR_MESSAGE) + Constants.FOR_CBPLANID + cbPlanId);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                }
            } else {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.CBPLAN_NOT_EXIST_FOR_ID + cbPlanId);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Failed to Publish CB Plan for OrgId: " + userOrgId, e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse retireCbPlan(SunbirdApiRequest request, String userOrgId, String authUserToken, List<String> userRoles) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CB_PLAN_RETIRE);
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
                response.getParams().setErrmsg(Constants.CB_PLANID_MISSING);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> cbPlanInfo = new HashMap<>();
            cbPlanInfo.put(Constants.ID, cbPlanId);
            cbPlanInfo.put(Constants.ORG_ID, userOrgId);
            List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanInfo, null);

            if (CollectionUtils.isNotEmpty(cbPlanMap)) {
                Map<String, Object> cbPlan = cbPlanMap.get(0);
                if (!(userId.equals(cbPlan.get(Constants.CREATED_BY)) ||
                        serverProperties.getCbPlanUpdatePublishAuthorizedRoles().stream().anyMatch(roles -> CollectionUtils.isNotEmpty(userRoles) && userRoles.contains(roles)))) {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("Not Authorized to delete cbp Plan");
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }
                if (Constants.CB_RETIRE.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))) {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams().setErrmsg("CbPlan is already archived for ID: " + cbPlanId);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                    return response;
                }

                cbPlan.put(Constants.UPDATED_AT, new Date());
                cbPlan.put(Constants.UPDATED_BY, userId);
                cbPlan.put(Constants.STATUS, Constants.CB_RETIRE);
                if (StringUtils.isNoneBlank(comment)) {
                    cbPlan.put(Constants.COMMENT, comment);
                }
                cbPlan.remove(Constants.ID);
                cbPlan.remove(Constants.ORG_ID);
                cbPlan.put(Constants.CB_PUBLISHED_AT, new Date());
                Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
                        Constants.TABLE_CB_PLAN, cbPlan, cbPlanInfo);
                if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                    updateCbPlanLookupInfoForRetire(userOrgId, cbPlanId);
                    response.getResult().put(Constants.STATUS, Constants.UPDATED);
                    response.getResult().put(Constants.MESSAGE, "Archived cbPlan for cbPlanId: " + cbPlanId);
                } else {
                    response.getParams().setStatus(Constants.FAILED);
                    response.getParams()
                            .setErrmsg((String) resp.get(Constants.ERROR_MESSAGE) + Constants.FOR_CBPLANID + cbPlanId);
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                }
            } else {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.CBPLAN_NOT_EXIST_FOR_ID + cbPlanId);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Failed to Retire CB Plan for OrgId: " + userOrgId, e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public SBApiResponse readCbPlan(String cbPlanId, String userOrgId, String authUserToken) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CB_PLAN_READ_BY_ID);
        try {
            UUID cbPlanUUID = UUID.fromString(cbPlanId);
            if (cbPlanId == null) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.CB_PLANID_MISSING);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> cbPlanInfo = new HashMap<>();
            cbPlanInfo.put(Constants.ID, cbPlanUUID);
            cbPlanInfo.put(Constants.ORG_ID, userOrgId);
            List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanInfo, null);

            if (CollectionUtils.isNotEmpty(cbPlanMap)) {
                Map<String, Object> cbPlan = cbPlanMap.get(0);
                Map<String, Object> enrichData = populateReadData(cbPlan);
                response.getResult().put(Constants.CONTENT, enrichData);
            } else {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg(Constants.CBPLAN_NOT_EXIST_FOR_ID + cbPlanId);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Failed to Read CB Plan for OrgId: " + userOrgId + "for CB PlanId: " + cbPlanId, e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;

    }

    public SBApiResponse getCBPlanListForUser(String userOrgId, String authUserToken) {
        return getCBPlanListForUser(userOrgId, authUserToken, false);
    }

    @Override
    public SBApiResponse getCBPlanListForUser(String userOrgId, String authTokenOrUserId, boolean isPrivate) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.CBP_PLAN_USER_LIST_API);
        try {
            String userId = "";
            if (isPrivate)
                userId = authTokenOrUserId;
            else
                userId = validateAuthTokenAndFetchUserId(authTokenOrUserId);
            logger.info("UserId of the User : {}, User org ID : {}", userId, userOrgId);
            List<String> fields = Arrays.asList(Constants.PROFILE_DETAILS, Constants.ROOT_ORG_ID);
            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put(Constants.ID, userId);
            List<Map<String, Object>> userDetailsResult = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.SUNBIRD_KEY_SPACE_NAME, Constants.USER, propertiesMap, fields);
            if (CollectionUtils.isEmpty(userDetailsResult)) {
                response.getParams().setStatus(Constants.FAILED);
                response.getParams().setErrmsg("User Does not Exist");
                response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }
            Map<String, Object> userDetails = userDetailsResult.get(0);
            String profileDetails = (String) userDetails.get(Constants.PROFILE_DETAILS_KEY);
            String userDesignation = "";
            Map<String, Object> profileDetailsMap = null;
            List<Map<String, Object>> professionalDetails = null;
            Boolean isVerifiedKaramyogi = false;
            if (StringUtils.isNotEmpty(profileDetails)) {
                profileDetailsMap = mapper.readValue(profileDetails, new TypeReference<HashMap<String, Object>>() {
                });
            }
            if (MapUtils.isNotEmpty(profileDetailsMap)) {
                professionalDetails = (List<Map<String, Object>>) profileDetailsMap.get(Constants.PROFESSIONAL_DETAILS);
                isVerifiedKaramyogi = (Boolean) profileDetailsMap.get(Constants.VERIFIED_KARMAYOGI);
            }
            if (CollectionUtils.isNotEmpty(professionalDetails)) {
                userDesignation = (String) professionalDetails.get(0).get(Constants.DESIGNATION);
            }
            List<String> assignmentTypeInfoKeyQueryList = new ArrayList<>(Arrays.asList(userId, Constants.ALL_USER));
            if (StringUtils.isNotEmpty(userDesignation)) {
                logger.info("User Designation : {}", userDesignation);
                assignmentTypeInfoKeyQueryList.add(userDesignation);
            }
            propertiesMap.clear();
            propertiesMap.put(Constants.ORG_ID, userOrgId);
            propertiesMap.put(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY, assignmentTypeInfoKeyQueryList);
            List<Map<String, Object>> cbplanResult = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.SUNBIRD_KEY_SPACE_NAME, Constants.TABLE_CB_PLAN_LOOKUP, propertiesMap, new ArrayList<>());
            if (CollectionUtils.isEmpty(cbplanResult)) {
                response.getParams().setStatus(Constants.SUCCESSFUL);
                response.getParams().setErrmsg("CB Plan does not exist for the user");
                response.setResponseCode(HttpStatus.OK);
                return response;
            }
            List<Map<String, Object>> resultMap = new ArrayList<>();
            Map<String, Object> courseDetailsMap = new HashMap<>();
            cbplanResult = cbplanResult.stream().filter(userCbPlan -> (Boolean)userCbPlan.get(Constants.CB_IS_ACTIVE) == true)
                    .sorted(Comparator.comparing(m -> (Date) ((Map<String, Object>) m).get(Constants.END_DATE)).reversed()).collect(Collectors.toList());
            for (Map<String, Object> cbPlan : cbplanResult) {
                Map<String, Object> cbPlanDetails = new HashMap<>();
                cbPlanDetails.put(Constants.ID, cbPlan.get(Constants.CB_PLAN_ID_KEY));
                cbPlanDetails.put(Constants.USER_TYPE, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE));
                cbPlanDetails.put(Constants.END_DATE, cbPlan.get(Constants.END_DATE));
                List<String> courses = (List<String>) cbPlan.get(Constants.CB_CONTENT_LIST);
                // Required Fields to be added later if required
                List<Map<String, Object>> courseList = new ArrayList<>();
                for (String courseId : courses) {
                    Map<String, Object> contentDetails = null;
                    if (!courseDetailsMap.containsKey(courseId)) {
                        contentDetails = contentService.readContentFromCache(courseId, null);
                        if (MapUtils.isNotEmpty(contentDetails)) {
                            if (courseId.contains("_rc")) {
                                if (isVerifiedKaramyogi != null && isVerifiedKaramyogi) {
                                    Map<String, Object> secureSettings = (Map<String, Object>) contentDetails.get(Constants.SECURE_SETTINGS);
                                    if (MapUtils.isNotEmpty(secureSettings)) {
                                        List<String> secureOrganisationList = (List<String>) secureSettings.get(Constants.ORGANISATION);
                                        if (CollectionUtils.isNotEmpty(secureOrganisationList) && secureOrganisationList.contains(userOrgId)) {
                                            courseDetailsMap.put(courseId, contentDetails);
                                        }
                                    }
                                }
                                if (!courseDetailsMap.containsKey(courseId)) {
                                    contentDetails.clear();
                                }
                            } else {
                                courseDetailsMap.put(courseId, contentDetails);
                            }
                            //}
                        } else {
                            logger.error("Failed to read course details for Id: {}", courseId);
                        }
                    } else {
                        continue;
                    }
                    if (MapUtils.isNotEmpty(contentDetails)) {
                        courseList.add(contentDetails);
                    }
                }
                cbPlanDetails.put(Constants.CB_CONTENT_LIST, courseList);
                resultMap.add(cbPlanDetails);
            }
            logger.info("Number of CB Plan Available for the user is {}", resultMap.size());
            response.getResult().put(Constants.COUNT, resultMap.size());
            response.getResult().put(Constants.CONTENT, resultMap);
        } catch (Exception e) {
            logger.error("Failed to lookup for user cb plan details. Exception: " + e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public SBApiResponse requestCbplanContent(SunbirdApiRequest request, String token, String userOrgId) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.CBP_PLAN_CONTENT_REQUEST_API);
        Map<String, Object> contentRequest = (Map<String, Object>) request.getRequest();
        Map<String, Object> competency = (Map<String, Object>) contentRequest.get(Constants.COMPETENCY);
        List<String> providersOrgId = (List<String>) contentRequest.get("providerList");
        String description = (String) contentRequest.get(Constants.DESCRIPTION);
        try {
            String userId = validateAuthTokenAndFetchUserId(token);
            if (!validateRequestCbplanPayload(userId, competency, providersOrgId, response))
                return response;
            Map<String, String> mdoInfo = userUtilityService.getUserDetails(Collections.singletonList(userId), new ArrayList<>()).get(userId);

            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put(Constants.ORG_ID, mdoInfo.get(Constants.ROOT_ORG_ID));
            propertiesMap.put(Constants.ID, UUID.randomUUID().toString());
            propertiesMap.put(Constants.COMPETENCY_INFO, mapper.writeValueAsString(competency));
            propertiesMap.put(Constants.STATUS, Constants.STATUS_IN_PROGRESS);
            propertiesMap.put(Constants.PROVIDER_ORG_ID, providersOrgId);
            propertiesMap.put(Constants.DESCRIPTION, description);
            propertiesMap.put(Constants.CREATED_AT, new Date());
            propertiesMap.put(Constants.CREATED_BY, userId);
            SBApiResponse dbResponse = cassandraOperation.insertRecord(Constants.SUNBIRD_KEY_SPACE_NAME, Constants.CB_CONTENT_REQUEST_TABLE, propertiesMap);
            if(!Constants.SUCCESS.equalsIgnoreCase((String)dbResponse.get(Constants.RESPONSE))){
                throw new RuntimeException("An error occurred while creating new content Request");
            }
            propertiesMap.put("mdoName", mdoInfo.get(Constants.CHANNEL));
            propertiesMap.put(Constants.EMAIL, mdoInfo.get(Constants.EMAIL));
            kafkaProducer.push(cbExtServerProperties.getCbplanContentRequestKafkaTopic(), propertiesMap);
        } catch (Exception e) {
            logger.error("Failed to send request for a content for cbplan Exception: " + e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private Map<String, Object> populateReadData(Map<String, Object> cbPlan) throws IOException {
        Map<String, Object> enrichData = new HashMap<>();
        List<String> assignmentTypeInfo = new ArrayList<>();
        List<String> contentTypeInfo = new ArrayList<>();
        List<String> userDraftAssignmentTypeInfoForLive = new ArrayList<>();
        String assignmentType = (String) cbPlan.get(Constants.CB_ASSIGNMENT_TYPE);
        if (StringUtils.isBlank((String) cbPlan.get(Constants.DRAFT_DATA)) ||
                (StringUtils.isNotBlank((String) cbPlan.get(Constants.DRAFT_DATA))
                        && Constants.LIVE.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS)))) {
            enrichData.put(Constants.NAME, cbPlan.get(Constants.NAME));
            enrichData.put(Constants.CB_ASSIGNMENT_TYPE, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE));
            assignmentTypeInfo = (List<String>) cbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO);
            enrichData.put(Constants.CB_CONTENT_TYPE, cbPlan.get(Constants.CB_CONTENT_TYPE));
            contentTypeInfo = (List<String>) cbPlan.get(Constants.CB_CONTENT_LIST);
            enrichData.put(Constants.END_DATE, cbPlan.get(Constants.END_DATE));
            if (!Constants.CB_CUSTOM_TYPE.equalsIgnoreCase(assignmentType)) {
                enrichData.put(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO));
            }
            if (StringUtils.isNotBlank((String) cbPlan.get(Constants.DRAFT_DATA))) {
                Map<String, Object> cbPlanDtoMap = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA),
                        new TypeReference<Map<String, Object>>() {
                        });

                if (cbPlanDtoMap.get(Constants.CB_ASSIGNMENT_TYPE_INFO) != null
                        && Constants.CB_CUSTOM_TYPE.equalsIgnoreCase(assignmentType)) {
                    userDraftAssignmentTypeInfoForLive
                            .addAll((List<String>) cbPlanDtoMap.get(Constants.CB_ASSIGNMENT_TYPE_INFO));
                }
                cbPlanDtoMap.remove(Constants.ID);
                enrichData.put(Constants.DRAFT_DATA, cbPlanDtoMap);
            }
        } else if (StringUtils.isNotBlank((String) cbPlan.get(Constants.DRAFT_DATA))
                && Constants.DRAFT.equalsIgnoreCase((String) cbPlan.get(Constants.STATUS))) {
            CbPlanDto cbPlanDto = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA), CbPlanDto.class);
            enrichData.put(Constants.NAME, cbPlanDto.getName());
            enrichData.put(Constants.CB_ASSIGNMENT_TYPE, cbPlanDto.getAssignmentType());
            assignmentTypeInfo = cbPlanDto.getAssignmentTypeInfo();
            enrichData.put(Constants.CB_CONTENT_TYPE, cbPlanDto.getContentType());
            contentTypeInfo = cbPlanDto.getContentList();
            assignmentType = cbPlanDto.getAssignmentType();
            enrichData.put(Constants.END_DATE, cbPlanDto.getEndDate());
            enrichData.put(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlanDto.getAssignmentTypeInfo());
        }
        Map<String, Map<String, String>> userInfoMap = new HashMap<>();
        enrichData.put(Constants.ID, cbPlan.get(Constants.ID));

        enrichData.put(Constants.CREATED_AT, cbPlan.get(Constants.CREATED_AT));
        enrichData.put(Constants.CB_PUBLISHED_AT, cbPlan.get(Constants.CB_PUBLISHED_AT));
        enrichData.put(Constants.STATUS, cbPlan.get(Constants.STATUS));

        if (Constants.CB_CUSTOM_TYPE.equalsIgnoreCase(assignmentType)) {
            List<Map<String, String>> enrichUserInfoMap = new ArrayList<>();
            List<Map<String, String>> enrichUserInfoMapDraft = new ArrayList<>();
            List<String> allUserInfo = new ArrayList<>();
            allUserInfo.addAll(assignmentTypeInfo);
            allUserInfo.add((String) cbPlan.get(Constants.CREATED_BY));
            if (CollectionUtils.isNotEmpty(userDraftAssignmentTypeInfoForLive)) {
                allUserInfo.addAll(userDraftAssignmentTypeInfoForLive);
            }

            userUtilityService.getUserDetailsFromDB(allUserInfo, Arrays.asList(Constants.FIRSTNAME, Constants.USER_ID, Constants.PROFILE_DETAILS_KEY),
                    userInfoMap);
            enrichUserInfo(userInfoMap);
            for (String userId : assignmentTypeInfo) {
                enrichUserInfoMap.add(userInfoMap.get(userId));
            }
            enrichData.put(Constants.USER_DETAILS, enrichUserInfoMap);

            for (String draftUserId : userDraftAssignmentTypeInfoForLive) {
                enrichUserInfoMapDraft.add(userInfoMap.get(draftUserId));
            }
            if (CollectionUtils.isNotEmpty(enrichUserInfoMapDraft)) {
                Map<String, Object> draft = (Map<String, Object>) enrichData.get(Constants.DRAFT_DATA);
                draft.put(Constants.USER_DETAILS, enrichUserInfoMapDraft);
                draft.remove(Constants.CB_ASSIGNMENT_TYPE_INFO);
                enrichData.put(Constants.DRAFT_DATA, draft);
            }
        } else {
            userUtilityService.getUserDetailsFromDB(Arrays.asList((String) cbPlan.get(Constants.CREATED_BY)),
                    Arrays.asList(Constants.FIRSTNAME, Constants.USER_ID),
                    userInfoMap);
            enrichUserInfo(userInfoMap);
        }

        enrichData.put(Constants.CREATED_BY_NAME,
                userInfoMap.get((String) cbPlan.get(Constants.CREATED_BY)).get(Constants.FIRSTNAME));
        enrichData.put(Constants.CREATED_BY, cbPlan.get(Constants.CREATED_BY));
        List<Map<String, Object>> enrichContentInfoMap = new ArrayList<>();
        for (String contentId : contentTypeInfo) {
            Map<String, Object> contentResponse = contentService.readContentFromCache(contentId, null);
            if (MapUtils.isNotEmpty(contentResponse)) {
                if (Constants.LIVE.equalsIgnoreCase((String) contentResponse.get(Constants.STATUS))) {
                    Map<String, Object> enrichContentMap = new HashMap<>();
                    enrichContentMap.put(Constants.NAME, contentResponse.get(Constants.NAME));
                    enrichContentMap.put(Constants.COMPETENCIES_V5, contentResponse.get(Constants.COMPETENCIES_V5));
                    enrichContentMap.put(Constants.AVG_RATING, contentResponse.get(Constants.AVG_RATING));
                    enrichContentMap.put(Constants.IDENTIFIER, contentResponse.get(Constants.IDENTIFIER));
                    enrichContentMap.put(Constants.DESCRIPTION, contentResponse.get(Constants.DESCRIPTION));
                    enrichContentMap.put(Constants.ADDITIONAL_TAGS, contentResponse.get(Constants.ADDITIONAL_TAGS));
                    enrichContentMap.put(Constants.CONTENT_TYPE_KEY, contentResponse.get(Constants.CONTENT_TYPE_KEY));
                    enrichContentMap.put(Constants.PRIMARY_CATEGORY, contentResponse.get(Constants.PRIMARY_CATEGORY));
                    enrichContentMap.put(Constants.DURATION, contentResponse.get(Constants.DURATION));
                    enrichContentMap.put(Constants.COURSE_APP_ICON, contentResponse.get(Constants.COURSE_APP_ICON));
                    enrichContentMap.put(Constants.POSTER_IMAGE, contentResponse.get(Constants.POSTER_IMAGE));
                    enrichContentMap.put(Constants.ORGANISATION, contentResponse.get(Constants.ORGANISATION));
                    enrichContentMap.put(Constants.CREATOR_LOGO, contentResponse.get(Constants.CREATOR_LOGO));
                    enrichContentInfoMap.add(enrichContentMap);
                }
            }
        }
        enrichData.put(Constants.CONTENT_LIST, enrichContentInfoMap);
        return enrichData;
    }

    private String validateAuthTokenAndFetchUserId(String authUserToken) {
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

    private boolean updateCbPlanLookupInfo(CbPlan planDto, String orgId, UUID cbPlanId) {
        Map<String, Object> cbPlanInfo = new HashMap<>();
        cbPlanInfo.put(Constants.CB_PLAN_ID, cbPlanId);
        cbPlanInfo.put(Constants.ORG_ID, orgId);
        boolean isUpdatedLookup = false;
        List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_CB_PLAN_LOOKUP, cbPlanInfo, null);
        List<String> cbPlanInfoInsertAssignmentKey;
        List<String> cbPlanInfoUpdateAssignmentKey = new ArrayList<>();
        List<String> cbPlanInfoRequestUpdateAssignmentKey = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cbPlanMap)) {
            List<String> assignmentKeyInfoList = cbPlanMap.stream()
                    .map(c -> (String) c.get(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY)).collect(Collectors.toList());
            cbPlanInfoInsertAssignmentKey = planDto.getAssignmentTypeInfo().stream()
                    .filter(assignmentKeyInfo -> !assignmentKeyInfoList.contains(assignmentKeyInfo))
                    .collect(Collectors.toList());
            cbPlanInfoUpdateAssignmentKey = assignmentKeyInfoList.stream()
                    .filter(key -> !planDto.getAssignmentTypeInfo().contains(key)).collect(Collectors.toList());
            cbPlanInfoRequestUpdateAssignmentKey = cbPlanMap.stream().filter(key -> (planDto.getAssignmentTypeInfo().contains((String)key.get(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY))
                                                  && (Boolean)key.get(Constants.CB_IS_ACTIVE) == false))
                                                    .map(key -> (String) key.get(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY))
                                                    .collect(Collectors.toList());
            cbPlanInfoUpdateAssignmentKey.addAll(cbPlanInfoRequestUpdateAssignmentKey);
            isUpdatedLookup = updateLookupInfoForProperties(cbPlanMap, planDto);
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
            SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_CB_PLAN_LOOKUP, lookupInfo);
            if (!resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                isUpdatedLookup = false;
            } else {
                isUpdatedLookup = true;
            }
        }

        for (String assignmentTypeInfo : cbPlanInfoUpdateAssignmentKey) {
            Map<String, Object> compositeKey = new HashMap<>();
            compositeKey.put(Constants.ORG_ID, orgId);
            compositeKey.put(Constants.CB_PLAN_ID, cbPlanId);
            compositeKey.put(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY, assignmentTypeInfo);

            Map<String, Object> lookupInfoUpdated = new HashMap<>();
            lookupInfoUpdated.put(Constants.CB_IS_ACTIVE, CollectionUtils.isNotEmpty(cbPlanInfoRequestUpdateAssignmentKey) && cbPlanInfoRequestUpdateAssignmentKey.contains(assignmentTypeInfo));
            lookupInfoUpdated.put(Constants.END_DATE, planDto.getEndDate());
            Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
                    Constants.TABLE_CB_PLAN_LOOKUP, lookupInfoUpdated, compositeKey);
            if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                isUpdatedLookup = true;
            } else {
                isUpdatedLookup = false;
            }
        }
        return isUpdatedLookup;
    }

    private String updateDraftInfo(Map<String, Object> updatedCbPlan, Map<String, Object> cbPlan) throws IOException {
        Map<String, Object> draftInfo = new HashMap<>();
        if (StringUtils.isBlank((String) cbPlan.get(Constants.DRAFT_DATA))) {
            draftInfo.put(Constants.NAME, updatedCbPlan.getOrDefault(Constants.NAME, cbPlan.get(Constants.NAME)));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE,
                    updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE)));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE_INFO, updatedCbPlan
                    .getOrDefault(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO)));
            draftInfo.put(Constants.CB_CONTENT_TYPE,
                    updatedCbPlan.getOrDefault(Constants.CB_CONTENT_TYPE, cbPlan.get(Constants.CB_CONTENT_TYPE)));
            draftInfo.put(Constants.CB_CONTENT_LIST,
                    updatedCbPlan.getOrDefault(Constants.CB_CONTENT_LIST, cbPlan.get(Constants.CB_CONTENT_LIST)));
            draftInfo.put(Constants.END_DATE,
                    updatedCbPlan.getOrDefault(Constants.END_DATE, cbPlan.get(Constants.END_DATE)));
        } else {
            CbPlanDto cbPlanDto = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA), CbPlanDto.class);
            draftInfo.put(Constants.NAME, updatedCbPlan.getOrDefault(Constants.NAME, cbPlanDto.getName()));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE,
                    updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE, cbPlanDto.getAssignmentType()));
            draftInfo.put(Constants.CB_ASSIGNMENT_TYPE_INFO,
                    updatedCbPlan.getOrDefault(Constants.CB_ASSIGNMENT_TYPE_INFO, cbPlanDto.getAssignmentTypeInfo()));
            draftInfo.put(Constants.CB_CONTENT_TYPE,
                    updatedCbPlan.getOrDefault(Constants.CB_CONTENT_TYPE, cbPlanDto.getContentType()));
            draftInfo.put(Constants.CB_CONTENT_LIST,
                    updatedCbPlan.getOrDefault(Constants.CB_CONTENT_LIST, cbPlanDto.getContentList()));
            draftInfo.put(Constants.END_DATE, updatedCbPlan.getOrDefault(Constants.END_DATE, cbPlanDto.getEndDate()));
        }
        return mapper.writeValueAsString(draftInfo);
    }

    private boolean updateCbPlanLookupInfoForRetire(String orgId, UUID cbPlanId) {
        Map<String, Object> cbPlanInfo = new HashMap<>();
        cbPlanInfo.put(Constants.CB_PLAN_ID, cbPlanId);
        cbPlanInfo.put(Constants.ORG_ID, orgId);
        boolean isUpdatedLookup = false;
        List<Map<String, Object>> cbPlanMap = cassandraOperation.getRecordsByProperties(Constants.KEYSPACE_SUNBIRD,
                Constants.TABLE_CB_PLAN_LOOKUP, cbPlanInfo, null);
        if (CollectionUtils.isNotEmpty(cbPlanMap)) {
            List<String> assignmentKeyInfoList = cbPlanMap.stream()
                    .map(c -> (String) c.get(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY)).collect(Collectors.toList());
            for (String assignmentTypeInfo : assignmentKeyInfoList) {
                Map<String, Object> compositeKey = new HashMap<>();
                compositeKey.put(Constants.ORG_ID, orgId);
                compositeKey.put(Constants.CB_PLAN_ID, cbPlanId);
                compositeKey.put(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY, assignmentTypeInfo);

                Map<String, Object> lookupInfoUpdated = new HashMap<>();
                lookupInfoUpdated.put(Constants.CB_IS_ACTIVE, false);
                Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
                        Constants.TABLE_CB_PLAN_LOOKUP, lookupInfoUpdated, compositeKey);
                if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                    isUpdatedLookup = true;
                } else {
                    isUpdatedLookup = false;
                }
            }
        }
        return isUpdatedLookup;
    }

    @Override
    public SBApiResponse listCbPlan(SunbirdApiRequest request, String userOrgId, String token) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CB_PLAN_LIST);

        try {
            CbPlanSearch searchReq = mapper.convertValue(request.getRequest(), CbPlanSearch.class);
            String errMsg = validateListAPI(userOrgId, request, searchReq);

            if (StringUtils.isNotBlank(errMsg)) {
                response.getParams().setErrmsg(errMsg);
                response.getParams().setStatus(Constants.FAILED);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }

            Map<String, Object> cbPlanPrimaryKey = new HashMap<>();
            cbPlanPrimaryKey.put(Constants.ORG_ID, userOrgId);
            List<String> fields = Arrays.asList(Constants.ORG_ID, Constants.ID, Constants.NAME,
                    Constants.CB_CONTENT_TYPE, Constants.CB_CONTENT_LIST, Constants.CB_ASSIGNMENT_TYPE,
                    Constants.CB_ASSIGNMENT_TYPE_INFO, Constants.END_DATE, Constants.STATUS, Constants.CREATED_BY,
                    Constants.CREATED_AT, Constants.DRAFT_DATA, Constants.UPDATED_AT, Constants.CB_PUBLISHED_AT);

            List<Map<String, Object>> cbPlanList = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    Constants.KEYSPACE_SUNBIRD, Constants.TABLE_CB_PLAN, cbPlanPrimaryKey, fields);
            if (CollectionUtils.isEmpty(cbPlanList)) {
                response.getResult().put(Constants.COUNT, 0);
                response.getResult().put(Constants.CONTENT, Collections.emptyList());
                return response;
            }

            Map<String, Map<String, Object>> courseInfoMap = new HashMap<String, Map<String, Object>>();
            Map<String, Map<String, String>> userInfoMap = new HashMap<String, Map<String, String>>();
            List<Map<String, Object>> filteredCbPlanList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> cbPlan : cbPlanList) {
                String status = (String) cbPlan.get(Constants.STATUS);
                if (StringUtils.isBlank(status)) {
                    logger.error(String.format("Status value is null for CB Plan. OrgId : %s, Id: %s", userOrgId,
                            cbPlan.get(Constants.ID)));
                    continue;
                }

                if (Constants.CB_RETIRE.equalsIgnoreCase(status)
                        || !status.equalsIgnoreCase((String) searchReq.getFilters().get(Constants.STATUS))) {
                    continue;
                }

                if (status.equalsIgnoreCase(Constants.DRAFT)) {
                    // If the status is draft, then details needs to be retrieved from draft
                    CbPlanDto draftDto = mapper.readValue((String) cbPlan.get(Constants.DRAFT_DATA), CbPlanDto.class);
                    cbPlan.put(Constants.NAME, draftDto.getName());
                    cbPlan.put(Constants.CB_ASSIGNMENT_TYPE, draftDto.getAssignmentType());
                    cbPlan.put(Constants.CB_CONTENT_TYPE, draftDto.getContentType());
                    cbPlan.put(Constants.END_DATE, draftDto.getEndDate());
                    cbPlan.put(Constants.CB_ASSIGNMENT_TYPE_INFO, draftDto.getAssignmentTypeInfo());
                    cbPlan.put(Constants.CB_CONTENT_LIST, draftDto.getContentList());
                    cbPlan.remove(Constants.DRAFT_DATA);
                }

                // these values could be null if plan is draft. set to empty string if so;
                String assignmentType = (String) cbPlan.get(Constants.CB_ASSIGNMENT_TYPE);
                assignmentType = (assignmentType == null) ? "" : assignmentType;
                String contentType = (String) cbPlan.get(Constants.CB_CONTENT_TYPE);
                contentType = (contentType == null) ? "" : contentType;

                if (searchReq.getFilters().containsKey(Constants.CB_ASSIGNMENT_TYPE)) {
                    if (!assignmentType
                            .equalsIgnoreCase((String) searchReq.getFilters().get(Constants.CB_ASSIGNMENT_TYPE))) {
                        continue;
                    }
                }

                if (searchReq.getFilters().containsKey(Constants.CB_CONTENT_TYPE)) {
                    if (!contentType.equalsIgnoreCase((String) searchReq.getFilters().get(Constants.CB_CONTENT_TYPE))) {
                        continue;
                    }
                }

                // enrich course information
                List<String> contentIdList = (List<String>) cbPlan.get(Constants.CB_CONTENT_LIST);
                List<Map<String, Object>> courseMapList = new ArrayList<Map<String, Object>>();
                for (String contentId : contentIdList) {
                    if (!courseInfoMap.containsKey(contentId)) {
                        Map<String, Object> courseInfo = contentService.readContentFromCache(contentId,
                                Collections.emptyList());
                        if (MapUtils.isNotEmpty(courseInfo)) {
                            if (Constants.LIVE.equalsIgnoreCase((String) courseInfo.get(Constants.STATUS))) {
                                courseInfoMap.put(contentId, courseInfo);
                            }
                        }
                    }
                    if (courseInfoMap.containsKey(contentId)) {
                        courseMapList.add(courseInfoMap.get(contentId));
                    }
                }
                cbPlan.put(Constants.CB_CONTENT_LIST, courseMapList);

                // enrich user Information
                if (Constants.CB_CUSTOM_TYPE.equalsIgnoreCase(assignmentType)) {
                    List<String> userIdList = (List<String>) cbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO);
                    userUtilityService.getUserDetailsFromDB(userIdList,
                            Arrays.asList(Constants.FIRSTNAME, Constants.USER_ID, Constants.PROFILE_DETAILS_KEY),
                            userInfoMap);
                    enrichUserInfo(userInfoMap);
                    List<Map<String, String>> enrichUserInfoList = new ArrayList<>();
                    for (String userId : userIdList) {
                        enrichUserInfoList.add(userInfoMap.get(userId));
                    }
                    cbPlan.put(Constants.USER_DETAILS, enrichUserInfoList);

                } else if (Constants.CB_DESIGNATION_TYPE.equalsIgnoreCase(assignmentType)) {
                    cbPlan.put(Constants.USER_DETAILS, (List<String>) cbPlan.get(Constants.CB_ASSIGNMENT_TYPE_INFO));
                }

                userUtilityService.getUserDetailsFromDB(Arrays.asList((String) cbPlan.get(Constants.CREATED_BY)),
                        Arrays.asList(Constants.FIRSTNAME, Constants.USER_ID, Constants.PROFILE_DETAILS_KEY),
                        userInfoMap);
                enrichUserInfo(userInfoMap);
                cbPlan.put(Constants.CREATED_BY_NAME,
                        userInfoMap.get((String) cbPlan.get(Constants.CREATED_BY)).get(Constants.FIRSTNAME));
                cbPlan.remove(Constants.CB_ASSIGNMENT_TYPE_INFO);
                cbPlan.put(Constants.USER_TYPE, assignmentType);
                cbPlan.remove(Constants.CB_ASSIGNMENT_TYPE);
                filteredCbPlanList.add(cbPlan);
            }
            filteredCbPlanList = filteredCbPlanList.stream().sorted(Comparator.comparing(entry -> (Date) entry.get(Constants.UPDATED_AT), Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            response.getResult().put(Constants.COUNT, filteredCbPlanList.size());
            response.getResult().put(Constants.CONTENT, filteredCbPlanList);
        } catch (Exception e) {
            logger.error("Failed to list CB Plan for OrgId: " + userOrgId, e);
            response.getParams().setErrmsg(e.getMessage());
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private String validateListAPI(String orgId, SunbirdApiRequest request, CbPlanSearch searchReq) {
        String errMsg = "";
        // List request Validation.
        if (StringUtils.isBlank(orgId)) {
            errMsg = "OrgId is missing";
            return errMsg;
        }
        if (request.getRequest() == null) {
            errMsg = "Request object is missing";
            return errMsg;
        }

        if (searchReq.getFilters() == null) {
            errMsg = "Request object is missing filters";
            return errMsg;
        }

        String status = (String) searchReq.getFilters().get(Constants.STATUS);
        if (StringUtils.isBlank(status)) {
            errMsg = "Filter object must contain status attribute";
            return errMsg;
        }

        String contentType;
        String assignmentType;
        contentType = (String) searchReq.getFilters().get(Constants.CONTENT_TYPE);
        assignmentType = (String) searchReq.getFilters().get(Constants.CB_ASSIGNMENT_TYPE);

        // Will remove the filters if one of the value is blank
        if (StringUtils.isBlank(contentType)) {
            searchReq.getFilters().remove(Constants.CONTENT_TYPE);
        }

        if (StringUtils.isBlank(assignmentType)) {
            searchReq.getFilters().remove(assignmentType);
        }

        return errMsg;
    }

    private String getDesignationForUser(String profileDetails, String userId) {
        String userDesignation = "";
        try {
            Map<String, Object> profileDetailsMap = null;
            List<Map<String, Object>> professionalDetails = null;
            if (StringUtils.isNotEmpty(profileDetails)) {
                profileDetailsMap = mapper.readValue(profileDetails, new TypeReference<HashMap<String, Object>>() {
                });
            }
            if (MapUtils.isNotEmpty(profileDetailsMap)) {
                professionalDetails = (List<Map<String, Object>>) profileDetailsMap.get(Constants.PROFESSIONAL_DETAILS);
            }
            if (CollectionUtils.isNotEmpty(professionalDetails)) {
                userDesignation = (String) professionalDetails.get(0).get(Constants.DESIGNATION);
            }
        } catch (Exception e) {
            logger.error("Not able to read the profile Details for userId: " + userId, e);
        }
        return userDesignation;
    }

    private void enrichUserInfo(Map<String, Map<String, String>> userInfoMap) {
        for (Map.Entry userEntry : userInfoMap.entrySet()) {
            Map<String, String> userInfo = (Map<String, String>) userEntry.getValue();
            String profileDetails = userInfo.get(Constants.PROFILE_DETAILS_KEY);
            String userDesignation = userInfo.get(Constants.DESIGNATION) != null ? userInfo.get(Constants.DESIGNATION) :
                    getDesignationForUser(profileDetails, (String) userEntry.getKey());
            userInfo.put(Constants.DESIGNATION, userDesignation);
            userInfo.remove(Constants.PROFILE_DETAILS_KEY);
        }
    }

    private boolean validateRequestCbplanPayload(String userId, Map<String, Object> competency, List<String> providersOrgId, SBApiResponse response) {
        StringBuilder exceptionMessage = new StringBuilder();
        if (StringUtils.isEmpty(userId))
            exceptionMessage.append(Constants.USER_ID_DOESNT_EXIST);
        if (competency == null || competency.isEmpty())
            exceptionMessage.append(" " + Constants.COMPETENCY_DETAILS_MISSING);
        if (CollectionUtils.isEmpty(providersOrgId))
            exceptionMessage.append(" " + Constants.ORG_ID_MISSING);
        if (StringUtils.isNotEmpty(exceptionMessage)) {
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            response.getParams().setErrmsg(exceptionMessage.toString());
            return false;
        }
        return true;
    }

    private boolean updateLookupInfoForProperties(List<Map<String, Object>> cbPlanMap, CbPlan planDto) {
        boolean isUpdatedLookup = true;
        for (Map<String, Object> cbLookupInfo : cbPlanMap) {
            Date endDate = (Date) cbLookupInfo.get(Constants.END_DATE);
            if (!planDto.getEndDate().equals(endDate)) {
                Map<String, Object> compositeKey = new HashMap<>();
                compositeKey.put(Constants.ORG_ID, planDto.getOrgId());
                compositeKey.put(Constants.CB_PLAN_ID, planDto.getId());
                compositeKey.put(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY, cbLookupInfo.get(Constants.CB_ASSIGNMENT_TYPE_INFO_KEY));

                Map<String, Object> lookupInfoUpdated = new HashMap<>();
                lookupInfoUpdated.put(Constants.END_DATE, planDto.getEndDate());
                Map<String, Object> resp = cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD,
                        Constants.TABLE_CB_PLAN_LOOKUP, lookupInfoUpdated, compositeKey);
                if (resp.get(Constants.RESPONSE).equals(Constants.SUCCESS)) {
                    isUpdatedLookup = true;
                } else {
                    isUpdatedLookup = false;
                }
            }
        }
        return isUpdatedLookup;
    }
}
