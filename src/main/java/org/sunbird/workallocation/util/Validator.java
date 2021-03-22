package org.sunbird.workallocation.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.SearchCriteria;
import org.sunbird.workallocation.model.WorkAllocation;

@Component
public class Validator {

    public void validateCriteria(SearchCriteria criteria) {
        if (StringUtils.isEmpty(criteria.getDepartmentName())) {
            throw new BadRequestException("Department name can not be empty!");
        }
    }

    public void validateWorkAllocationReq(WorkAllocation workAllocation) {
        if (StringUtils.isEmpty(workAllocation.getUserId())) {
            throw new BadRequestException("User Id can not be empty!");
        }
        if (StringUtils.isEmpty(workAllocation.getDeptId())) {
            throw new BadRequestException("Department id can not be empty!");
        }
        if (StringUtils.isEmpty(workAllocation.getDeptName())) {
            throw new BadRequestException("Department name can not be empty!");
        }
    }
}
