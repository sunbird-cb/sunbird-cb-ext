package org.sunbird.portal.department.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.sunbird.portal.department.dto.Department;

public interface DepartmentRepository extends CrudRepository<Department, Integer> {

	Optional<Department> findByIdAndIsDeleted(Integer id, boolean isDeleted);

	List<Department> findAll();

	List<Department> findAllByIsDeletedOrderByDeptNameAsc(boolean isDeleted);

	boolean existsByDeptNameIgnoreCase(String deptName);

	List<Department> findAllByIdIn(Iterable<Integer> deptTypeIds);

	Department findByDeptNameIgnoreCaseAndIsDeleted(String deptKey, boolean isDeleted);

//	@Query("select dept from departments where dept.dept_type_id in (select id from department_types where dept_type = ?0)")
//	List<Department> findDeptUsingType(String deptTypeKey);
}
