package org.sunbird.workallocation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.Response;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.exception.ApplicationLogicError;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.*;
import org.sunbird.workallocation.repo.WorkAllocationRepo;
import org.sunbird.workallocation.repo.WorkOrderRepo;
import org.sunbird.workallocation.util.Validator;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import java.io.IOException;
import java.util.*;

@Service
public class AllocationServiceV2 {

    public static final String RESULT = "result";
    @Autowired
    private IndexerService indexerService;

    @Autowired
    private Validator validator;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;

    @Autowired
    private WorkAllocationRepo workAllocationRepo;

    @Autowired
    private WorkOrderRepo workOrderRepo;

    @Value("${workorder.index.name}")
    public String workOrderIndex;

    @Value("${workorder.index.type}")
    public String workOrderIndexType;

    @Value("${workallocationv2.index.name}")
    public String workAllocationIndex;

    @Value("${workallocation.index.type}")
    public String workAllocationIndexType;


    ObjectMapper mapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(AllocationServiceV2.class);

    /**
     *
     * @param userId user Id of the user
     * @param workOrder work order object
     * @return response message as success of failed
     */
    public Response addWorkOrder(String userId, WorkOrderDTO workOrder) {
        validator.validateWorkOrder(workOrder, WorkAllocationConstants.ADD);
        enrichmentService.enrichWorkOrder(workOrder, userId, WorkAllocationConstants.ADD);
        RestStatus restStatus = null;
        try {
            WorkOrderCassandraModel workOrderCassandraModel = new WorkOrderCassandraModel(workOrder.getId(), mapper.writeValueAsString(workOrder));
            workOrderRepo.save(workOrderCassandraModel);
            restStatus = indexerService.addEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                    mapper.convertValue(workOrder, Map.class));
        }catch (Exception ex){
            logger.error("Exception occurred while creating the work order", ex);
            throw new ApplicationLogicError("Exception occurred while creating the work order", ex);
        }
        Response response = new Response();
        if (!ObjectUtils.isEmpty(restStatus)) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        } else {
            response.put(Constants.MESSAGE, Constants.FAILED);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", workOrder.getId());
        response.put(Constants.DATA, data);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    /**
     *
     * @param userId user Id of the user
     * @param workOrder work order object
     * @return response message as success of failed
     */
    public Response updateWorkOrder(String userId, WorkOrderDTO workOrder, String xAuthUser) {
        validator.validateWorkOrder(workOrder, WorkAllocationConstants.UPDATE);
        enrichmentService.enrichWorkOrder(workOrder, userId, WorkAllocationConstants.UPDATE);
        RestStatus restStatus = null;
        try {
            WorkOrderCassandraModel workOrderCassandraModel = new WorkOrderCassandraModel(workOrder.getId(), mapper.writeValueAsString(workOrder));
            workOrderRepo.save(workOrderCassandraModel);
            restStatus = indexerService.updateEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                    mapper.convertValue(workOrder, Map.class));
            String publishedPdfLink = uploadPdfToContentService(workOrder, xAuthUser);
            if (!StringUtils.isEmpty(publishedPdfLink)) {
                workOrder.setPublishedPdfLink(publishedPdfLink);
                workOrderCassandraModel = new WorkOrderCassandraModel(workOrder.getId(), mapper.writeValueAsString(workOrder));
                workOrderRepo.save(workOrderCassandraModel);
                indexerService.updateEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                        mapper.convertValue(workOrder, Map.class));
            }
        } catch (Exception ex) {
            logger.error("Exception occurred while updating the work order", ex);
            throw new ApplicationLogicError("Exception occurred while updating the work order", ex);
        }
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
     * @param authUserToken auth token
     * @param userId user Id
     * @param workAllocationDTO work allocation object
     * @return
     */
    public Response addWorkAllocation(String authUserToken, String userId, WorkAllocationDTOV2 workAllocationDTO) {
        validator.validateWorkAllocation(workAllocationDTO, WorkAllocationConstants.ADD);
        enrichmentService.enrichWorkAllocation(workAllocationDTO, userId);
        RestStatus restStatus = null;
        if (StringUtils.isEmpty(workAllocationDTO.getId()))
            workAllocationDTO.setId(UUID.randomUUID().toString());
        if (!CollectionUtils.isEmpty(workAllocationDTO.getRoleCompetencyList())) {
            verifyRoleActivity(authUserToken, workAllocationDTO);
            verifyCompetencyDetails(authUserToken, workAllocationDTO);
        }
        if (StringUtils.isEmpty(workAllocationDTO.getPositionId())
                && !StringUtils.isEmpty(workAllocationDTO.getUserPosition())) {
            workAllocationDTO.setPositionId(allocationService.createUserPosition(authUserToken, workAllocationDTO.getUserPosition()));
        }
        try {
            WorkAllocationCassandraModel workAllocationCassandraModel = new WorkAllocationCassandraModel(workAllocationDTO.getId(), mapper.writeValueAsString(workAllocationDTO));
            workAllocationRepo.save(workAllocationCassandraModel);
            restStatus = indexerService.addEntity(workAllocationIndex, workAllocationIndexType, workAllocationDTO.getId(),
                    mapper.convertValue(workAllocationDTO, Map.class));
            Map<String, Object> workOrderObject = indexerService.readEntity(workOrderIndex, workOrderIndexType, workAllocationDTO.getWorkOrderId());
            WorkOrderDTO workOrder = mapper.convertValue(workOrderObject, WorkOrderDTO.class);
            if (CollectionUtils.isEmpty(workOrder.getUserIds())) {
                workOrder.setUserIds(new ArrayList<>());
            }
            workOrder.addUserId(workAllocationDTO.getId());
            updateWorkOderCount(workOrder);
            enrichmentService.enrichWorkOrder(workOrder, userId, WorkAllocationConstants.UPDATE);
            WorkOrderCassandraModel  workOrderCassandraModel = new WorkOrderCassandraModel(workOrder.getId(), mapper.writeValueAsString(workOrder));
            workOrderRepo.save(workOrderCassandraModel);
            indexerService.updateEntity(workOrderIndex, workOrderIndexType, workOrder.getId(), mapper.convertValue(workOrder, Map.class));
        }catch (Exception ex){
            logger.error("Exception occurred while saving the work allocation!!", ex);
            throw new ApplicationLogicError("Exception occurred while saving the work allocation!!", ex);
        }

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
     * @param authUserToken auth token
     * @param userId user Id
     * @param workAllocationDTO work allocation object
     * @return
     */
    public Response updateWorkAllocation(String authUserToken, String userId, WorkAllocationDTOV2 workAllocationDTO) {
        validator.validateWorkAllocation(workAllocationDTO, WorkAllocationConstants.UPDATE);
        enrichmentService.enrichWorkAllocation(workAllocationDTO, userId);
        RestStatus restStatus = null;
        if (!CollectionUtils.isEmpty(workAllocationDTO.getRoleCompetencyList())) {
            verifyRoleActivity(authUserToken, workAllocationDTO);
            verifyCompetencyDetails(authUserToken, workAllocationDTO);
        }
        if (StringUtils.isEmpty(workAllocationDTO.getPositionId())
                && !StringUtils.isEmpty(workAllocationDTO.getUserPosition())) {
            workAllocationDTO.setPositionId(allocationService.createUserPosition(authUserToken, workAllocationDTO.getUserPosition()));
        }
        try {
            WorkAllocationCassandraModel workAllocationCassandraModel = new WorkAllocationCassandraModel(workAllocationDTO.getId(), mapper.writeValueAsString(workAllocationDTO));
            workAllocationRepo.save(workAllocationCassandraModel);
            restStatus = indexerService.updateEntity(workAllocationIndex, workAllocationIndexType, workAllocationDTO.getId(),
                    mapper.convertValue(workAllocationDTO, Map.class));
            Map<String, Object> workOrderObject = indexerService.readEntity(workOrderIndex, workOrderIndexType, workAllocationDTO.getWorkOrderId());
            WorkOrderDTO workOrder = mapper.convertValue(workOrderObject, WorkOrderDTO.class);
            if (CollectionUtils.isEmpty(workOrder.getUserIds())) {
                workOrder.setUserIds(new ArrayList<>());
            }
            workOrder.addUserId(workAllocationDTO.getId());
            updateWorkOderCount(workOrder);
            enrichmentService.enrichWorkOrder(workOrder, userId, WorkAllocationConstants.UPDATE);
            WorkOrderCassandraModel  workOrderCassandraModel = new WorkOrderCassandraModel(workOrder.getId(), mapper.writeValueAsString(workOrder));
            workOrderRepo.save(workOrderCassandraModel);
            indexerService.updateEntity(workOrderIndex, workOrderIndexType, workOrder.getId(), mapper.convertValue(workOrder, Map.class));
        }catch (Exception ex){
            logger.error("Exception occurred while saving the work allocation!!", ex);
            throw new ApplicationLogicError("Exception occurred while saving the work allocation!!", ex);
        }
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

    private void verifyRoleActivity(String authUserToken, WorkAllocationDTOV2 workAllocation) {
        for (RoleCompetency roleCompetency : workAllocation.getRoleCompetencyList()) {
            Role oldRole = roleCompetency.getRoleDetails();
            Role newRole = null;
            try {
                if (StringUtils.isEmpty(oldRole.getId())) {
                    newRole = allocationService.fetchAddedRole(authUserToken, oldRole, null);
                    maintainExtraRoleInfo(newRole, roleCompetency);
                    roleCompetency.setRoleDetails(newRole);
                } else {
                    // Role is from FRAC - No need to create new.
                    // However, we need to check Activity is from FRAC or not.
                    if (!CollectionUtils.isEmpty(oldRole.getChildNodes())) {
                        List<ChildNode> newChildNodes = new ArrayList<>();
                        boolean isNewChildAdded = oldRole.getChildNodes().stream()
                                .anyMatch(childNode -> StringUtils.isEmpty(childNode.getId()));
                        if (isNewChildAdded) {
                            newRole = allocationService.fetchAddedRole(authUserToken, oldRole, null);
                            newChildNodes.addAll(newRole.getChildNodes());
                        } else {
                            newChildNodes.addAll(oldRole.getChildNodes());
                        }
                        oldRole.setChildNodes(newChildNodes);
                        maintainExtraRoleInfo(oldRole, roleCompetency);
                        roleCompetency.setRoleDetails(oldRole);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to Add Role / Activity. Excption: ", e);
            }
        }
    }

    private void maintainExtraRoleInfo(Role role, RoleCompetency roleCompetency) {
        if (!CollectionUtils.isEmpty(role.getChildNodes())) {
            for (ChildNode childNode : role.getChildNodes()) {
                if (!StringUtils.isEmpty(childNode.getDescription())) {
                    for (ChildNode childNode1 : roleCompetency.getRoleDetails().getChildNodes()) {
                        if (childNode.getDescription().equals(childNode1.getDescription())) {
                            childNode.setSubmittedFromId(childNode1.getSubmittedFromId());
                            childNode.setSubmittedFromName(childNode1.getSubmittedFromName());
                            childNode.setSubmittedFromEmail(childNode1.getSubmittedFromEmail());
                            childNode.setSubmittedToId(childNode1.getSubmittedToId());
                            childNode.setSubmittedToName(childNode1.getSubmittedToName());
                            childNode.setSubmittedToEmail(childNode1.getSubmittedToEmail());
                        }
                    }
                }
            }
        }
    }

    private void verifyCompetencyDetails(String authUserToken, WorkAllocationDTOV2 workAllocation) {
        for (RoleCompetency roleCompetency : workAllocation.getRoleCompetencyList()) {
            List<CompetencyDetails> oldCompetencyDetails = roleCompetency.getCompetencyDetails();
            List<CompetencyDetails> newCompetencyDetails = new ArrayList<>();
            allocationService.addOrUpdateCompetencyToFrac(authUserToken, oldCompetencyDetails, newCompetencyDetails);
            if (oldCompetencyDetails.size() == newCompetencyDetails.size()) {
                roleCompetency.setCompetencyDetails(newCompetencyDetails);
            } else {
                logger.error("Failed to create FRAC Competency / CompetencyLevel. Old List Size: {} , New List Size: {}", oldCompetencyDetails.size(), newCompetencyDetails.size());
            }
        }
    }

    public Response getWorkOrders(SearchCriteria criteria) {
        validator.validateSearchCriteria(criteria);
        final BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(criteria.getStatus())) {
            query.must(QueryBuilders.termQuery("status.keyword", criteria.getStatus()));
        }
        if (!StringUtils.isEmpty(criteria.getDepartmentName())) {
            query.must(QueryBuilders.termQuery("deptName.keyword", criteria.getDepartmentName()));
        }
        if(!StringUtils.isEmpty(criteria.getQuery())){
            query.must(QueryBuilders.matchPhrasePrefixQuery("name", criteria.getQuery()));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.from(criteria.getPageNo());
        sourceBuilder.size(criteria.getPageSize());
        sourceBuilder.sort(SortBuilders.fieldSort("updatedAt").order(SortOrder.DESC));

        List<WorkOrderDTO> workOrderDTOList = new ArrayList<>();
        long totalCount = 0;
        try {
            SearchResponse searchResponse = indexerService.getEsResult(workOrderIndex, workOrderIndexType, sourceBuilder);
            totalCount = searchResponse.getHits().getTotalHits();
            for (SearchHit hit : searchResponse.getHits()) {
                workOrderDTOList.add(mapper.convertValue(hit.getSourceAsMap(), WorkOrderDTO.class));
            }
        } catch (IOException e) {
            logger.error("Elastic Search Exception", e);
        }
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, workOrderDTOList);
        response.put("totalHit", totalCount);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    public Response getWorkOrderById(String workOrderId){
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, getWorkOrderObject(workOrderId));
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    public Map<String, Object> getWorkOrderObject(String workOrderId)
    {
        Map<String, Object> workOrderObject = indexerService.readEntity(workOrderIndex, workOrderIndexType, workOrderId);
        if (!CollectionUtils.isEmpty((Collection<?>) workOrderObject.get("userIds"))) {
            List<WorkAllocationDTOV2> workAllocationDTOV2List =  getWorkAllocationListByIds((List<String>)workOrderObject.get("userIds"));
            workOrderObject.put("users", workAllocationDTOV2List);
        } else {
            workOrderObject.put("users", new ArrayList<>());
        }
        return workOrderObject;
    }

    public Response getWorkAllocationById(String workAllocationId){
        Map<String, Object> workAllocationObject = indexerService.readEntity(workAllocationIndex, workAllocationIndexType, workAllocationId);
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, workAllocationObject);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    private SearchResponse getSearchResponseForWorkOrder(List<String> workAllocationIds) throws IOException {
        final BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("id.keyword", workAllocationIds));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        return indexerService.getEsResult(workAllocationIndex, workAllocationIndexType, sourceBuilder);
    }

    private List<WorkAllocationDTOV2> getWorkAllocationListByIds(List<String> workAllocationIds){
        List<WorkAllocationDTOV2> workAllocationDTOV2List = new ArrayList<>();
        if (!CollectionUtils.isEmpty(workAllocationIds)) {
            workAllocationIds.forEach(id -> {
                try {
                    WorkAllocationDTOV2 workAllocationDTOV2 = mapper.convertValue(indexerService.readEntity(workAllocationIndex, workAllocationIndexType, id), WorkAllocationDTOV2.class);
                    workAllocationDTOV2List.add(workAllocationDTOV2);
                } catch (Exception ex) {
                    logger.error("Exception occurred while reading the work allocation for id, {}", id);
                }
            });
        }
        return workAllocationDTOV2List;
    }

    /**
     *
     * @param userId user Id of the user
     * @param workOrderDTO work order object
     * @return response message as success of failed
     */
    public Response copyWorkOrder(String userId, WorkOrderDTO workOrderDTO) {
        if(StringUtils.isEmpty(workOrderDTO.getId())){
            throw new BadRequestException("Work Order Id should not be empty!");
        }
        Map<String, Object> workOrderObject = indexerService.readEntity(workOrderIndex, workOrderIndexType, workOrderDTO.getId());
        if(ObjectUtils.isEmpty(workOrderObject)){
            throw new BadRequestException("No work order found on given Id!");
        }
        WorkOrderDTO workOrder = mapper.convertValue(workOrderObject, WorkOrderDTO.class);
        if(!WorkAllocationConstants.PUBLISHED_STATUS.equals(workOrder.getStatus())){
            throw new BadRequestException("Can not copy the work order, work order is not in published status!");
        }
        validator.validateWorkOrder(workOrder, WorkAllocationConstants.ADD);
        if(!StringUtils.isEmpty(workOrderDTO.getName())){
            workOrder.setName(workOrderDTO.getName());
        }
        enrichmentService.enrichCopyWorkOrder(workOrder, userId);
        ArrayList<String> workAllocationIds = new ArrayList<>();
        List<IndexRequest> indexRequestList = new ArrayList<>();
        List<WorkAllocationCassandraModel> cassandraModelList = new ArrayList<>();
        prepareWorkAllocations(userId, workOrder, workAllocationIds, indexRequestList, cassandraModelList);
        RestStatus restStatus = null;
        if(!CollectionUtils.isEmpty(indexRequestList)){
            workAllocationRepo.saveAll(cassandraModelList);
            indexerService.BulkInsert(indexRequestList);
        }
        workOrder.setUserIds(workAllocationIds);
        WorkOrderCassandraModel workOrderCassandraModel = null;
        try {
            workOrderCassandraModel = new WorkOrderCassandraModel(workOrder.getId(), mapper.writeValueAsString(workOrder));
            workOrderRepo.save(workOrderCassandraModel);
            restStatus = indexerService.addEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                    mapper.convertValue(workOrder, Map.class));
        } catch (JsonProcessingException e) {
            logger.error("Exception occurred while saving the work order!!", e);
            throw new ApplicationLogicError("Exception occurred while saving the work order!!", e);
        }
        Response response = new Response();
        if (!ObjectUtils.isEmpty(restStatus)) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        } else {
            response.put(Constants.MESSAGE, Constants.FAILED);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", workOrder.getId());
        response.put(Constants.DATA, data);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    private void prepareWorkAllocations(String userId, WorkOrderDTO workOrder, ArrayList<String> workAllocationIds,
                                        List<IndexRequest> indexRequestList, List<WorkAllocationCassandraModel> cassandraModelList) {
        try {
            if (!CollectionUtils.isEmpty(workOrder.getUserIds())) {
                for (String id : workOrder.getUserIds()) {
                    WorkAllocationDTOV2 workAllocationDTO = mapper.convertValue(indexerService.readEntity(workAllocationIndex, workOrderIndexType, id), WorkAllocationDTOV2.class);
                    if (!ObjectUtils.isEmpty(workAllocationDTO)) {
                        workAllocationDTO.setCreatedBy(null);
                        enrichmentService.enrichWorkAllocation(workAllocationDTO, userId);
                        workAllocationDTO.setId(UUID.randomUUID().toString());
                        workAllocationDTO.setWorkOrderId(workOrder.getId());
                        workAllocationIds.add(workAllocationDTO.getId());
                        WorkAllocationCassandraModel workAllocationCassandraModel = null;
                        workAllocationCassandraModel = new WorkAllocationCassandraModel(workAllocationDTO.getId(), mapper.writeValueAsString(workAllocationDTO));
                        cassandraModelList.add(workAllocationCassandraModel);
                        IndexRequest indexRequest = new IndexRequest(workAllocationIndex, workAllocationIndexType, workAllocationDTO.getId()).
                                source(mapper.convertValue(workAllocationDTO, Map.class));
                        indexRequestList.add(indexRequest);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Exception occurred while preparing the copy work allocation!", e);
            throw new ApplicationLogicError("Exception occurred while preparing the copy work allocation!", e);
        }
    }

    public Response getUserBasicDetails(String userId) throws IOException {
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        response.put(Constants.DATA, allocationService.getUserDetails(userIds).get(userId));
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    private void updateWorkOderCount(WorkOrderDTO workOrderDTO) {
        int rolesCount = 0;
        int activitiesCount = 0;
        int competenciesCount = 0;
        int errorCount = 0;
        int progress = 0;
        List<WorkAllocationDTOV2> workAllocationList =  getWorkAllocationListByIds(workOrderDTO.getUserIds());
        for (WorkAllocationDTOV2 workAllocationDTOV2 : workAllocationList) {
            if (!CollectionUtils.isEmpty(workAllocationDTOV2.getRoleCompetencyList())) {
                rolesCount = rolesCount + workAllocationDTOV2.getRoleCompetencyList().size();
                for (RoleCompetency roleCompetency : workAllocationDTOV2.getRoleCompetencyList()) {
                    if (!ObjectUtils.isEmpty(roleCompetency.getRoleDetails()) && !CollectionUtils.isEmpty(roleCompetency.getRoleDetails().getChildNodes()))
                        activitiesCount = activitiesCount + roleCompetency.getRoleDetails().getChildNodes().size();
                    if (!ObjectUtils.isEmpty(roleCompetency.getRoleDetails()) && !CollectionUtils.isEmpty(roleCompetency.getCompetencyDetails()))
                        competenciesCount = competenciesCount + roleCompetency.getCompetencyDetails().size();
                }
            }
            if (!CollectionUtils.isEmpty(workAllocationDTOV2.getUnmappedActivities())) {
                activitiesCount = activitiesCount + workAllocationDTOV2.getUnmappedActivities().size();
            }
            if (!CollectionUtils.isEmpty(workAllocationDTOV2.getUnmappedCompetencies())) {
                competenciesCount = competenciesCount + workAllocationDTOV2.getUnmappedCompetencies().size();
            }
            errorCount = errorCount + workAllocationDTOV2.getErrorCount();
            progress = progress + workAllocationDTOV2.getProgress();
        }
        if (!CollectionUtils.isEmpty(workAllocationList)) {
            progress = progress / workAllocationList.size();
        }
        workOrderDTO.setRolesCount(rolesCount);
        workOrderDTO.setActivitiesCount(activitiesCount);
        workOrderDTO.setCompetenciesCount(competenciesCount);
        workOrderDTO.setErrorCount(errorCount);
        workOrderDTO.setProgress(progress);
    }

    private String uploadPdfToContentService(WorkOrderDTO workOrderDTO, String xAuthUser) {
        String pdfLink = null;
        try {
            String pdfFilePath = pdfGeneratorService.generatePdfAndGetFilePath(workOrderDTO.getId());
            String identifier = createContentAndGetIdentifier(workOrderDTO, xAuthUser);
            if (StringUtils.isEmpty(identifier)) {
                logger.error("Fail to generate the pdf asset");
                return pdfLink;
            }
            pdfLink = uploadPdfAndgetArtifactURL(identifier, xAuthUser, pdfFilePath);

        } catch (Exception ex) {
            logger.error("Exception occurred while creating the pdf link for published work order!", ex);
        }
        return pdfLink;
    }

    private String createContentAndGetIdentifier(WorkOrderDTO workOrderDTO, String xAuthUser) {
        String identifier = null;
        ContentCreateRequest contentCreateRequest = new ContentCreateRequest(
                "PDF Asset", workOrderDTO.getUpdatedByName(), workOrderDTO.getUpdatedBy(), "pdf asset", "application/pdf",
                "Asset", "Asset", Arrays.asList(cbExtServerProperties.getContentDefaultOrgId()), Arrays.asList(cbExtServerProperties.getContentDefaultChannelId()));
        HashMap<String, Object> request = new HashMap<>();
        HashMap<String, Object> contentReq = new HashMap<>();
        contentReq.put("content", contentCreateRequest);
        request.put("request", contentReq);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-channel-id", cbExtServerProperties.getContentDefaultChannelId());
        headers.put("X-Authenticated-User-Token", xAuthUser);
        headers.put("Authorization", cbExtServerProperties.getSbApiKey());
        headers.put("Content-Type", "application/json");
        Map<String, Object> response = outboundRequestHandlerService.fetchResultUsingPost(cbExtServerProperties.getContentHost().concat(cbExtServerProperties.getContentCreateEndPoint()), request, headers);
        if (!ObjectUtils.isEmpty(response.get(RESULT)))
            identifier = (String) ((Map<String, Object>) response.get(RESULT)).get("identifier");
        return identifier;
    }

    private String uploadPdfAndgetArtifactURL(String identifier, String xAuthUser, String filePath) {
        String downloadableLink = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authenticated-User-Token", xAuthUser);
        headers.set("Authorization", cbExtServerProperties.getSbApiKey());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        FileSystemResource resource = new FileSystemResource(filePath);
        body.add("data", resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        String uploadURL = cbExtServerProperties.getContentUploadEndPoint().replace("{identifier}", identifier);
        ResponseEntity<Map> response = restTemplate
                .postForEntity(cbExtServerProperties.getContentHost().concat(uploadURL), requestEntity, Map.class);
        if (!ObjectUtils.isEmpty(response.getBody())){
            downloadableLink = (String) ((Map<String, Object>) response.getBody().get(RESULT)).get("artifactUrl");
        }
        return downloadableLink;
    }

}