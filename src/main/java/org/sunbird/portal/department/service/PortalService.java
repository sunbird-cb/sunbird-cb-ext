package org.sunbird.portal.department.service;

import java.util.List;

import org.sunbird.portal.department.model.DeptPublicInfo;

public interface PortalService {

	List<String> getDeptNameList();

	List<DeptPublicInfo> getAllDept() throws Exception;

	DeptPublicInfo searchDept(String deptName) throws Exception;
}
