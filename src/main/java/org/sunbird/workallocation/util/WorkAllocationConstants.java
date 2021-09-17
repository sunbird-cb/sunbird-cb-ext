package org.sunbird.workallocation.util;

import java.util.ArrayList;
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

	//WAT telemetry const

	public static final String AUDIT_CONST = "AUDIT";

	public static final String USER_CONST = "User";

	public static final String VERSION = "3.0";

	public static final String WAT_NAME = "WAT";

	public static final String EVENTS_NAME = "events";

	public static final String WORK_ORDER_ID_CONST = "WorkOrder";

	public static final String MDO_NAME_CONST = "mdo";

	public static final String VERSION_TYPE = "1.0";

	public static final String EID = "CB_AUDIT";

	public static final String CB_NAME = "CB";


	public static final String TYPE = "WorkOrder";

	public static List<String> PROPS = Collections.unmodifiableList(Arrays.asList("WAT"));


}
