package org.sunbird.workallocation.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.SearchCriteria;
import org.sunbird.workallocation.model.WorkAllocationDTO;

import static org.sunbird.workallocation.service.AllocationService.STATUS_LIST;

@Component
public class Validator {

	public void validateCriteria(SearchCriteria criteria) {
		if (StringUtils.isEmpty(criteria.getDepartmentName())) {
			throw new BadRequestException("Department name can not be empty!");
		}
		if (StringUtils.isEmpty(criteria.getStatus())) {
			throw new BadRequestException("Status can not be empty!");
		}
		if(!STATUS_LIST.contains(criteria.getStatus())){
			throw new BadRequestException("Trying to send wrong status!");
		}
	}

	public void validateWorkAllocationReq(WorkAllocationDTO workAllocation, String reqType) {
		if (reqType.equals("update") && StringUtils.isEmpty(workAllocation.getWaId())) {
			throw new BadRequestException("Wa Id can not be empty!");
		}
		if (reqType.equals("update") && StringUtils.isEmpty(workAllocation.getUserId())) {
			throw new BadRequestException("User Id can not be empty!");
		}
		if (StringUtils.isEmpty(workAllocation.getStatus())) {
			throw new BadRequestException("Status can not be empty!");
		}
		if (StringUtils.isEmpty(workAllocation.getDeptId())) {
			throw new BadRequestException("Department id can not be empty!");
		}
		if (StringUtils.isEmpty(workAllocation.getDeptName())) {
			throw new BadRequestException("Department name can not be empty!");
		}
	}
}
