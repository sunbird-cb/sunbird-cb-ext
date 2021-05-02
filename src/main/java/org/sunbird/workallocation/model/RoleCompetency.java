package org.sunbird.workallocation.model;

import java.util.List;

public class RoleCompetency {
	private Role roleDetails;
	private List<CompetencyDetails> competencyDetails;

	public Role getRoleDetails() {
		return roleDetails;
	}

	public void setRoleDetails(Role roleDetails) {
		this.roleDetails = roleDetails;
	}

	public List<CompetencyDetails> getCompetencyDetails() {
		return competencyDetails;
	}

	public void setCompetencyDetails(List<CompetencyDetails> competencyDetails) {
		this.competencyDetails = competencyDetails;
	}
}
