package org.sunbird.workallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.model.Response;
import org.sunbird.common.util.Constants;
import org.sunbird.workallocation.model.*;
import org.sunbird.workallocation.util.Validator;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import java.io.IOException;
import java.util.*;

@Service
public class AllocationServiceV2 {

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
        enrichmentService.enrichWorkOrder(workOrder, userId);
        RestStatus restStatus = indexerService.addEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                mapper.convertValue(workOrder, Map.class));
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
     * @param userId user Id of the user
     * @param workOrder work order object
     * @return response message as success of failed
     */
    public Response updateWorkOrder(String userId, WorkOrderDTO workOrder) {
        validator.validateWorkOrder(workOrder, WorkAllocationConstants.UPDATE);
        enrichmentService.enrichWorkOrder(workOrder, userId);
        RestStatus restStatus = indexerService.updateEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                mapper.convertValue(workOrder, Map.class));
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
        validator.addWorkAllocation(workAllocationDTO);
        enrichmentService.enrichWorkAllocation(workAllocationDTO, userId);
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
        RestStatus restStatus = indexerService.addEntity(workAllocationIndex, workAllocationIndexType, workAllocationDTO.getId(),
                mapper.convertValue(workAllocationDTO, Map.class));
        Map<String, Object> workOrderObject = indexerService.readEntity(workOrderIndex, workOrderIndexType, workAllocationDTO.getWorkOrderId());
        WorkOrderDTO workOrder = mapper.convertValue(workOrderObject, WorkOrderDTO.class);
        if(CollectionUtils.isEmpty(workOrder.getUserIds()))workOrder.setUserIds(new ArrayList<>());
        if (!workOrder.getUserIds().contains(workAllocationDTO.getId())) {
            workOrder.addUserId(workAllocationDTO.getId());
            indexerService.updateEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                    mapper.convertValue(workOrder, Map.class));
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
                        roleCompetency.setRoleDetails(oldRole);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to Add Role / Activity. Excption: ", e);
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
            query.must(QueryBuilders.matchQuery("status", criteria.getStatus()));
        }
        if (!StringUtils.isEmpty(criteria.getDepartmentName())) {
            query.must(QueryBuilders.matchQuery("deptName", criteria.getDepartmentName()));
        }if(!StringUtils.isEmpty(criteria.getQuery())){
            query.must(QueryBuilders.matchPhrasePrefixQuery("name", criteria.getQuery()));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
        sourceBuilder.from(criteria.getPageNo());
        sourceBuilder.size(criteria.getPageSize());
        sourceBuilder.sort(SortBuilders.fieldSort("name.keyword").order(SortOrder.ASC));

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

    public Response getWorkOrderById(String workOrderId) throws IOException {
        Map<String, Object> workOrderObject = indexerService.readEntity(workOrderIndex, workOrderIndexType, workOrderId);
        List<Object> userList = null;
        if (!CollectionUtils.isEmpty((Collection<?>) workOrderObject.get("userIds"))) {
            userList = new ArrayList<>();
            final BoolQueryBuilder query = QueryBuilders.boolQuery();
            query.must(QueryBuilders.matchQuery("workOrderId", workOrderId));
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
            System.out.println(sourceBuilder.query().toString());
            SearchResponse searchResponse = indexerService.getEsResult(workAllocationIndex, workAllocationIndexType, sourceBuilder);
            for (SearchHit hit : searchResponse.getHits()) {
                userList.add(hit.getSourceAsMap());
            }
            workOrderObject.put("users", userList);
        } else {
            workOrderObject.put("users", new ArrayList<>());
        }
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.DATA, workOrderObject);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    /**
     *
     * @param userId user Id of the user
     * @param workOrder work order object
     * @return response message as success of failed
     */
    public Response copyWorkOrder(String userId, WorkOrderDTO workOrder) {
        validator.validateWorkOrder(workOrder, WorkAllocationConstants.ADD);
        workOrder.setStatus(null);
        enrichmentService.enrichWorkOrder(workOrder, userId);
        RestStatus restStatus = indexerService.addEntity(workOrderIndex, workOrderIndexType, workOrder.getId(),
                mapper.convertValue(workOrder, Map.class));
        for(String id : workOrder.getUserIds()){
            WorkAllocationDTOV2 workAllocationDTO = mapper.convertValue(indexerService.readEntity(workAllocationIndex, workOrderIndexType, id), WorkAllocationDTOV2.class);
            workAllocationDTO.setCreatedBy(null);
            enrichmentService.enrichWorkAllocation(workAllocationDTO, userId);
            workAllocationDTO.setId(UUID.randomUUID().toString());
            workAllocationDTO.setWorkOrderId(workOrder.getId());
            indexerService.addEntity(workAllocationIndex, workOrderIndexType, workAllocationDTO.getId(), mapper.convertValue(workAllocationDTO, Map.class));
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
    public Response getUserBasicDetails(String userId) throws IOException {
        Response response = new Response();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        response.put(Constants.DATA, allocationService.getUserDetails(userIds).get(userId));
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }
}
