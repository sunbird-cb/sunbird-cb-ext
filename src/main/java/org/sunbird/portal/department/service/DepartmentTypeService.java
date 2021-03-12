package org.sunbird.portal.department.service;

import org.sunbird.portal.department.dto.DepartmentType;

import java.util.List;
import java.util.Map;

public interface DepartmentTypeService {

	Map<String, List<DepartmentType>> getAllDepartmentTypes();

	List<DepartmentType> getDepartmentByType(String deptType);

	DepartmentType getDepartmentTypeById(Integer id) throws Exception;

	Map<String, List<String>> getDepartmentTypeNames();
}
