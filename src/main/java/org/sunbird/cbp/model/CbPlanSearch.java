package org.sunbird.cbp.model;

import java.util.Map;

public class CbPlanSearch {
    private Map<String, Object> filters;
    Integer limit;

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
