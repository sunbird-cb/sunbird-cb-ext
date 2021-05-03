package org.sunbird.workallocation.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorkAllocationConstants {
	public static final String ALLOCATION_DETAILS = "allocationDetails";
	public static final String USER_DETAILS = "userDetails";

	public static final String DRAFT_STATUS = "Draft";
	public static final String PUBLISHED_STATUS = "Published";
	public static final String ARCHIVED_STATUS = "Archived";
	public static final List<String> STATUS_LIST = Collections
			.unmodifiableList(Arrays.asList(DRAFT_STATUS, PUBLISHED_STATUS, ARCHIVED_STATUS));
	public static final String ADD = "add";
	public static final String UPDATE = "update";
}
