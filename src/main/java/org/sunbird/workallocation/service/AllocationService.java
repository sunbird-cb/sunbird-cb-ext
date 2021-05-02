package org.sunbird.workallocation.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.Response;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.*;
import org.sunbird.workallocation.util.FRACReqBuilder;
import org.sunbird.workallocation.util.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AllocationService {

    static final String ALLOCATION_DETAILS = "allocationDetails";
    static final String USER_DETAILS = "userDetails";
    public static final String DRAFT_STATUS = "Draft";
    public static final String PUBLISHED_STATUS = "Published";
    public static final String ARCHIVED_STATUS = "Archived";
    public static final List<String> STATUS_LIST = Collections.unmodifiableList(Arrays.asList(DRAFT_STATUS, PUBLISHED_STATUS, ARCHIVED_STATUS));
    public static final String ADD = "add";
    public static final String UPDATE = "update";
    private Logger logger = LoggerFactory.getLogger(AllocationService.class);

    final String[] includeFields = {"personalDetails.firstname", "personalDetails.surname",
            "personalDetails.primaryEmail", "id", "professionalDetails.name"};

    @Autowired
    private CbExtServerProperties extServerProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Validator validator;

    @Autowired
    private CbExtServerProperties configuration;

    @Autowired
    private IndexerService indexerService;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private FRACReqBuilder fracReqBuilder;

    @Value("${workallocation.index.name}")
    public String index;

    @Value("${workallocation.index.type}")
    public String indexType;

    ObjectMapper mapper = new ObjectMapper();

    /**
     * @param userId            user id
     * @param workAllocationDTO work allocation object
     * @return response
     */
    public Response addWorkAllocation(String userAuthToken, String userId, WorkAllocationDTO workAllocationDTO) {
        validator.validateWorkAllocationReq(workAllocationDTO, ADD);
        if (DRAFT_STATUS.equals(workAllocationDTO.getStatus())) {
            if (StringUtils.isEmpty(workAllocationDTO.getUserId())) {
                workAllocationDTO.setUserId(UUID.randomUUID().toString());
            }
            if (StringUtils.isEmpty(workAllocationDTO.getWaId())) {
                workAllocationDTO.setWaId(UUID.randomUUID().toString());
            }
        }
        if (PUBLISHED_STATUS.equals(workAllocationDTO.getStatus())) {
            validator.validateWorkAllocationReq(workAllocationDTO, UPDATE);
        }
        if (!CollectionUtils.isEmpty(workAllocationDTO.getRoleDetails())) {
            verifyRoleActivity(userAuthToken, workAllocationDTO);
        }
        //To - Do
        //Add competency to FRAC
        if (StringUtils.isEmpty(workAllocationDTO.getPositionId()) && !StringUtils.isEmpty(workAllocationDTO.getUserPosition())) {
            workAllocationDTO.setPositionId(createUserPosition(userAuthToken, workAllocationDTO.getUserPosition()));
        }
        Map<String, Object> existingRecord = indexerService.readEntity(index, indexType, workAllocationDTO.getUserId());
        WorkAllocation workAllocation;
        if (!ObjectUtils.isEmpty(existingRecord)) {
            workAllocation = mapper.convertValue(existingRecord, WorkAllocation.class);
            workAllocation = waObjectTransition(userId, workAllocationDTO, workAllocation);

        } else {
            workAllocation = new WorkAllocation();
            workAllocation.setId(workAllocationDTO.getUserId());
            workAllocation.setUserId(workAllocationDTO.getUserId());
            workAllocation.setUserName(workAllocationDTO.getUserName());
            workAllocation.setUserEmail(workAllocationDTO.getUserEmail());
            WAObject draft = getWAObject(userId, workAllocationDTO);
            draft.setCreatedAt(System.currentTimeMillis());
            draft.setCreatedBy(userId);
            workAllocation.setDraftWAObject(draft);
            workAllocation.setActiveWAObject(null);
            workAllocation.setArchivedWAList(null);
        }
        RestStatus restStatus = indexerService.addEntity(index, indexType, workAllocationDTO.getUserId(),
                mapper.convertValue(workAllocation, Map.class));
        Response response = new Response();
        if (!ObjectUtils.isEmpty(restStatus)) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        } else {
            response.put(Constants.MESSAGE, Constants.FAILED);
        }
        response.put(Constants.DATA, restStatus);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    /**
     *
     * @param authUserToken
     * @param userId
     * @param workAllocationDTO
     * @return
     */
    public Response updateWorkAllocation(String authUserToken, String userId, WorkAllocationDTO workAllocationDTO) {
          validator.validateWorkAllocationReq(workAllocationDTO, UPDATE);
        if (!CollectionUtils.isEmpty(workAllocationDTO.getRoleDetails())) {
            verifyRoleActivity(authUserToken, workAllocationDTO);
        }
        if (StringUtils.isEmpty(workAllocationDTO.getPositionId()) && !StringUtils.isEmpty(workAllocationDTO.getUserPosition())) {
            workAllocationDTO.setPositionId(createUserPosition(authUserToken, workAllocationDTO.getUserPosition()));
        }
        Map<String, Object> existingRecord = indexerService.readEntity(index, indexType, workAllocationDTO.getUserId());
        WorkAllocation workAllocation;
        if (CollectionUtils.isEmpty(existingRecord)) {
            throw new BadRequestException("No record found on given Id!");
        }
        workAllocation = mapper.convertValue(existingRecord, WorkAllocation.class);
        workAllocation = waObjectTransition(userId, workAllocationDTO, workAllocation);
        RestStatus restStatus = indexerService.updateEntity(index, indexType, workAllocation.getUserId(),
                mapper.convertValue(workAllocation, Map.class));
        Response response = new Response();
        if (!ObjectUtils.isEmpty(restStatus)) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        } else {
            response.put(Constants.MESSAGE, Constants.FAILED);
        }
        response.put(Constants.DATA, restStatus);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    /**
     * Search User's for work allocation
     *
     * @param criteria search criteria
     * @return return the user's list based on search criterias
     */
    public Response getUsers(SearchCriteria criteria) {
        validator.validateCriteria(criteria);
        final BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (DRAFT_STATUS.equals(criteria.getStatus())) {
            query.must(QueryBuilders.matchQuery("draftWAObject.deptName", criteria.getDepartmentName())).
                    must(QueryBuilders.matchQuery("draftWAObject.status", criteria.getStatus()));
        }
        if (PUBLISHED_STATUS.equals(criteria.getStatus())) {
            query.must(QueryBuilders.matchQuery("activeWAObject.deptName", criteria.getDepartmentName()))
                    .must(QueryBuilders.matchQuery("activeWAObject.status", criteria.getStatus()));
        }
        if (ARCHIVED_STATUS.equals(criteria.getStatus())){
            query.must(QueryBuilders.nestedQuery("archivedWAList", QueryBuilders.matchQuery("archivedWAList.deptName", criteria.getDepartmentName()), ScoreMode.None));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.from(criteria.getPageNo());
        sourceBuilder.size(criteria.getPageSize());
        if (DRAFT_STATUS.equals(criteria.getStatus()))
        sourceBuilder.sort(SortBuilders.fieldSort("draftWAObject.updatedAt").order(SortOrder.DESC));
        if (PUBLISHED_STATUS.equals(criteria.getStatus()))
            sourceBuilder.sort(SortBuilders.fieldSort("activeWAObject.updatedAt").order(SortOrder.DESC));
        logger.info("Query ======> ");
        logger.info(query.toString());
        List<WorkAllocation> allocationSearchList = new ArrayList<>();
        List<Map<String, Object>> finalRes = new ArrayList<>();
        Map<String, Object> result;
        long totalCount = 0;
        try {
            SearchResponse searchResponse = indexerService.getEsResult(index, indexType, sourceBuilder);
            totalCount = searchResponse.getHits().getTotalHits();
            for (SearchHit hit : searchResponse.getHits()) {
                allocationSearchList.add(mapper.convertValue(hit.getSourceAsMap(), WorkAllocation.class));
            }
            Set<String> userIds = allocationSearchList.stream().map(WorkAllocation::getUserId)
                    .collect(Collectors.toSet());
            Map<String, Object> usersMap = getUserDetails(userIds);
            for (WorkAllocation workAllocation : allocationSearchList) {
                result = new HashMap<>();
                result.put(ALLOCATION_DETAILS, workAllocation);
                result.put(USER_DETAILS, usersMap.get(workAllocation.getUserId()));
                finalRes.add(result);
            }
        } catch (IOException e) {
            logger.error("Elastic Search Exception", e);
        }
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, finalRes);
        response.put("totalhit", totalCount);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    /**
     * Get the user details from open-saber es index based on given user id's
     *
     * @param userIds
     * @return map of userId's and user details
     * @throws IOException
     */
    public Map<String, Object> getUserDetails(Set<String> userIds) throws IOException {
        Map<String, Object> result = new HashMap<>();
        final BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("id.keyword", userIds));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.fetchSource(includeFields, new String[]{});
        SearchResponse searchResponse = indexerService.getEsResult(configuration.getEsProfileIndex(),
                configuration.getEsProfileIndexType(), sourceBuilder);
        for (SearchHit hit : searchResponse.getHits()) {
            Map<String, Object> userResult = extractUserDetails(hit.getSourceAsMap());
            if (!StringUtils.isEmpty(userResult.get("wid")))
                result.put((String) userResult.get("wid"), userResult);
        }
        return result;
    }

    /**
     * Extract the user details from elastic search map
     *
     * @param searObjectMap
     * @return return the user's map
     */
    public Map<String, Object> extractUserDetails(Map<String, Object> searObjectMap) {
        Map<String, Object> personalDetails = (Map<String, Object>) searObjectMap.get("personalDetails");
        List<Map<String, Object>> professionalDetails = (List<Map<String, Object>>) searObjectMap
                .get("professionalDetails");
        String depName = null;
        if (!CollectionUtils.isEmpty(professionalDetails)) {
            Optional<Map<String, Object>> propDetails = professionalDetails.stream().findFirst();
            if (propDetails.isPresent()) {
                depName = CollectionUtils.isEmpty(propDetails.get()) ? "" : (String) propDetails.get().get("name");
            }
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("first_name", personalDetails.get("firstname"));
        result.put("last_name", personalDetails.get("surname"));
        result.put("email", personalDetails.get("primaryEmail"));
        result.put("wid", searObjectMap.get("id"));
        result.put("department_name", depName);
        return result;
    }

    /**
     * Search the user's based on searchTerm
     *
     * @param searchTerm
     * @return list of user's
     * @throws IOException
     */
    public List<Map<String, Object>> getUserSearchData(String searchTerm) throws IOException {
        if (StringUtils.isEmpty(searchTerm))
            throw new BadRequestException("Search term should not be empty!");
        List<Map<String, Object>> resultArray = new ArrayList<>();
        Map<String, Object> result;
        final BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.primaryEmail", searchTerm))
                .should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.firstname", searchTerm))
                .should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.surname", searchTerm));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.fetchSource(includeFields, new String[]{});
        SearchResponse searchResponse = indexerService.getEsResult(configuration.getEsProfileIndex(),
                configuration.getEsProfileIndexType(), sourceBuilder);
        for (SearchHit hit : searchResponse.getHits()) {
            result = extractUserDetails(hit.getSourceAsMap());
            resultArray.add(result);
        }
        return resultArray;
    }

    /**
     * Search the user and enhance the user with work allocation details.
     *
     * @param searchTerm
     * @return user auto complete result
     */
    public Response userAutoComplete(String searchTerm) {
        List<Map<String, Object>> userData = null;
        Map<String, Object> allocationSearchMap = new HashMap<>();
        try {
            userData = getUserSearchData(searchTerm);
        } catch (IOException e) {
            logger.error("Exception occurred while searching the user's from user registry", e);
        }
        if (!CollectionUtils.isEmpty(userData)) {
            Set<String> userIds = userData.stream().map(data -> (String) data.get("wid")).collect(Collectors.toSet());
            final QueryBuilder query = QueryBuilders.termsQuery("userId.keyword", userIds);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
            try {
                SearchResponse searchResponse = indexerService.getEsResult(index, indexType, sourceBuilder);
                for (SearchHit hit : searchResponse.getHits()) {
                    allocationSearchMap.put((String) hit.getSourceAsMap().get("userId"), hit.getSourceAsMap());
                }
            } catch (IOException e) {
                logger.error("Exception occurred while searching the user's from work allocation", e);
            }
        }
        List<Map<String, Object>> finalRes = new ArrayList<>();
        Map<String, Object> result;
        if (!CollectionUtils.isEmpty(userData)) {
            for (Map<String, Object> user : userData) {
                result = new HashMap<>();
                result.put(USER_DETAILS, user);
                result.put(ALLOCATION_DETAILS, allocationSearchMap.getOrDefault(user.get("wid"), null));
                finalRes.add(result);
            }
        }
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, finalRes);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    private void verifyRoleActivity(String authUserToken, WorkAllocationDTO workAllocation) {
        List<Role> oldRoleList = workAllocation.getRoleDetails();
        List<Role> newRoleList = new ArrayList<>();
        try {
            for (Role r : oldRoleList) {
                if (StringUtils.isEmpty(r.getId())) {
                    Role newRole = fetchAddedRole(authUserToken, r, null);
                    newRoleList.add(newRole);
                } else {
                    // Role is from FRAC - No need to create new.
                    // However, we need to check Activity is from FRAC or not.
                    if (!CollectionUtils.isEmpty(r.getChildNodes())) {
                        List<ChildNode> newChildNodes = new ArrayList<>();
                        boolean isNewChildAdded = r.getChildNodes().stream().anyMatch(childNode -> StringUtils.isEmpty(childNode.getId()));
                        if (isNewChildAdded) {
                            Role newRole = fetchAddedRole(authUserToken, r, null);
                            newChildNodes.addAll(newRole.getChildNodes());
                        } else {
                            newChildNodes.addAll(r.getChildNodes());
                        }
                        r.setChildNodes(newChildNodes);
                    }
                    newRoleList.add(r);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Add Role / Activity. Excption: ", e);
        }
        if (oldRoleList.size() == newRoleList.size()) {
            workAllocation.setRoleDetails(newRoleList);
        } else {
            logger.error("Failed to create FRAC Roles / Activities. Old List Size: " + oldRoleList.size()
                    + ", New List Size: " + newRoleList.size());
        }
    }

    private String createUserPosition(String authUserToken, String positionName) {
        logger.info("Adding Position into FRAC System...");
        String positionId = null;
        FracRequest request = fracReqBuilder.getPositionRequest(getSourceValue(), positionName);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authUserToken);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(mapper.writeValueAsString(request), headers);
            FracResponse response = restTemplate.postForObject(
                    extServerProperties.getFracHost() + extServerProperties.getFracActivityPath(), entity, FracResponse.class);
            if (!ObjectUtils.isEmpty(response) && !ObjectUtils.isEmpty(response.getResponseData())) {
                positionId = response.getResponseData().getId();
            }
            logger.info("Added Position successful ...");
        } catch (JsonProcessingException e) {
            logger.error("Parsing Exception While Creating the Position in Frac", e);
        }
        return positionId;
    }

    private Role fetchAddedRole(String authUserToken, Role role, ChildNode cn) throws Exception {
        logger.info("Adding Role into FRAC Service...");
        ObjectMapper mapper = new ObjectMapper();

        FracRequest request = role.getFracRequest(getSourceValue(), cn);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authUserToken);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(request), headers);
        FracResponse response = restTemplate.postForObject(
                extServerProperties.getFracHost() + extServerProperties.getFracNodePath(), entity, FracResponse.class);
        if (response != null && response.getStatusInfo().getStatusCode() == 200) {
            return processRole(response);
        }
        return role;
    }

    private String getSourceValue() {
        // TODO -- Check useDeptName config value. If TRUE then get the current
        // department.
        return extServerProperties.getFracSource();
    }

    private Role processRole(FracResponse response) {
        Role r = new Role();
        r.setId(response.getResponseData().getId());
        r.setName(response.getResponseData().getName());
        r.setType(response.getResponseData().getType());
        r.setStatus(response.getResponseData().getStatus());
        r.setSource(response.getResponseData().getSource());
        List<ChildNode> children = response.getResponseData().getChildren();
        if (children != null && !CollectionUtils.isEmpty(children)) {
            List<ChildNode> childNodes = new ArrayList<ChildNode>();
            for (ChildNode child : children) {
                childNodes.add(processActivity(child));
            }
            r.setChildNodes(childNodes);
        }
        return r;
    }

    private ChildNode processActivity(ChildNode child) {
        ChildNode cn = new ChildNode();
        cn.setId(child.getId());
        cn.setType(child.getType());
        cn.setName(child.getName());
        cn.setStatus(child.getStatus());
        cn.setSource(child.getSource());
        return cn;
    }

    private WAObject getWAObject(String userId, WorkAllocationDTO dto) {
        long currentMillis = System.currentTimeMillis();
        WAObject wa = new WAObject();
        wa.setDeptId(dto.getDeptId());
        wa.setDeptName(dto.getDeptName());
        wa.setId(dto.getWaId());
        wa.setPositionId(dto.getPositionId());
        wa.setCompetencyDetails(dto.getCompetencyDetails());
        wa.setRoleDetails(dto.getRoleDetails());
        wa.setUserPosition(dto.getUserPosition());
        wa.setUpdatedAt(currentMillis);
        wa.setUpdatedBy(userId);
        wa.setStatus(dto.getStatus());
        return wa;
    }

    private WorkAllocation waObjectTransition(String userId, WorkAllocationDTO dto, WorkAllocation workAllocation) {
        WAObject wa = getWAObject(userId, dto);
        WorkAllocation deepCopy = null;
        try {
            deepCopy = mapper.readValue(mapper.writeValueAsString(workAllocation), WorkAllocation.class);
        } catch (IOException e) {
            logger.error("Exception occurred while deserializing the data!");
        }
        long currentMillis = System.currentTimeMillis();
        if (DRAFT_STATUS.equals(dto.getStatus())) {
            if (ObjectUtils.isEmpty(deepCopy.getDraftWAObject())) {
                wa.setCreatedBy(userId);
                wa.setCreatedAt(currentMillis);
                workAllocation.setDraftWAObject(wa);
            }
        }
        if (PUBLISHED_STATUS.equals(dto.getStatus())) {
            if (!ObjectUtils.isEmpty(deepCopy.getActiveWAObject())) {
                WAObject oldPublishObject = deepCopy.getActiveWAObject();
                oldPublishObject.setUpdatedAt(currentMillis);
                oldPublishObject.setUpdatedBy(userId);
                workAllocation.addArchivedWAList(oldPublishObject);
            } else {
                wa.setCreatedBy(userId);
                wa.setCreatedAt(currentMillis);
            }
            workAllocation.setActiveWAObject(wa);
            workAllocation.setDraftWAObject(null);
        }
        return workAllocation;
    }

}
