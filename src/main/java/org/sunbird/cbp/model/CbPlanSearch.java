package org.sunbird.cbp.model;

import java.util.Map;
import java.util.UUID;

public class CbPlanSearch {
    private Map<String, Object> filters;
    Integer limit;
    private UUID offset;

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

    public UUID getOffset() {
        return offset;
    }

    public void setOffset(UUID offset) {
        this.offset = offset;
    }

}
