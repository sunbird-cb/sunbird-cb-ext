package org.sunbird.workallocation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.lucene.search.join.ScoreMode;
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
import org.sunbird.common.service.PdfGenerationService;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.IndexerService;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.Child;
import org.sunbird.workallocation.model.ChildNode;
import org.sunbird.workallocation.model.CompetencyDetails;
import org.sunbird.workallocation.model.FracRequest;
import org.sunbird.workallocation.model.FracResponse;
import org.sunbird.workallocation.model.Role;
import org.sunbird.workallocation.model.RoleCompetency;
import org.sunbird.workallocation.model.SearchCriteria;
import org.sunbird.workallocation.model.WAObject;
import org.sunbird.workallocation.model.WorkAllocation;
import org.sunbird.workallocation.model.WorkAllocationDTO;
import org.sunbird.workallocation.util.FRACReqBuilder;
import org.sunbird.workallocation.util.Validator;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AllocationService {

	public static final String AUTHORIZATION = "Authorization";
	public static final String ACCEPT = "Accept";
	private Logger logger = LoggerFactory.getLogger(AllocationService.class);

	final String[] includeFields = { "personalDetails.firstname", "personalDetails.surname",
			"personalDetails.primaryEmail", "id", "professionalDetails.name", "professionalDetails.designation" };

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
	private FRACReqBuilder fracReqBuilder;

	@Value("${workallocation.index.name}")
	public String index;

	@Value("${workallocation.index.type}")
	public String indexType;

	@Autowired
	private PdfGenerationService pdfService;

	ObjectMapper mapper = new ObjectMapper();

	/**
	 * @param userId            user id
	 * @param workAllocationDTO work allocation object
	 * @return response
	 */
	public Response addWorkAllocation(String userAuthToken, String userId, WorkAllocationDTO workAllocationDTO) {

		validator.validateWorkAllocationReq(workAllocationDTO, WorkAllocationConstants.ADD);
		if (WorkAllocationConstants.DRAFT_STATUS.equals(workAllocationDTO.getStatus())) {
			if (StringUtils.isEmpty(workAllocationDTO.getUserId())) {
				workAllocationDTO.setUserId(UUID.randomUUID().toString());
			}
			if (StringUtils.isEmpty(workAllocationDTO.getWaId())) {
				workAllocationDTO.setWaId(UUID.randomUUID().toString());
			}
		}

		if (!CollectionUtils.isEmpty(workAllocationDTO.getRoleCompetencyList())) {
			verifyRoleActivity(userAuthToken, workAllocationDTO);
			verifyCompetencyDetails(userAuthToken, workAllocationDTO);
		}
		// To - Do
		// Add competency to FRAC
		if (StringUtils.isEmpty(workAllocationDTO.getPositionId())
				&& !StringUtils.isEmpty(workAllocationDTO.getUserPosition())) {
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
		validator.validateWorkAllocationReq(workAllocationDTO, WorkAllocationConstants.UPDATE);
		if (!CollectionUtils.isEmpty(workAllocationDTO.getRoleCompetencyList())) {
			verifyRoleActivity(authUserToken, workAllocationDTO);
		}
		if (StringUtils.isEmpty(workAllocationDTO.getPositionId())
				&& !StringUtils.isEmpty(workAllocationDTO.getUserPosition())) {
			workAllocationDTO.setPositionId(createUserPosition(authUserToken, workAllocationDTO.getUserPosition()));
		}
		Map<String, Object> existingRecord = indexerService.readEntity(index, indexType, workAllocationDTO.getUserId());
		WorkAllocation workAllocation;
		if (CollectionUtils.isEmpty(existingRecord)) {
			throw new BadRequestException("No record found on given Id!");
		}
		workAllocation = mapper.convertValue(existingRecord, WorkAllocation.class);
		WorkAllocation finalObj = waObjectTransition(userId, workAllocationDTO, workAllocation);
		RestStatus restStatus = indexerService.updateEntity(index, indexType, workAllocation.getUserId(),
				mapper.convertValue(finalObj, Map.class));
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
		final BoolQueryBuilder query = QueryBuilders.boolQuery();
			if (WorkAllocationConstants.DRAFT_STATUS.equals(criteria.getStatus())) {
				query.must(QueryBuilders.matchQuery("draftWAObject.deptName", criteria.getDepartmentName()))
						.must(QueryBuilders.matchQuery("draftWAObject.status", criteria.getStatus()));
			}
			if (WorkAllocationConstants.PUBLISHED_STATUS.equals(criteria.getStatus())) {
				query.must(QueryBuilders.matchQuery("activeWAObject.deptName", criteria.getDepartmentName()))
						.must(QueryBuilders.matchQuery("activeWAObject.status", criteria.getStatus()));
			}
			if (WorkAllocationConstants.ARCHIVED_STATUS.equals(criteria.getStatus())) {
				query.must(QueryBuilders.nestedQuery("archivedWAList",
						QueryBuilders.matchQuery("archivedWAList.deptName", criteria.getDepartmentName()), ScoreMode.None));
			}
			if(!StringUtils.isEmpty(criteria.getUserId())){
				query.must(QueryBuilders.matchQuery("userId", criteria.getUserId()));
			}
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
		sourceBuilder.from(criteria.getPageNo());
		sourceBuilder.size(criteria.getPageSize());
		if (WorkAllocationConstants.DRAFT_STATUS.equals(criteria.getStatus()))
			sourceBuilder.sort(SortBuilders.fieldSort("draftWAObject.updatedAt").order(SortOrder.DESC));
		if (WorkAllocationConstants.PUBLISHED_STATUS.equals(criteria.getStatus()))
			sourceBuilder.sort(SortBuilders.fieldSort("activeWAObject.updatedAt").order(SortOrder.DESC));
		List<WorkAllocation> allocationSearchList = new ArrayList<>();
		List<Map<String, Object>> finalRes = new ArrayList<>();
		Map<String, Object> result;
		long totalCount = 0;
		try {
			SearchResponse searchResponse = indexerService.getEsResult(index, indexType, sourceBuilder, false);
			totalCount = searchResponse.getHits().getTotalHits();
			for (SearchHit hit : searchResponse.getHits()) {
				allocationSearchList.add(mapper.convertValue(hit.getSourceAsMap(), WorkAllocation.class));
			}
			Set<String> userIds = allocationSearchList.stream().map(WorkAllocation::getUserId)
					.collect(Collectors.toSet());
			Map<String, Object> usersMap = getUserDetails(userIds);
			for (WorkAllocation workAllocation : allocationSearchList) {
				result = new HashMap<>();
				result.put(WorkAllocationConstants.ALLOCATION_DETAILS, workAllocation);
				result.put(WorkAllocationConstants.USER_DETAILS, usersMap.get(workAllocation.getUserId()));
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
		sourceBuilder.fetchSource(includeFields, new String[] {});
		SearchResponse searchResponse = indexerService.getEsResult(configuration.getEsProfileIndex(),
				configuration.getEsProfileIndexType(), sourceBuilder, false);
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
		String designation = null;
		if (!CollectionUtils.isEmpty(professionalDetails)) {
			Optional<Map<String, Object>> propDetails = professionalDetails.stream().findFirst();
			if (propDetails.isPresent()) {
				depName = CollectionUtils.isEmpty(propDetails.get()) ? "" : (String) propDetails.get().get("name");
				designation = CollectionUtils.isEmpty(propDetails.get()) ? "" : (String) propDetails.get().get("designation");
			}
		}
		HashMap<String, Object> result = new HashMap<>();
		result.put("first_name", personalDetails.get("firstname"));
		result.put("last_name", personalDetails.get("surname"));
		result.put("email", personalDetails.get("primaryEmail"));
		result.put("wid", searObjectMap.get("id"));
		result.put("department_name", depName);
		result.put("designation", designation);
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
		sourceBuilder.fetchSource(includeFields, new String[] {});
		SearchResponse searchResponse = indexerService.getEsResult(configuration.getEsProfileIndex(),
				configuration.getEsProfileIndexType(), sourceBuilder, false);
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
		try {
			userData = getUserSearchData(searchTerm);
		} catch (IOException e) {
			logger.error("Exception occurred while searching the user's from user registry", e);
		}
		List<Map<String, Object>> finalRes = new ArrayList<>();
		Map<String, Object> result;
		if (!CollectionUtils.isEmpty(userData)) {
			for (Map<String, Object> user : userData) {
				result = new HashMap<>();
				result.put(WorkAllocationConstants.USER_DETAILS, user);
				result.put(WorkAllocationConstants.ALLOCATION_DETAILS, null);
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
		for (RoleCompetency roleCompetency : workAllocation.getRoleCompetencyList()) {
			Role oldRole = roleCompetency.getRoleDetails();
			Role newRole = null;
			try {
				if (StringUtils.isEmpty(oldRole.getId())) {
					newRole = fetchAddedRole(authUserToken, oldRole, null);
					roleCompetency.setRoleDetails(newRole);
				} else {
					// Role is from FRAC - No need to create new.
					// However, we need to check Activity is from FRAC or not.
					if (!CollectionUtils.isEmpty(oldRole.getChildNodes())) {
						List<ChildNode> newChildNodes = new ArrayList<>();
						boolean isNewChildAdded = oldRole.getChildNodes().stream()
								.anyMatch(childNode -> StringUtils.isEmpty(childNode.getId()));
						if (isNewChildAdded) {
							newRole = fetchAddedRole(authUserToken, oldRole, null);
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

	public String createUserPosition(String authUserToken, String positionName) {
		logger.info("Adding Position into FRAC System...");
		String positionId = null;
		FracRequest request = fracReqBuilder.getPositionRequest(getSourceValue(), positionName);
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, authUserToken);
		headers.add(ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = null;
		try {
			entity = new HttpEntity<>(mapper.writeValueAsString(request), headers);
			FracResponse response = restTemplate.postForObject(
					extServerProperties.getFracHost() + extServerProperties.getFracActivityPath(), entity,
					FracResponse.class);
			if (!ObjectUtils.isEmpty(response) && !ObjectUtils.isEmpty(response.getResponseData())) {
				positionId = response.getResponseData().getId();
			}
			logger.info("Added Position successful ...");
		} catch (JsonProcessingException e) {
			logger.error("Parsing Exception While Creating the Position in Frac", e);
		}
		return positionId;
	}

	public Role fetchAddedRole(String authUserToken, Role role, ChildNode cn) throws JsonProcessingException {
		logger.info("Adding Role into FRAC Service...");

		FracRequest request = role.getFracRequest(getSourceValue(), cn);
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, authUserToken);
		headers.add(ACCEPT, MediaType.APPLICATION_JSON_VALUE);
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
		return extServerProperties.getFracSource();
	}

	private Role processRole(FracResponse response) {
		Role r = new Role();
		r.setId(response.getResponseData().getId());
		r.setName(response.getResponseData().getName());
		r.setType(response.getResponseData().getType());
		r.setStatus(response.getResponseData().getStatus());
		r.setSource(response.getResponseData().getSource());
		r.setDescription(response.getResponseData().getDescription());
		List<ChildNode> children = response.getResponseData().getChildren();
		if (children != null && !CollectionUtils.isEmpty(children)) {
			List<ChildNode> childNodes = new ArrayList<>();
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
		cn.setDescription(child.getDescription());
		cn.setLevel(child.getLevel());
		return cn;
	}

	private WAObject getWAObject(String userId, WorkAllocationDTO dto) {
		long currentMillis = System.currentTimeMillis();
		WAObject wa = new WAObject();
		wa.setDeptId(dto.getDeptId());
		wa.setDeptName(dto.getDeptName());
		wa.setId(dto.getWaId());
		wa.setPositionId(dto.getPositionId());
		wa.setRoleCompetencyList(dto.getRoleCompetencyList());
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
		if (WorkAllocationConstants.DRAFT_STATUS.equals(dto.getStatus())) {
			if (!ObjectUtils.isEmpty(deepCopy) && ObjectUtils.isEmpty(deepCopy.getDraftWAObject())) {
				wa.setCreatedBy(userId);
				wa.setCreatedAt(currentMillis);
			}
			workAllocation.setDraftWAObject(wa);
		}
		if (WorkAllocationConstants.PUBLISHED_STATUS.equals(dto.getStatus())) {
			if (!ObjectUtils.isEmpty(deepCopy) && !ObjectUtils.isEmpty(deepCopy.getActiveWAObject())) {
				WAObject oldPublishObject = deepCopy.getActiveWAObject();
				oldPublishObject.setStatus(WorkAllocationConstants.ARCHIVED_STATUS);
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

	private void verifyCompetencyDetails(String authUserToken, WorkAllocationDTO workAllocation) {
		for (RoleCompetency roleCompetency : workAllocation.getRoleCompetencyList()) {
			List<CompetencyDetails> oldCompetencyDetails = roleCompetency.getCompetencyDetails();
			List<CompetencyDetails> newCompetencyDetails = new ArrayList<>();
			addOrUpdateCompetencyToFrac(authUserToken, oldCompetencyDetails, newCompetencyDetails);
			if (oldCompetencyDetails.size() == newCompetencyDetails.size()) {
				roleCompetency.setCompetencyDetails(newCompetencyDetails);
			} else {
				logger.error("Failed to create FRAC Competency / CompetencyLevel. Old List Size: {} , New List Size: {}", oldCompetencyDetails.size(), newCompetencyDetails.size());
			}
		}
	}

	public void addOrUpdateCompetencyToFrac(String authUserToken, List<CompetencyDetails> oldCompetencyDetails, List<CompetencyDetails> newCompetencyDetails) {
		try {
			for (CompetencyDetails c : oldCompetencyDetails) {
				if (StringUtils.isEmpty(c.getId())) {
					CompetencyDetails newCompetency = fetchAddedComptency(authUserToken, c, null);
					newCompetency.setLevel(c.getLevel());
					newCompetencyDetails.add(newCompetency);
				} else {
					// Competency is from FRAC - No need to create new.
					// However, we need to check children is from FRAC or not.
					boolean isNewChildFound = false;
					if (!CollectionUtils.isEmpty(c.getChildren())) {
						isNewChildFound = c.getChildren().stream()
								.anyMatch(childNode -> StringUtils.isEmpty(childNode.getId()));
					} else {
						isNewChildFound = true;
					}

					if (isNewChildFound) {
						CompetencyDetails newCompetency = fetchAddedComptency(authUserToken, c, null);
						newCompetency.setLevel(c.getLevel());
						newCompetencyDetails.add(newCompetency);
					} else {
						newCompetencyDetails.add(c);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Failed to Add Competency / Competency area. Exception: ", e);
		}
	}

	private CompetencyDetails fetchAddedComptency(String authUserToken, CompetencyDetails competency, Child cn) throws JsonProcessingException {
		logger.info("Adding Competency into FRAC Service...");

		FracRequest request = competency.getFracRequest(getSourceValue(), cn);
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, authUserToken);
		headers.add(ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(request), headers);
		FracResponse response = restTemplate.postForObject(
				extServerProperties.getFracHost() + extServerProperties.getFracNodePath(), entity, FracResponse.class);
		if (response != null && response.getStatusInfo().getStatusCode() == 200) {
			return processCompetencyResponse(response);
		}
		return competency;
	}

	private CompetencyDetails processCompetencyResponse(FracResponse response) {
		CompetencyDetails r = new CompetencyDetails();
		r.setId(response.getResponseData().getId());
		r.setName(response.getResponseData().getName());
		r.setType(response.getResponseData().getType());
		r.setStatus(response.getResponseData().getStatus());
		r.setSource(response.getResponseData().getSource());
		r.setAdditionalProperties(response.getResponseData().getAdditionalProperties());
		r.setDescription(response.getResponseData().getDescription());
		List<ChildNode> children = response.getResponseData().getChildren();
		if (children != null && !CollectionUtils.isEmpty(children)) {
			List<Child> childNodes = new ArrayList<>();
			for (ChildNode child : children) {
				childNodes.add(processCompetencyLevel(child));
			}
			r.setChildren(childNodes);
		}
		return r;
	}

	private Child processCompetencyLevel(ChildNode child) {
		Child cn = new Child();
		cn.setId(child.getId());
		cn.setType(child.getType());
		cn.setName(child.getName());
		cn.setStatus(child.getStatus());
		cn.setSource(child.getSource());
		cn.setLevel(child.getLevel());
		cn.setDescription(child.getDescription());
		return cn;
	}

	public byte[] getWaPdf(String userId, String waId){
		Map<String, Object> existingRecord = indexerService.readEntity(index, indexType, userId);
		if (CollectionUtils.isEmpty(existingRecord)) {
			return pdfService.getWaErrorPdf("Failed to find Work Allocation details for given User.");
		}
		String statusSelected = null;

		WorkAllocation wa = mapper.convertValue(existingRecord, WorkAllocation.class);
		WAObject waObj = null;
		if (!ObjectUtils.isEmpty(wa.getActiveWAObject()) && waId.equals(wa.getActiveWAObject().getId())) {
			waObj = wa.getActiveWAObject();
			statusSelected = WorkAllocationConstants.PUBLISHED_STATUS;
		} else if (!ObjectUtils.isEmpty(wa.getDraftWAObject()) && waId.equals(wa.getDraftWAObject().getId())) {
			waObj = wa.getDraftWAObject();
			statusSelected = WorkAllocationConstants.DRAFT_STATUS;
		}

		if (ObjectUtils.isEmpty(waObj)) {
			return pdfService.getWaErrorPdf("Work allocation details not found or superseded by new order. Please contact Department Administrator.");
		}

		// If status Draft
		if (WorkAllocationConstants.DRAFT_STATUS.equalsIgnoreCase(waObj.getStatus())) {
		}
		
		return pdfService.getWAPdf(wa, statusSelected);
	}
}