package org.sunbird.operationalreports.service;

/**
 * @author Deepak kumar Thakur
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRequest;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.AccessTokenValidator;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.config.PropertiesConfig;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OprationalReportServiceImpl implements OperationalReportService {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CbExtServerProperties serverConfig;

    @Autowired
    PropertiesConfig configuration;

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundReqService;

    @Autowired
    OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    AccessTokenValidator accessTokenValidator;

    @Autowired
    private CassandraOperation cassandraOperation;

    public SBApiResponse grantReportAccessToMDOAdmin(SunbirdApiRequest request, String userOrgId, String authToken) throws Exception {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.GRANT_REPORT_ACCESS_API);
        try {
            Map<String, Object> mdoAdminDetails = (Map<String, Object>) request.getRequest();
            String mdoAdminUserId = (String) ((Map<String, Object>) mdoAdminDetails.get(Constants.MDO_ADMIN)).get(Constants.USER_ID);
            String reportExpiryDate = (String) ((Map<String, Object>) mdoAdminDetails.get(Constants.MDO_ADMIN)).get("reportExpiryDate");
            Map<String, String> headersValue = new HashMap<>();
            headersValue.put(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            //String mdoLeaderUserId = accessTokenValidator.fetchUserIdFromAccessToken(authToken);
            String mdoLeaderUserId = "291fd548-ab04-4f16-938a-67963ed0b6b6";
            if (null == mdoLeaderUserId)
                throw new RuntimeException("UserId Couldn't fetch from auth token");
            logger.info("MDO Leader User ID : {}", mdoLeaderUserId);
            StringBuilder url = new StringBuilder(configuration.getLmsServiceHost()).append(configuration.getLmsUserSearchEndPoint());
            Map<String, Object> userSearchRequest = getSearchObject(Arrays.asList(mdoLeaderUserId, mdoAdminUserId));
            Map<String, Object> searchProfileApiResp = outboundReqService.fetchResultUsingPost(url.toString(), userSearchRequest, headersValue);
            if (searchProfileApiResp != null) {
                String mdoUserOrgId = (String) searchProfileApiResp.get(Constants.ROOT_ORG_ID);
                logger.info("User Org ID : {}", mdoUserOrgId);
                Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
                Map<String, Object> userSearchResponse = (Map<String, Object>) map.get(Constants.RESPONSE);
                List<Map<String, Object>> contents = (List<Map<String, Object>>) userSearchResponse.get(Constants.CONTENT);
                Set<String> rootOrgIds = new HashSet<>();
                String rootOrgId = null;
                if (!CollectionUtils.isEmpty(contents)) {
                    for (Map<String, Object> content : contents) {
                        rootOrgId = (String) content.get(Constants.ROOT_ORG_ID);
                        rootOrgIds.add(rootOrgId);
                    }
                }
                if (rootOrgIds.size() != 1) {
                    logger.error("Failed to grant access due to different org.");
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Failed to grant access due to different org.");
                }
                StringBuilder uri = new StringBuilder(serverConfig.getSbUrl());
                Map<String, Object> roleRead = (Map<String, Object>) outboundRequestHandlerService.fetchResult(String.valueOf(uri.append(serverConfig.getSbRoleRead()).append(mdoAdminUserId)));
                List<Map<String, Object>> roles = (List<Map<String, Object>>) ((Map<String, Object>) roleRead.get("result")).get("roles");
                List<String> assignedRoles = new ArrayList<>();
                for (Map<String, Object> roleMap : roles) {
                    assignedRoles.add((String) roleMap.get("role"));
                }
                if (!assignedRoles.contains("MDO_ADMIN")) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Grantee doesn't have MDO_ADMIN role");
                }
                assignedRoles.add(Constants.MDO_REPORT_ACCESSOR);
                Map<String, Object> assignRoleReq = new HashMap<>();
                Map<String, Object> roleRequestBody = new HashMap<>();
                roleRequestBody.put(Constants.ORGANIZATION_ID, rootOrgId);
                roleRequestBody.put("userId", mdoAdminUserId);
                roleRequestBody.put("roles", assignedRoles);
                assignRoleReq.put("request", roleRequestBody);
                outboundRequestHandlerService.fetchResultUsingPost(serverConfig.getSbUrl() + serverConfig.getSbAssignRolePath(), assignRoleReq,
                        headersValue);
                Map<String, Object> primaryKeyMap = new HashMap<>();
                primaryKeyMap.put(Constants.ID, mdoAdminUserId);
                long reportExpiryDateMillis = getParsedDate(reportExpiryDate);
                Map<String, Object> keyMap = new HashMap<>();
                keyMap.put("report_access_expiry", reportExpiryDateMillis);
                cassandraOperation.updateRecord(
                        Constants.KEYSPACE_SUNBIRD, Constants.USER, keyMap, primaryKeyMap);
                response.getResult().put(Constants.STATUS, Constants.SUCCESS);
                response.getResult().put(Constants.MESSAGE, "Report access has been granted successfully");
            }
        } catch (HttpClientErrorException e) {
            logger.error("An exception occurred {}", e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("An exception occurred {}", e.getMessage(), e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private Map<String, Object> getSearchObject(List<String> userIds) {
        Map<String, Object> requestObject = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put(Constants.USER_ID, userIds);
        request.put(Constants.LIMIT, userIds.size());
        request.put(Constants.OFFSET, 0);
        request.put(Constants.FILTERS, filters);
        request.put(Constants.FIELDS_CONSTANT, Arrays.asList(Constants.USER_ID, Constants.STATUS, Constants.CHANNEL, Constants.ROOT_ORG_ID));
        requestObject.put(Constants.REQUEST, request);
        return requestObject;
    }

    private long getParsedDate(String reportExpiryDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = dateFormat.parse(reportExpiryDate);
        long reportExpiryDateMillis = parsedDate.getTime();
        return reportExpiryDateMillis;
    }
}



