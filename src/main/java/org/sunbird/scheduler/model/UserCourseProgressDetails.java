package org.sunbird.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCourseProgressDetails implements Serializable {

  private static final long serialVersionUID = 1L;
  private String email;

  @Override
  public String toString() {
    return "UserCourseProgressDetails{" +
            "email='" + email + '\'' +
            ", incompleteCourses=" + incompleteCourses +
            '}';
  }

  private List<IncompleteCourses> incompleteCourses;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<IncompleteCourses> getIncompleteCourses() {
    return incompleteCourses;
  }

  public void setIncompleteCourses(List<IncompleteCourses> incompleteCourses) {
    this.incompleteCourses = incompleteCourses;
  }
}