package org.sunbird.portal.department.service;

import java.util.List;
import java.util.Map;

import org.sunbird.portal.department.model.DeptPublicInfo;

public interface PortalService {

	List<String> getDeptNameList();

	List<DeptPublicInfo> getAllDept() throws Exception;

	DeptPublicInfo searchDept(String deptName) throws Exception;

	List<Map<String,String>> getDeptNameListByAdmin();
}
