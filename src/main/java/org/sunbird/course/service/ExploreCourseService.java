package org.sunbird.course.service;

import org.sunbird.common.model.SBApiResponse;

/**
 * Provides list of API implementation to access course details which are
 * specific to public users.
 * 
 * @author karthik
 *
 */
public interface ExploreCourseService {
	/**
	 * Retrieves the list of course details from sunbird_courses.public_course
	 * table.
	 * 
	 * @return - Course details in Sunbird API Response format.
	 */
	public SBApiResponse getExploreCourseList();

	/**
	 * Refreshes the cache by re-reading the details from DB.
	 * 
	 * @return - Returns success or failure details in Sunbird API Response format.
	 */
	public SBApiResponse refreshCache();
}
