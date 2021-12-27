package org.sunbird.portal.department.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.common.service.UserUtilityService;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.core.producer.Producer;
import org.sunbird.portal.department.model.DeptPublicInfo;

@Service
public class PortalServiceImpl implements PortalService {

	public static final String NO_RECORDS_EXIST_FOR_USER_ID = "No records exist for UserId: ";
	public static final String LIST_OF_USER_RECORDS = "List of User Records -> ";
	public static final String DEPT_IDS = ", DeptIds: ";
	public static final String DEPARTMENT_NAME = "departmentName";
	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	UserUtilityService userUtilService;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	Producer producer;

	@Override
	public List<String> getDeptNameList() {
		return Collections.emptyList();
	}

	@Override
	public List<DeptPublicInfo> getAllDept() {
		return Collections.emptyList();
	}

	@Override
	public DeptPublicInfo searchDept(String deptName) {
		return null;
	}

}