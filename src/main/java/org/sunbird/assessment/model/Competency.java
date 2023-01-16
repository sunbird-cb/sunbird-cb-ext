package org.sunbird.assessment.model;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"name",
"description",
"competencyType",
"competencyArea",
"source",
"selectedLevelId",
"selectedLevelLevel",
"selectedLevelName",
"selectedLevelDescription"
})
public class Competency {

@JsonProperty("id")
private String id;
@JsonProperty("name")
private String name;
@JsonProperty("description")
private String description;
@JsonProperty("competencyType")
private String competencyType;
@JsonProperty("competencyArea")
private String competencyArea;
@JsonProperty("source")
private String source;
@JsonProperty("selectedLevelId")
private String selectedLevelId;
@JsonProperty("selectedLevelLevel")
private String selectedLevelLevel;
@JsonProperty("selectedLevelName")
private String selectedLevelName;
@JsonProperty("selectedLevelDescription")
private String selectedLevelDescription;

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("description")
public String getDescription() {
return description;
}

@JsonProperty("description")
public void setDescription(String description) {
this.description = description;
}

@JsonProperty("competencyType")
public String getCompetencyType() {
return competencyType;
}

@JsonProperty("competencyType")
public void setCompetencyType(String competencyType) {
this.competencyType = competencyType;
}

@JsonProperty("competencyArea")
public String getCompetencyArea() {
return competencyArea;
}

@JsonProperty("competencyArea")
public void setCompetencyArea(String competencyArea) {
this.competencyArea = competencyArea;
}

@JsonProperty("source")
public String getSource() {
return source;
}

@JsonProperty("source")
public void setSource(String source) {
this.source = source;
}

@JsonProperty("selectedLevelId")
public String getSelectedLevelId() {
return selectedLevelId;
}

@JsonProperty("selectedLevelId")
public void setSelectedLevelId(String selectedLevelId) {
this.selectedLevelId = selectedLevelId;
}

@JsonProperty("selectedLevelLevel")
public String getSelectedLevelLevel() {
return selectedLevelLevel;
}

@JsonProperty("selectedLevelLevel")
public void setSelectedLevelLevel(String selectedLevelLevel) {
this.selectedLevelLevel = selectedLevelLevel;
}

@JsonProperty("selectedLevelName")
public String getSelectedLevelName() {
return selectedLevelName;
}

@JsonProperty("selectedLevelName")
public void setSelectedLevelName(String selectedLevelName) {
this.selectedLevelName = selectedLevelName;
}

@JsonProperty("selectedLevelDescription")
public String getSelectedLevelDescription() {
return selectedLevelDescription;
}

@JsonProperty("selectedLevelDescription")
public void setSelectedLevelDescription(String selectedLevelDescription) {
this.selectedLevelDescription = selectedLevelDescription;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Competency.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("id");
sb.append('=');
sb.append(((this.id == null)?"<null>":this.id));
sb.append(',');
sb.append("name");
sb.append('=');
sb.append(((this.name == null)?"<null>":this.name));
sb.append(',');
sb.append("description");
sb.append('=');
sb.append(((this.description == null)?"<null>":this.description));
sb.append(',');
sb.append("competencyType");
sb.append('=');
sb.append(((this.competencyType == null)?"<null>":this.competencyType));
sb.append(',');
sb.append("competencyArea");
sb.append('=');
sb.append(((this.competencyArea == null)?"<null>":this.competencyArea));
sb.append(',');
sb.append("source");
sb.append('=');
sb.append(((this.source == null)?"<null>":this.source));
sb.append(',');
sb.append("selectedLevelId");
sb.append('=');
sb.append(((this.selectedLevelId == null)?"<null>":this.selectedLevelId));
sb.append(',');
sb.append("selectedLevelLevel");
sb.append('=');
sb.append(((this.selectedLevelLevel == null)?"<null>":this.selectedLevelLevel));
sb.append(',');
sb.append("selectedLevelName");
sb.append('=');
sb.append(((this.selectedLevelName == null)?"<null>":this.selectedLevelName));
sb.append(',');
sb.append("selectedLevelDescription");
sb.append('=');
sb.append(((this.selectedLevelDescription == null)?"<null>":this.selectedLevelDescription));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}