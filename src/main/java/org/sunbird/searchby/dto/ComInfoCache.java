package org.sunbird.searchby.dto;

import org.sunbird.searchby.model.CompetencyInfo;

import java.util.List;

public class ComInfoCache {
    public String keyName;
    public List<CompetencyInfo>  valueList;

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public List<CompetencyInfo> getValueList() {
        return valueList;
    }

    public void setValueList(List<CompetencyInfo> valueList) {
        this.valueList = valueList;
    }
}
