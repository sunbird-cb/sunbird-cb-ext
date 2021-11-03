package org.sunbird.common.model;

import java.util.List;

public class SearchUserAPIResponse {
    private int count;
    private List<SearchUserAPIResponse> content;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<SearchUserAPIResponse> getContent() {
        return content;
    }

    public void setContent(List<SearchUserAPIResponse> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "SearchUserAPIResponse{" +
                "count=" + count +
                ", content=" + content +
                '}';
    }
}
