package org.sunbird.portal.department.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.OpenSaberApiUserProfile;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiRespContent;
import org.sunbird.common.model.SunbirdApiResultResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.service.UserUtilityService;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.DataValidator;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.portal.department.PortalConstants;
import org.sunbird.portal.department.dto.*;
import org.sunbird.portal.department.model.*;
import org.sunbird.portal.department.repo.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortalServiceImpl implements PortalService {

	public static final String NO_RECORDS_EXIST_FOR_USER_ID = "No records exist for UserId: ";
	public static final String LIST_OF_USER_RECORDS = "List of User Records -> ";
	public static final String DEPT_IDS = ", DeptIds: ";
	public static final String DEPARTMENT_NAME = "departmentName";
	private CbExtLogger logger = new CbExtLogger(getClass().getName());
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	UserDepartmentRoleRepository userDepartmentRoleRepo;

	@Autowired
	DepartmentRepository deptRepo;

	@Autowired
	DepartmentTypeRepository deptTypeRepo;

	@Autowired
	DepartmentRoleRepository deptRoleRepo;

	@Autowired
	RoleRepository roleRepo;

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	Producer producer;

	@Autowired
	RedisCacheMgr redisCacheMgr;

	@Autowired
	CassandraOperation cassandraOperation;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	private static final String ROOT_ORG_CONST = "rootOrg";
	private static final String ORG_CONST = "org";

	@Override
	public List<String> getDeptNameList() {
		try {
			Long orgCount = cassandraOperation.getRecordCount(Constants.DATABASE, Constants.ORGANIZATION);
			List<String> orgNameList = new ArrayList<>();
			List<String> cachedOrgNames = mapper.convertValue(redisCacheMgr.getCache(Constants.ORG_LIST), List.class);
			if (ObjectUtils.isEmpty(cachedOrgNames) || !Long.valueOf(cachedOrgNames.size()).equals(orgCount)) {
				List<String> orgNames = getAllOrg();
				redisCacheMgr.putCache(Constants.ORG_LIST, orgNames);
				return orgNames;
			}
			return cachedOrgNames;

		} catch (Exception e) {
			logger.info("Exception occurred in getDeptNameList");
			logger.error(e);
		}
		return Collections.emptyList();
	}

	private List<String> getAllOrg() throws Exception {
		List<String> orgNames = new ArrayList<>();
		int count = 0;
		do {
			// request body
			Map<String, Object> requestMap = new HashMap<String, Object>() {
				{
					put(Constants.REQUEST, new HashMap<String, Object>() {
						{
							put(Constants.FILTERS, new HashMap<String, Object>() {
								{
									put(Constants.IS_TENANT, Boolean.TRUE);
								}
							});
							put(Constants.FIELDS, new ArrayList<>(Arrays.asList(Constants.CHANNEL)));
							put(Constants.LIMIT, 100);
							put(Constants.OFFSET, orgNames.size());
						}
					});
				}
			};
			String serviceURL = serverConfig.getSbUrl() + serverConfig.getSbOrgSearchPath();
			SunbirdApiResp orgResponse = mapper.convertValue(
					outboundRequestHandlerService.fetchResultUsingPost(serviceURL, requestMap), SunbirdApiResp.class);
			SunbirdApiResultResponse resultResp = orgResponse.getResult().getResponse();
			count = resultResp.getCount();
			orgNames.addAll(resultResp.getContent().stream().map(SunbirdApiRespContent::getChannel)
					.collect(Collectors.toList()));
		} while (count != orgNames.size());
		return orgNames;
	}

	@Override
	public List<DeptPublicInfo> getAllDept() {
		List<Department> deptList = deptRepo.findAllByIsDeletedOrderByDeptNameAsc(false);
		if (!DataValidator.isCollectionEmpty(deptList)) {
			List<DeptPublicInfo> publicDeptList = new ArrayList<>(deptList.size());
			for (Department dept : deptList) {
				publicDeptList.add(dept.getPublicInfo());
			}
			return publicDeptList;
		}
		return Collections.emptyList();
	}

	@Override
	public DeptPublicInfo searchDept(String deptName) {
		Department dept = deptRepo.findByDeptNameIgnoreCaseAndIsDeleted(deptName, false);
		if (dept != null) {
			return dept.getPublicInfo();
		}
		return null;
	}

	@Override
	public List<DepartmentInfo> getAllDepartments(String rootOrg) {
		return enrichDepartmentInfo(deptRepo.findAllByIsDeletedOrderByDeptNameAsc(false), true, rootOrg);
	}

	@Override
	public DepartmentInfo getDepartmentById(Integer deptId, boolean isUserInfoRequired, String rootOrg) {
		return enrichDepartmentInfo(deptId, isUserInfoRequired, true, rootOrg);
	}

	@Override
	public DepartmentInfo getMyDepartment(String deptType, String userId, boolean isUserInfoRequired, String rootOrg) {
		return enrichDepartmentInfo(getMyActiveDepartment(deptType, userId), isUserInfoRequired, true, rootOrg);
	}

	@Override
	public DepartmentInfo getMyDepartmentForRole(String roleName, String userId, boolean isUserInfoRequired,
			String rootOrg) {
		return enrichDepartmentInfo(getMyCurrentDepartment(roleName, userId), isUserInfoRequired, true, rootOrg);
	}

	public DepartmentInfo getMyDepartmentForRoles(List<String> roleNames, String userId, boolean isUserInfoRequired,
			String rootOrg) {
		return enrichDepartmentInfo(getMyCurrentDepartment(roleNames, userId), isUserInfoRequired, true, rootOrg);
	}

	@Override
	public DepartmentInfo getMyCbpDepartment(String userId, String rootOrg) {
		Department myDept = null;
		List<UserDepartmentRole> userList = userDepartmentRoleRepo.findAllByUserIdAndIsActiveAndIsBlocked(userId, true,
				false);
		if (DataValidator.isCollectionEmpty(userList)) {
			throw new BadRequestException(NO_RECORDS_EXIST_FOR_USER_ID + userId);
		}
		List<Integer> deptIds = userList.stream().map(UserDepartmentRole::getDeptId).collect(Collectors.toList());
		logger.info(LIST_OF_USER_RECORDS + userList.size() + DEPT_IDS + deptIds.toString());

		Iterable<Department> deptList = deptRepo.findAllById(deptIds);

		for (Department dept : deptList) {
			Iterable<DepartmentType> deptTypeList = deptTypeRepo.findAllById(Arrays.asList(dept.getDeptTypeIds()));
			for (DepartmentType deptType : deptTypeList) {
				if (deptType.getDeptType().equalsIgnoreCase(PortalConstants.CBP_DEPT_TYPE)
						&& hasCBPRole(dept, userList)) {
					// We found a CBP Type department which user is mapped... Now need to check user
					// has any roles related CBP Portal in this department
					if (myDept != null) {
						throw new BadRequestException("More than one CBP Department is available for the User. ");
					} else {
						myDept = dept;
					}
				}
			}
		}

		return enrichDepartmentInfo(myDept, false, false, rootOrg);
	}

	private Department getMyCurrentDepartment(List<String> roleNames, String userId) {
		List<UserDepartmentRole> userList = userDepartmentRoleRepo.findAllByUserIdAndIsActiveAndIsBlocked(userId, true,
				false);
		if (DataValidator.isCollectionEmpty(userList)) {
			throw new BadRequestException(NO_RECORDS_EXIST_FOR_USER_ID + userId);
		}
		List<Integer> deptIds = userList.stream().map(UserDepartmentRole::getDeptId).collect(Collectors.toList());
		logger.info(LIST_OF_USER_RECORDS + userList.size() + DEPT_IDS + deptIds.toString());

		Iterable<Department> deptList = deptRepo.findAllById(deptIds);
		Department myDept = null;
		Map<Integer, Department> deptMap = new HashMap<>();
		if (!DataValidator.isCollectionEmpty(deptList)) {
			for (Department dept : deptList) {
				deptMap.put(dept.getDeptId(), dept);
			}
		}
		return filterCurrentDeptInfo(roleNames, userList, myDept, deptMap);
	}

	private Department filterCurrentDeptInfo(List<String> roleNames, List<UserDepartmentRole> userList,
			Department myDept, Map<Integer, Department> deptMap) {
		for (UserDepartmentRole user : userList) {
			Iterable<Role> roles = roleRepo.findAllById(Arrays.asList(user.getRoleIds()));
			for (Role r : roles) {
				if (roleNames.contains(r.getRoleName())) {
					if (myDept != null) {
						if (!myDept.getDeptId().equals(deptMap.get(user.getDeptId()).getDeptId())) {
							throw new BadRequestException(
									"More than one Department is available with Role: " + r.getRoleName());
						}
					} else {
						myDept = deptMap.get(user.getDeptId());
					}
				}
			}
		}
		return myDept;
	}

	private Department getMyCurrentDepartment(String roleName, String userId) {
		List<UserDepartmentRole> userList = userDepartmentRoleRepo.findAllByUserIdAndIsActiveAndIsBlocked(userId, true,
				false);
		if (DataValidator.isCollectionEmpty(userList)) {
			throw new BadRequestException(NO_RECORDS_EXIST_FOR_USER_ID + userId);
		}
		List<Integer> deptIds = userList.stream().map(UserDepartmentRole::getDeptId).collect(Collectors.toList());
		logger.info(LIST_OF_USER_RECORDS + userList.size() + DEPT_IDS + deptIds.toString());

		Iterable<Department> deptList = deptRepo.findAllById(deptIds);
		Department myDept = null;
		Map<Integer, Department> deptMap = new HashMap<>();
		if (!DataValidator.isCollectionEmpty(deptList)) {
			for (Department dept : deptList) {
				deptMap.put(dept.getDeptId(), dept);
			}
		}

		for (UserDepartmentRole user : userList) {
			Iterable<Role> roles = roleRepo.findAllById(Arrays.asList(user.getRoleIds()));
			for (Role r : roles) {
				if (r.getRoleName().equals(roleName)) {
					if (myDept != null) {
						throw new BadRequestException("More than one Department is available with Role: " + roleName);
					} else {
						myDept = deptMap.get(user.getDeptId());
					}
				}
			}
		}
		return myDept;
	}

	private boolean hasCBPRole(Department dept, List<UserDepartmentRole> userDeptRoleList) {
		for (UserDepartmentRole userDeptRole : userDeptRoleList) {
			if (userDeptRole.getDeptId() != null && userDeptRole.getDeptId().equals(dept.getDeptId())) {
				Iterable<Role> userRoles = roleRepo.findAllById(Arrays.asList(userDeptRole.getRoleIds()));
				for (Role r : userRoles) {
					if (PortalConstants.CBP_ROLES.contains(r.getRoleName())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private Department getMyActiveDepartment(String strDeptType, String userId) {
		List<UserDepartmentRole> userList = userDepartmentRoleRepo.findAllByUserIdAndIsActiveAndIsBlocked(userId, true,
				false);
		if (DataValidator.isCollectionEmpty(userList)) {
			throw new BadRequestException(NO_RECORDS_EXIST_FOR_USER_ID + userId);
		}
		List<Integer> deptIds = userList.stream().map(UserDepartmentRole::getDeptId).collect(Collectors.toList());
		logger.info(LIST_OF_USER_RECORDS + userList.size() + DEPT_IDS + deptIds.toString());

		Iterable<Department> deptList = deptRepo.findAllById(deptIds);
		Department myDept = null;

		for (Department dept : deptList) {
			Iterable<DepartmentType> deptTypeList = deptTypeRepo.findAllById(Arrays.asList(dept.getDeptTypeIds()));
			for (DepartmentType deptType : deptTypeList) {
				if (deptType.getDeptType().equalsIgnoreCase(strDeptType)) {
					if (myDept != null) {
						throw new BadRequestException(
								"More than one Department is available for DeptType: " + strDeptType);
					} else {
						myDept = dept;
					}
				}
			}
		}
		return myDept;
	}

	@Override
	public DepartmentInfo getMyDepartment(String userId, String rootOrg) {
		return getMyDepartment("MDO", userId, true, rootOrg);
	}

	@Override
	public List<Department> getDepartmentsByUserId(String userId) {
		return Collections.emptyList();
	}

	@Override
	public DepartmentInfo addDepartment(String authUserToken, String userId, String userRoleName,
			DepartmentInfo deptInfo, String rootOrg) {
		validateDepartmentInfo(deptInfo);
		if (deptInfo.getDeptTypeIds() == null) {
			validateDepartmentTypeInfo(deptInfo.getDeptTypeInfos());

			List<Integer> deptTypeIds = new ArrayList<>();
			// We need to make sure this DeptType & subDeptType exist.
			for (DeptTypeInfo deptTypeInfo : deptInfo.getDeptTypeInfos()) {
				DepartmentType dType = deptTypeRepo.findByDeptTypeAndDeptSubType(deptTypeInfo.getDeptType(),
						deptTypeInfo.getDeptSubType());
				if (dType == null) {
					DepartmentType deptType = new DepartmentType();
					deptType.setDeptType(deptTypeInfo.getDeptType());
					deptType.setDeptSubType(deptTypeInfo.getDeptSubType());
					deptType.setDescription(deptTypeInfo.getDescription());
					dType = deptTypeRepo.save(deptType);
				}
				deptTypeIds.add(dType.getId());
			}
			deptInfo.setDeptTypeIds(deptTypeIds.toArray(new Integer[deptTypeIds.size()]));
		} else {
			validateDepartmentTypeInfo(deptInfo.getDeptTypeIds());
		}

		// Department is Valid -- add this Department
		Department dept = Department.clone(deptInfo);
		dept.setCreationDate(java.time.Instant.now().toEpochMilli());
		dept.setCreatedBy(userId);
		dept = deptRepo.save(dept);

		Iterator<Role> roles = roleRepo.findAll().iterator();
		List<Integer> roleIds = new ArrayList<>();
		List<Role> roleList = new ArrayList<>();
		roles.forEachRemaining(roleList::add);
		Map<String, Role> roleMap = roleList.stream().collect(Collectors.toMap(Role::getRoleName, role -> role));
		if (roleMap.containsKey(userRoleName)) {
			roleIds.add(roleMap.get(userRoleName).getId());
		}
		if (!DataValidator.isCollectionEmpty(deptInfo.getAdminUserList())) { // We have Few admin Users to assign to
			for (UserDepartmentRole userDeptRole : deptInfo.getAdminUserList()) {
				try {
					userDeptRole.setDeptId(dept.getDeptId());
					userDeptRole.setRoleIds(roleIds.toArray(new Integer[roleIds.size()]));
					userDeptRole.setIsActive(true);
					userDeptRole.setIsBlocked(false);
					userDeptRole = userDepartmentRoleRepo.save(userDeptRole);
					createUserDepartmentRoleAudit(userDeptRole, userId);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
		HashMap<String, Object> orgObj = new HashMap<>();
		orgObj.put("userToken", authUserToken);
		orgObj.put("orgName", dept.getDeptName());
		producer.push(serverConfig.getOrgCreationKafkaTopic(), orgObj);
		return enrichDepartmentInfo(dept, false, true, rootOrg);
	}

	@Override
	public DepartmentInfo updateDepartment(DepartmentInfo deptInfo, String rootOrg) {
		Optional<Department> department = deptRepo.findByIdAndIsDeleted(deptInfo.getId(), false);
		if (department.isPresent()) {
			Department existingDept = department.get();
			logger.info("Updating Department record -> " + existingDept);
			existingDept.setDescription(deptInfo.getDescription());
			existingDept.setHeadquarters(deptInfo.getHeadquarters());
			existingDept.setLogo(deptInfo.getLogo());
			existingDept.setDeptName(deptInfo.getDeptName());
			existingDept.setDeptTypeIds(deptInfo.getDeptTypeIds());
			if (deptInfo.getSourceId() != null) {
				existingDept.setSourceId(deptInfo.getSourceId());
			}
			logger.info("Updating Department with existing record -> " + existingDept);

			existingDept = deptRepo.save(existingDept);
			return enrichDepartmentInfo(existingDept, false, true, rootOrg);
		} else {
			throw new BadRequestException("Failed to find Department for Id: " + deptInfo.getId());
		}
	}

	@Override
	public UserDepartmentInfo addUserRoleInDepartment(UserDepartmentRole userDeptRole, String wid, String rootOrg,
			String org) {
		validateUserDepartmentRole(userDeptRole, true, rootOrg);
		UserDepartmentRole existingRecord = userDepartmentRoleRepo.findByUserIdAndDeptId(userDeptRole.getUserId(),
				userDeptRole.getDeptId());
		if (existingRecord != null) {
			throw new BadRequestException("Record already exist for UserId: '" + userDeptRole.getUserId()
					+ ", RoleName: " + userDeptRole.getRoles());
		}

		existingRecord = userDeptRole;
		Iterator<Role> roles = roleRepo.findAll().iterator();
		Set<Integer> roleIds = new HashSet<>();

		while (roles.hasNext()) {
			Role role = roles.next();
			for (String r : userDeptRole.getRoles()) {
				if (role.getRoleName().equalsIgnoreCase(r)) {
					roleIds.add(role.getId());
				}
			}
		}

		int prevDeptId = 0;
		existingRecord.setIsActive(userDeptRole.getIsActive());
		existingRecord.setIsBlocked(userDeptRole.getIsBlocked());
		prevDeptId = existingRecord.getDeptId();
		existingRecord.setDeptId(userDeptRole.getDeptId());
		existingRecord.setRoleIds(roleIds.stream().collect(Collectors.toList()).toArray(new Integer[roleIds.size()]));

		UserDepartmentInfo userDeptInfo = enrichUserDepartment(userDepartmentRoleRepo.save(existingRecord), rootOrg);
		createUserDepartmentRoleAudit(existingRecord, wid);
		// Update the WF history and OpenSaber profile for department details
		HashMap<String, Object> request = new HashMap<>();
		request.put("userId", userDeptInfo.getUserId());
		request.put("applicationId", userDeptInfo.getUserId());
		request.put("actorUserId", wid);
		request.put("serviceName", "profile");
		request.put("comment", "Updating Department Details.");
		ArrayList<HashMap<String, Object>> fieldValues = new ArrayList<>();
		HashMap<String, Object> fieldValue = new HashMap<>();
		fieldValue.put("fieldKey", "employmentDetails");

		// Try to get existing dept if available
		String prevDeptName = "";
		if (prevDeptId != 0) {
			Optional<Department> optionalPrevDept = deptRepo.findByIdAndIsDeleted(prevDeptId, false);
			if (optionalPrevDept.isPresent()) {
				prevDeptName = optionalPrevDept.get().getDeptName();
			}
		}

		HashMap<String, Object> fromValue = new HashMap<>();
		fromValue.put(DEPARTMENT_NAME, prevDeptName);
		fieldValue.put("fromValue", fromValue);
		HashMap<String, Object> toValue = new HashMap<>();
		toValue.put(DEPARTMENT_NAME, userDeptInfo.getDeptInfo().getDeptName());
		fieldValue.put("toValue", toValue);
		fieldValues.add(fieldValue);
		request.put("updateFieldValues", fieldValues);

		HttpHeaders headers = new HttpHeaders();
		headers.set(ROOT_ORG_CONST, rootOrg);
		headers.set(ORG_CONST, org);
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		restTemplate.postForObject(serverConfig.getWfServiceHost() + serverConfig.getWfServicePath(), entity,
				Map.class);
		return userDeptInfo;
	}

	public UserDepartmentInfo updateUserRoleInDepartment(UserDepartmentRole userDeptRole, String wid, String rootOrg,
			String org) {
		validateUserDepartmentRole(userDeptRole, false, rootOrg);
		UserDepartmentRole existingRecord = userDepartmentRoleRepo.findByUserIdAndDeptId(userDeptRole.getUserId(),
				userDeptRole.getDeptId());
		if (existingRecord == null) {
			throw new BadRequestException("Failed to identify User details for UserId: " + userDeptRole.getUserId());
		}
		Iterator<Role> roles = roleRepo.findAll().iterator();
		List<Role> roleList = new ArrayList<>();
		roles.forEachRemaining(roleList::add);
		Set<Integer> roleIds = new HashSet<>();
		Map<String, Role> roleMap = roleList.stream().collect(Collectors.toMap(Role::getRoleName, role -> role));
		for (String r : userDeptRole.getRoles()) {
			if (roleMap.containsKey(r)) {
				roleIds.add(roleMap.get(r).getId());
			}
		}

		int prevDeptId = 0;
		existingRecord.setIsActive(userDeptRole.getIsActive());
		existingRecord.setIsBlocked(userDeptRole.getIsBlocked());
		prevDeptId = existingRecord.getDeptId();
		existingRecord.setDeptId(userDeptRole.getDeptId());
		existingRecord.setRoleIds(roleIds.stream().collect(Collectors.toList()).toArray(new Integer[roleIds.size()]));

		UserDepartmentInfo userDeptInfo = enrichUserDepartment(userDepartmentRoleRepo.save(existingRecord), rootOrg);
		createUserDepartmentRoleAudit(existingRecord, wid);
		// Update the WF history and OpenSaber profile for department details
		HashMap<String, Object> request = new HashMap<>();
		request.put("userId", userDeptInfo.getUserId());
		request.put("applicationId", userDeptInfo.getUserId());
		request.put("actorUserId", wid);
		request.put("serviceName", "profile");
		request.put("comment", "Updating Department Details.");
		ArrayList<HashMap<String, Object>> fieldValues = new ArrayList<>();
		HashMap<String, Object> fieldValue = new HashMap<>();
		fieldValue.put("fieldKey", "employmentDetails");

		// Try to get existing dept if available
		String prevDeptName = "";
		if (prevDeptId != 0) {
			Optional<Department> optionalPrevDept = deptRepo.findByIdAndIsDeleted(prevDeptId, false);
			if (optionalPrevDept.isPresent()) {
				prevDeptName = optionalPrevDept.get().getDeptName();
			}
		}
		HashMap<String, Object> fromValue = new HashMap<>();
		fromValue.put(DEPARTMENT_NAME, prevDeptName);
		fieldValue.put("fromValue", fromValue);
		HashMap<String, Object> toValue = new HashMap<>();
		toValue.put(DEPARTMENT_NAME, userDeptInfo.getDeptInfo().getDeptName());
		fieldValue.put("toValue", toValue);
		fieldValues.add(fieldValue);
		request.put("updateFieldValues", fieldValues);

		HttpHeaders headers = new HttpHeaders();
		headers.set(ROOT_ORG_CONST, rootOrg);
		headers.set(ORG_CONST, org);
		HttpEntity<Object> entity = new HttpEntity<>(request, headers);

		restTemplate.postForObject(serverConfig.getWfServiceHost() + serverConfig.getWfServicePath(), entity,
				Map.class);

		return userDeptInfo;
	}

	@Override
	public Boolean checkAdminPrivilage(Integer deptId, String userId, String rootOrg) {
		UserDepartmentInfo userDeptInfoList = enrichUserDepartment(
				userDepartmentRoleRepo.findByUserIdAndDeptId(userId, deptId), rootOrg);
		Iterator<Role> roles = userDeptInfoList.getRoleInfo().iterator();
		while (roles.hasNext()) {
			Role r = roles.next();
			if ("MDO ADMIN".equalsIgnoreCase(r.getRoleName())) {
				return true;
			}
		}
		// If User is not ADMIN of given dept, then check for SPV admin.
		return checkMdoAdminPrivilage("SPV", userId);
	}

	@Override
	public Boolean checkMdoAdminPrivilage(String deptKey, String userId) {
		boolean retValue = false;
		try {
			logger.info("checkMdoAdminPrivilage... userId: " + userId + ", deptKey: " + deptKey);
			// Find MDO Departments
			List<DepartmentType> deptTypes = deptTypeRepo.findByDeptTypeIgnoreCase(deptKey);

			List<Integer> deptTypeIds = new ArrayList<>();
			if (!DataValidator.isCollectionEmpty(deptTypes)) {
				deptTypeIds = deptTypes.stream().map(DepartmentType::getId).collect(Collectors.toList());
			}

			List<Department> depts = deptRepo.findAllByIdIn(deptTypeIds);
			List<Integer> deptIds = new ArrayList<>();
			if (!DataValidator.isCollectionEmpty(depts)) {
				deptIds = depts.stream().map(Department::getDeptId).collect(Collectors.toList());
			}
			List<UserDepartmentRole> userDeptRoles = userDepartmentRoleRepo.findAllByUserIdAndDeptId(userId, deptIds);
			for (UserDepartmentRole userDeptRole : userDeptRoles) {
				if (userDeptRole.getUserId().equalsIgnoreCase(userId)) {
					retValue = userDeptRole.getIsActive() && !userDeptRole.getIsBlocked();
					break;
				}
			}

			logger.info("checkMdoAdminPrivilage... returns : " + retValue);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
		return retValue;
	}

	@Override
	public DepartmentInfo getMyDepartmentDetails(String userId, boolean isUserInfoRequired) {
		return null;
	}

	@Override
	public boolean isAdmin(String strDeptType, String roleName, String userId) {
		boolean retValue = false;
		StringBuilder str = new StringBuilder("isAdmin");
		str.append("strDeptType: ").append(strDeptType).append(", roleName: ").append(roleName);
		str.append(", userId: ").append(userId).append(", roleName: ").append(roleName).append(System.lineSeparator());
		List<UserDepartmentRole> userDeptRoleList = userDepartmentRoleRepo
				.findAllByUserIdAndIsActiveAndIsBlocked(userId, true, false);
		if (!DataValidator.isCollectionEmpty(userDeptRoleList)) {
			for (UserDepartmentRole userDeptRole : userDeptRoleList) {
				if (!Boolean.TRUE.equals(userDeptRole.getIsActive())
						|| Boolean.TRUE.equals(userDeptRole.getIsBlocked())) {
					continue;
				}
				str.append("Found userDepartmentRole entry id= ").append(userDeptRole.getId())
						.append(System.lineSeparator());
				// Get Roles
				Iterable<Role> roles = roleRepo.findAllById(Arrays.asList(userDeptRole.getRoleIds()));
				if (!DataValidator.isCollectionEmpty(roles)) {
					for (Role role : roles) {
						if (role.getRoleName().contains(roleName)) {
							// Just check this department type is equal to given roleName
							Optional<Department> dept = deptRepo.findByIdAndIsDeleted(userDeptRole.getDeptId(), false);

							if (dept.isPresent()) {
								str.append("Found Department with Id: ").append(dept.get().getDeptId())
										.append(System.lineSeparator());
								Iterable<DepartmentType> deptTypeList = deptTypeRepo
										.findAllById(Arrays.asList(dept.get().getDeptTypeIds()));
								if (!DataValidator.isCollectionEmpty(deptTypeList)) {
									for (DepartmentType deptType : deptTypeList) {
										if (deptType.getDeptType().equalsIgnoreCase(strDeptType)) {
											// We have found the expected Department.
											retValue = true;
											str.append("Found that department has Type: ").append(strDeptType);
											break;
										}
									}
								}
							}
						}
						if (retValue) {
							break;
						}
					}
				}
			}
		}
		str.append("Return value: ").append(retValue);
		logger.info(str.toString());
		return retValue;
	}

	@Override
	public List<SearchUserInfo> searchUserForRole(Integer deptId, String roleName, String userName) {
		return Collections.emptyList();
	}

	private DepartmentInfo enrichDepartmentInfo(Integer deptId, boolean isUserInfoRequired, boolean enrichData,
			String rootOrg) {
		Optional<Department> dept = deptRepo.findByIdAndIsDeleted(deptId, false);
		if (dept.isPresent()) {
			return enrichDepartmentInfo(dept.get(), isUserInfoRequired, enrichData, rootOrg);
		}
		return null;
	}

	private DepartmentInfo enrichDepartmentInfo(Department dept, boolean isUserInfoRequired, boolean enrichData,
			String rootOrg) {
		DepartmentInfo deptInfo = null;
		if (dept != null) {
			deptInfo = new DepartmentInfo();
			deptInfo.setDeptName(dept.getDeptName());
			deptInfo.setDescription(dept.getDescription());
			deptInfo.setId(dept.getDeptId());
			deptInfo.setRootOrg(dept.getRootOrg());
			deptInfo.setDeptTypeIds(dept.getDeptTypeIds());
			deptInfo.setHeadquarters(dept.getHeadquarters());
			deptInfo.setLogo(dept.getLogo());
			deptInfo.setCreationDate(dept.getCreationDate());
			deptInfo.setCreatedBy(dept.getCreatedBy());
			if (dept.getSourceId() != null) {
				deptInfo.setSourceId(dept.getSourceId());
			}
			// Get Dept Type Information
			deptInfo.setDeptTypeInfos(enrichDepartmentTypeInfo(dept.getDeptTypeIds()));

			// Get Number of Users in Department
			if (enrichData) {
				List<UserDepartmentRole> userDeptList = userDepartmentRoleRepo.findByDeptId(deptInfo.getId());
				deptInfo.setNoOfUsers(userDeptList == null ? 0 : userDeptList.size());

				// Get Role Informations
				List<Role> roleList = getDepartmentRoles(Arrays.asList(deptInfo.getDeptTypeIds()));
				Collections.sort(roleList, Comparator.nullsFirst(Comparator.comparing(Role::getRoleName)));
				if (!isUserInfoRequired && !CollectionUtils.isEmpty(roleList)) {
					List<Role> newRoleList = new ArrayList<>();
					for (Role role : roleList) {
						Role assignRole = new Role();
						assignRole.setDescription(role.getDescription());
						assignRole.setId(role.getId());
						assignRole.setRoleName(role.getRoleName());
						assignRole.setNoOfUsers(userDepartmentRoleRepo.getTotalUserCountOnRoleIdAndDeptId(role.getId(),
								deptInfo.getId()));
						newRoleList.add(assignRole);
					}
					deptInfo.setRolesInfo(newRoleList);
				} else {
					deptInfo.setRolesInfo(roleList);
				}
				Map<Integer, Role> deptRoleMap = deptInfo.getRolesInfo().stream()
						.collect(Collectors.toMap(Role::getId, roleInfo -> roleInfo));

				if (isUserInfoRequired && userDeptList != null && !CollectionUtils.isEmpty(userDeptList)) {
					Set<String> userIdSet = userDeptList.stream().map(UserDepartmentRole::getUserId)
							.collect(Collectors.toSet());
					List<String> userIds = userIdSet.stream().collect(Collectors.toList());
					Map<String, Object> result = userUtilService.getUsersDataFromUserIds(rootOrg, userIds,
							new ArrayList<>(Arrays.asList(Constants.FIRST_NAME, Constants.LAST_NAME, Constants.EMAIL,
									Constants.DEPARTMENT_NAME)));
					logger.info("enrichDepartmentInfo UserIds -> " + userIds.toString() + ", fetched Information -> "
							+ result.size());
					for (UserDepartmentRole userDeptRole : userDeptList) {
						PortalUserInfo pUserInfo = new PortalUserInfo();
						pUserInfo.setUserId(userDeptRole.getUserId());
						pUserInfo.setActive(userDeptRole.getIsActive());
						pUserInfo.setBlocked(userDeptRole.getIsBlocked());
						// Fetch User Data
						if (result.containsKey(userDeptRole.getUserId())) {
							OpenSaberApiUserProfile userProfile = (OpenSaberApiUserProfile) result
									.get(userDeptRole.getUserId());
							pUserInfo.setEmailId(userProfile.getPersonalDetails().getPrimaryEmail());
							pUserInfo.setFirstName(userProfile.getPersonalDetails().getFirstname());
							pUserInfo.setLastName(userProfile.getPersonalDetails().getSurname());

							// Assign RoleInfo
							List<Role> userRoleInfo = new ArrayList<>();
							for (Integer roleId : userDeptRole.getRoleIds()) {
								Role r = deptRoleMap.get(roleId);
								userRoleInfo.add(r);
								r.incrementUserCount();
							}
							pUserInfo.setRoleInfo(userRoleInfo);

							if (Boolean.TRUE.equals(userDeptRole.getIsBlocked())) {
								deptInfo.addBlockedUser(pUserInfo);
							} else if (Boolean.TRUE.equals(userDeptRole.getIsActive())) {
								deptInfo.addActiveUser(pUserInfo);
							} else {
								deptInfo.addInActiveUser(pUserInfo);
							}
						} else {
							logger.error(
									new Exception("UserRegistry not found for UserId --> " + userDeptRole.getUserId()));
						}
					}

					logger.info("enrichDepartmentInfo: " + deptInfo);
					List<PortalUserInfo> portalActiveUsers = deptInfo.getActive_users();
					if (!CollectionUtils.isEmpty(portalActiveUsers)) {
						Collections.sort(portalActiveUsers,
								Comparator.nullsFirst(Comparator.comparing(PortalUserInfo::getFirstName)));
						deptInfo.setActive_users(portalActiveUsers);
					}
					List<PortalUserInfo> portalBlockedUsers = deptInfo.getBlocked_users();
					if (!CollectionUtils.isEmpty(portalBlockedUsers)) {
						Collections.sort(portalBlockedUsers,
								Comparator.nullsFirst(Comparator.comparing(PortalUserInfo::getFirstName)));
						deptInfo.setBlocked_users(portalBlockedUsers);
					}
					List<PortalUserInfo> portalInActiveUsers = deptInfo.getInActive_users();
					if (!CollectionUtils.isEmpty(portalInActiveUsers)) {
						Collections.sort(portalInActiveUsers,
								Comparator.nullsFirst(Comparator.comparing(PortalUserInfo::getFirstName)));
						deptInfo.setInActive_users(portalInActiveUsers);
					}
				}
			}

			logger.info("enrichDepartmentInfo: " + deptInfo);
			return deptInfo;
		}
		return null;
	}

	private List<DepartmentInfo> enrichDepartmentInfo(List<Department> depts, boolean enrichData, String rootOrg) {
		List<DepartmentInfo> deptInfoList = new ArrayList<>();
		for (Department dept : depts) {
			try {
				deptInfoList.add(enrichDepartmentInfo(dept, false, enrichData, rootOrg));
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return deptInfoList;
	}

	private List<DeptTypeInfo> enrichDepartmentTypeInfo(Integer[] deptTypeId) {
		List<DeptTypeInfo> deptTypeInfoList = new ArrayList<>();
		Iterable<DepartmentType> dTypeList = deptTypeRepo.findAllById(Arrays.asList(deptTypeId));
		if (!DataValidator.isCollectionEmpty(dTypeList)) {
			for (DepartmentType dType : dTypeList) {
				DeptTypeInfo deptTypeInfo = new DeptTypeInfo();
				deptTypeInfo.setId(dType.getId());
				deptTypeInfo.setDeptType(dType.getDeptType());
				deptTypeInfo.setDeptSubType(dType.getDeptSubType());
				deptTypeInfo.setDescription(dType.getDescription());
				deptTypeInfoList.add(deptTypeInfo);
			}
		}
		return deptTypeInfoList;
	}

	private UserDepartmentInfo enrichUserDepartment(UserDepartmentRole userDeptRole, String rootOrg) {
		UserDepartmentInfo deptInfo = new UserDepartmentInfo();
		deptInfo.setUserId(userDeptRole.getUserId());
		deptInfo.setIsActive(userDeptRole.getIsActive());
		deptInfo.setIsBlocked(userDeptRole.getIsBlocked());

		// Enrich Department Info
		deptInfo.setDeptInfo(enrichDepartmentInfo(userDeptRole.getDeptId(), false, false, rootOrg));

		// Enrich Department Role Info
		deptInfo.setRoleInfo(roleRepo.findAllById(Arrays.asList(userDeptRole.getRoleIds())));

		return deptInfo;
	}

	private void validateDepartmentInfo(DepartmentInfo deptInfo) {
		if (deptRepo.existsByDeptNameIgnoreCase(deptInfo.getDeptName())) {
			throw new BadRequestException(
					"Failed to create Department. Given deptName: '" + deptInfo.getDeptName() + "' already exists");
		}
		boolean isValid = !DataValidator.isStringEmpty(deptInfo.getDeptName())
				&& !DataValidator.isStringEmpty(deptInfo.getRootOrg());
		if (!isValid) {
			throw new BadRequestException(
					"Failed to create Department. Given Department is null OR RootOrg/DeptName is null");
		}
	}

	private void validateDepartmentTypeInfo(List<DeptTypeInfo> deptTypeInfoList) {
		if (!DataValidator.isCollectionEmpty(deptTypeInfoList)) {
			for (DeptTypeInfo deptTypeInfo : deptTypeInfoList) {
				boolean isValid = !DataValidator.isStringEmpty(deptTypeInfo.getDeptType())
						&& !DataValidator.isStringEmpty(deptTypeInfo.getDeptSubType());
				if (!isValid) {
					throw new BadRequestException(
							"Failed to create Department. Given deptType or deptSubType is Empty");
				}
			}
		}
	}

	private void validateDepartmentTypeInfo(Integer[] deptTypeIds) {
		for (Integer deptTypeId : deptTypeIds) {
			Optional<DepartmentType> dType = deptTypeRepo.findById(deptTypeId);
			if (!dType.isPresent()) {
				throw new BadRequestException(
						"Failed to create Department. Given deptTypeId: '" + deptTypeId + "' doesn't exist");
			}
		}
	}

	private void validateUserDepartmentRole(UserDepartmentRole userDeptRole, boolean isAddReq, String rootOrg) {
		// Check User exists
		if (!userUtilService.validateUser(rootOrg, userDeptRole.getUserId())) {
			throw new BadRequestException("Invalid UserId.");
		}
		// Check department exist
		Optional<Department> dept = deptRepo.findByIdAndIsDeleted(userDeptRole.getDeptId(), false);
		if (!dept.isPresent()) {
			throw new BadRequestException("Invalid Department");
		}
		// Check Role exist
		if (!DataValidator.isCollectionEmpty(userDeptRole.getRoles())) {
			boolean isCbpRoleGiven = false;
			List<Role> roles = roleRepo.findAllByRoleNameIn(userDeptRole.getRoles());
			for (Role r : roles) {
				if (PortalConstants.CBP_ROLES.contains(r.getRoleName())) {
					isCbpRoleGiven = true;
					break;
				}
			}
			Map<Integer, Role> givenRoleMap = roles.stream()
					.collect(Collectors.toMap(Role::getId, roleInfo -> roleInfo));
			if (DataValidator.isCollectionEmpty(roles) || roles.size() != userDeptRole.getRoles().size()) {
				throw new BadRequestException("Invalid Role Names Provided");
			}
			List<Role> rolesAvailableInDept = getDepartmentRoles(Arrays.asList(dept.get().getDeptTypeIds()));
			Set<String> availableRoleNames = new HashSet<>();
			if (!CollectionUtils.isEmpty(rolesAvailableInDept)) {
				availableRoleNames = rolesAvailableInDept.stream().map(Role::getRoleName).collect(Collectors.toSet());
			}
			for (String roleName : userDeptRole.getRoles()) {
				if (!availableRoleNames.contains(roleName)) {
					throw new BadRequestException("Invalid Role Name provided for the Department");
				}
			}
			if (isAddReq && !serverConfig.isUserMultiMapDeptEnabled()) {
				// Check this user has the same Role in another department.
				List<UserDepartmentRole> existingUserDepts = userDepartmentRoleRepo
						.findAllByUserIdAndIsActiveAndIsBlocked(userDeptRole.getUserId(), true, false);
				if (!DataValidator.isCollectionEmpty(existingUserDepts)) {
					for (UserDepartmentRole uDeptRole : existingUserDepts) {
						if (isCbpRoleGiven) {
							// Just check any CBP role is already assigned
							Iterable<Role> existingUserDeptRoles = roleRepo
									.findAllById(Arrays.asList(uDeptRole.getRoleIds()));
							for (Role r : existingUserDeptRoles) {
								if (PortalConstants.CBP_ROLES.contains(r.getRoleName())) {
									throw new BadRequestException(
											"User is already assigned with a CBP Role in another Department.");
								}
							}
						}
						for (Integer i : uDeptRole.getRoleIds()) {
							Role r = givenRoleMap.get(i);
							if (r != null && !r.getRoleName().equalsIgnoreCase("MEMBER")) {
								throw new BadRequestException(
										"User is already assigned with given Role in another Department.");
							}
						}
					}
				}
			}
		} else {
			throw new BadRequestException("Roles cannot be empty.");
		}
	}

	private List<Role> getDepartmentRoles(List<Integer> deptTypeIdList) {
		Iterable<DepartmentType> deptTypeList = deptTypeRepo.findAllById(deptTypeIdList);
		if (DataValidator.isCollectionEmpty(deptTypeList)) {
			return Collections.emptyList();
		}
		Set<String> deptTypeNames = new HashSet<>();
		deptTypeNames.add("COMMON");
		for (DepartmentType deptType : deptTypeList) {
			deptTypeNames.add(deptType.getDeptType());
		}

		Iterable<DepartmentRole> deptRoleList = deptRoleRepo
				.findAllByDeptTypeIn(deptTypeNames.stream().collect(Collectors.toList()));
		Set<Role> roleList = new HashSet<>();
		if (DataValidator.isCollectionEmpty(deptRoleList)) {
			return Collections.emptyList();
		}

		for (DepartmentRole deptRole : deptRoleList) {
			Iterable<Role> existingRoles = roleRepo.findAllById(Arrays.asList(deptRole.getRoleIds()));
			for (Role r : existingRoles) {
				roleList.add(r);
			}
		}
		return roleList.stream().collect(Collectors.toList());
	}

	@Override
	public boolean validateCBPUserLogin(String userId) {
		List<UserDepartmentRole> userDeptRoleList = userDepartmentRoleRepo
				.findAllByUserIdAndIsActiveAndIsBlocked(userId, true, false);
		DepartmentRole cbpRole = deptRoleRepo.findByDeptTypeIgnoreCase(PortalConstants.CBP_DEPT_TYPE);
		Set<Integer> cbpRoleIds = new HashSet<>();
		if (cbpRole != null) {
			cbpRoleIds.addAll(Arrays.asList(cbpRole.getRoleIds()));
		}
		if (!DataValidator.isCollectionEmpty(userDeptRoleList)) {
			for (UserDepartmentRole userDeptRole : userDeptRoleList) {
				if (!Boolean.TRUE.equals(userDeptRole.getIsActive())
						|| Boolean.TRUE.equals(userDeptRole.getIsBlocked())) {
					continue;
				}
				// Just check this department type is "SPV"
				Optional<Department> dept = deptRepo.findByIdAndIsDeleted(userDeptRole.getDeptId(), false);
				if (dept.isPresent()) {
					Iterable<DepartmentType> deptTypeList = deptTypeRepo
							.findAllById(Arrays.asList(dept.get().getDeptTypeIds()));
					if (!DataValidator.isCollectionEmpty(deptTypeList)) {
						for (DepartmentType deptType : deptTypeList) {
							if (deptType.getDeptType().equalsIgnoreCase(PortalConstants.CBP_DEPT_TYPE)) {
								// We have found the expected Department.
								// Let's check user has at least one Role.
								for (Integer uRoleId : userDeptRole.getRoleIds()) {
									if (cbpRoleIds.contains(uRoleId)) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean validateUserLogin(String userId, List<String> roles, String departmentType) {
		List<Role> roleList = roleRepo.findAllByRoleNameIn(roles);
		Set<Integer> fracRoleIds = new HashSet<>();
		for (Role r : roleList) {
			fracRoleIds.add(r.getId());
		}

		List<UserDepartmentRole> userDeptRoleList = userDepartmentRoleRepo
				.findAllByUserIdAndIsActiveAndIsBlocked(userId, true, false);
		if (!DataValidator.isCollectionEmpty(userDeptRoleList)) {
			for (UserDepartmentRole userDeptRole : userDeptRoleList) {
				if (!Boolean.TRUE.equals(userDeptRole.getIsActive())
						|| Boolean.TRUE.equals(userDeptRole.getIsBlocked())) {
					continue;
				}
				Optional<Department> dept = deptRepo.findByIdAndIsDeleted(userDeptRole.getDeptId(), false);
				if (dept.isPresent()) {
					Iterable<DepartmentType> deptTypeList = deptTypeRepo
							.findAllById(Arrays.asList(dept.get().getDeptTypeIds()));
					if (!DataValidator.isCollectionEmpty(deptTypeList)) {
						for (DepartmentType deptType : deptTypeList) {
							if (deptType.getDeptType().equalsIgnoreCase(departmentType)) {
								// We have found the expected Department.
								// Let's check user has at least one Role.
								for (Integer uRoleId : userDeptRole.getRoleIds()) {
									if (fracRoleIds.contains(uRoleId)) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean validateUserLoginForDepartment(String userId, String departmentType) {
		List<UserDepartmentRole> userDeptRoleList = userDepartmentRoleRepo
				.findAllByUserIdAndIsActiveAndIsBlocked(userId, true, false);
		DepartmentRole departmentRole = deptRoleRepo.findByDeptTypeIgnoreCase(departmentType);
		Set<Integer> departmentRoleIds = new HashSet<>();
		if (departmentRole != null) {
			departmentRoleIds.addAll(Arrays.asList(departmentRole.getRoleIds()));
		}
		if (!DataValidator.isCollectionEmpty(userDeptRoleList)) {
			for (UserDepartmentRole userDeptRole : userDeptRoleList) {
				Optional<Department> dept = deptRepo.findByIdAndIsDeleted(userDeptRole.getDeptId(), false);
				if (dept.isPresent()) {
					Iterable<DepartmentType> deptTypeList = deptTypeRepo
							.findAllById(Arrays.asList(dept.get().getDeptTypeIds()));
					if (!DataValidator.isCollectionEmpty(deptTypeList)) {
						for (DepartmentType deptType : deptTypeList) {
							if (deptType.getDeptType().equalsIgnoreCase(departmentType)) {
								for (Integer uRoleId : userDeptRole.getRoleIds()) {
									if (departmentRoleIds.contains(uRoleId)) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public Boolean isUserActive(String userId) {
		List<UserDepartmentRole> userDepartmentRole = userDepartmentRoleRepo
				.findAllByUserIdAndIsActiveAndIsBlocked(userId, true, false);
		return !CollectionUtils.isEmpty(userDepartmentRole);
	}

	/**
	 *
	 * @param userDepartmentRole
	 *            user department role object
	 * @param modifiedBy
	 *            modified by value
	 */
	private void createUserDepartmentRoleAudit(UserDepartmentRole userDepartmentRole, String modifiedBy) {
		try {
			logger.info("Triggered the audit event .....");
			UserDepartmentRoleAudit auditObject = new UserDepartmentRoleAudit(userDepartmentRole.getUserId(),
					userDepartmentRole.getDeptId(), userDepartmentRole.getRoleIds(), userDepartmentRole.getIsActive(),
					userDepartmentRole.getIsBlocked(), modifiedBy);
			producer.push(serverConfig.getUserRoleAuditTopic(), auditObject);
		} catch (Exception ex) {
			logger.info("Exception occurred while creating the user department audit!");
			logger.error(ex);
		}

	}
}
