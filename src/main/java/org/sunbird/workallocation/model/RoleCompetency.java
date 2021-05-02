package org.sunbird.workallocation.model;

import java.util.List;

public class RoleCompetency {
	private List<Role> roleDetails;
	private List<CompetencyDetails> competencyDetails;

	public List<Role> getRoleDetails() {
		return roleDetails;
	}

	public void setRoleDetails(List<Role> roleDetails) {
		this.roleDetails = roleDetails;
	}

	public List<CompetencyDetails> getCompetencyDetails() {
		return competencyDetails;
	}

	public void setCompetencyDetails(List<CompetencyDetails> competencyDetails) {
		this.competencyDetails = competencyDetails;
	}
}
