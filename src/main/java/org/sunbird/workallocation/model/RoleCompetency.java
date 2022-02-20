package org.sunbird.workallocation.model;

import java.util.List;

public class RoleCompetency {
	private Role roleDetails;
	private List<CompetencyDetails> competencyDetails;

	public List<CompetencyDetails> getCompetencyDetails() {
		return competencyDetails;
	}

	public Role getRoleDetails() {
		return roleDetails;
	}

	public void setCompetencyDetails(List<CompetencyDetails> competencyDetails) {
		this.competencyDetails = competencyDetails;
	}

	public void setRoleDetails(Role roleDetails) {
		this.roleDetails = roleDetails;
	}
}
