package org.sunbird.user.registration.service;

import org.sunbird.common.model.SBApiResponse;
import org.sunbird.user.registration.model.UserRegistrationInfo;

/**
 * Service class which provides CURD operation for User Registration.
 * 
 * @author karthik
 *
 */
public interface UserRegistrationService {
	/**
	 * Registers the given user and creates WorkFlow Service request for approval.
	 * 
	 * @param userRegInfo - User Registration Information
	 * @return - Returns the API Response object which contains success / failure
	 *         message
	 */
	public SBApiResponse registerUser(UserRegistrationInfo userRegInfo);

	/**
	 * Retrieves the User Registration Code details if exists for the given
	 * Registration Code value.
	 * 
	 * @param registrationCode - Registration Code which used to identify the object
	 * @return - Returns the API Response object which contains success / failure
	 *         message
	 */
	public SBApiResponse getUserRegistrationDetails(String registrationCode);

	/**
	 * Retrieves the department details which can be provided user to select upon
	 * while creating Register request
	 * 
	 * @return - Returns the API response object which contains success / failure
	 *         message
	 */
	public SBApiResponse getDeptDetails();

	/**
	 * Initiates the User Registration process
	 * 
	 * @param registrationCode - User Registration Code
	 */
	public void initiateCreateUserFlow(String registrationCode);
}
