package org.sunbird.workallocation.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.workallocation.model.SearchCriteria;

@Component
public class Validator {

    public void validateCriteria(SearchCriteria criteria) {
        if (StringUtils.isEmpty(criteria.getDepartmentName())) {
            throw new BadRequestException("Department name can not be empty!");
        }
    }
}
