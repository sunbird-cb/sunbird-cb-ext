package org.sunbird.common.util;

import java.util.List;

public class DataValidator {

	public static boolean isCollectionEmpty(Iterable<?> list) {
		return list == null || !list.iterator().hasNext();
	}

	public static boolean isCollectionEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	public static boolean isStringEmpty(String str) {
		return str == null || str.isEmpty();
	}

	private DataValidator() {
		throw new IllegalStateException("DataValidator class");
	}

}
