package org.sunbird.workallocation.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LastLoginConstants {
	public static final String USER_CONST = "User";

	public static final String VERSION = "3.0.2";

	public static final String EID = "AUDIT";

	public static final String PDATA_ID = "dev.sunbird.cb.ext.service";

	public static final String PDATA_PID = "sunbird-cb-ext-service";

	public static final String TYPE = "WorkOrder";

	public static final String STATE = "FirstLogin";

	public static final String LOGIN_TIME = "login_time";

	public static final List<String> PROPS = Collections.unmodifiableList(Arrays.asList("WAT"));

	private LastLoginConstants() {
	}

}
