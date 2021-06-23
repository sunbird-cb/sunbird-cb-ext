package org.sunbird.portal.department;

import java.util.Arrays;
import java.util.List;

public class PortalConstants {

	private PortalConstants() {
		throw new IllegalStateException("PortalConstants class");
	}

	public static final String MDO_ROLE_NAME = "MDO ADMIN";
	public static final String MDO_DEPT_TYPE = "MDO";
	public static final String SPV_ROLE_NAME = "SPV ADMIN";
	public static final String SPV_DEPT_TYPE = "SPV";
	public static final String CBP_DEPT_TYPE = "CBP";
	public static final String CBP_ROLE_NAME = "CBP ADMIN";
	public static final String CBC_ROLE_NAME = "CBC ADMIN";
	public static final String CBC_MEMBER_NAME = "CBC_MEMBER";
	public static final String CBC_DEPT_TYPE = "CBC";
	public static final List<String> MDO_ROLES = Arrays.asList(MDO_ROLE_NAME, "WAT_USER");
	public static final List<String> CBP_ROLES = Arrays.asList("EDITOR", "REVIEWER", "PUBLISHER", "CONTENT_CREATOR");
	public static final List<String> FRAC_ROLES = Arrays.asList("IFUMember", "fracAdmin", "fracReviewerOne",
			"fracReviewerTwo", "fracAccessCompetency", "competencyReviewer");
	public static final List<String> CBC_ROLE_LIST = Arrays.asList(CBC_ROLE_NAME, CBC_MEMBER_NAME);
}
