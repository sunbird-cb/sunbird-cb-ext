package org.sunbird.karmapoints.model;

public class KarmaPointsRequest {

    private int limit;
    private String offset;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
}
