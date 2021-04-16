package org.sunbird.common.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ContentService {

	/**
	 * gets specific meta for a list of content ids
	 * 
	 * @param ids
	 * @param source
	 * @param Status
	 * @return
	 * @throws IOException
	 */
	List<Map<String, Object>> getMetaByIDListandSource(List<String> ids, String[] source, String status)
			throws IOException;
}
