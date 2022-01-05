package org.sunbird.portal.department.service;

import java.util.List;

import org.sunbird.portal.department.model.DeptPublicInfo;

public interface PortalService {

	List<DeptPublicInfo> getAllDept();

	List<String> getDeptNameList();

	DeptPublicInfo searchDept(String deptName);

}
