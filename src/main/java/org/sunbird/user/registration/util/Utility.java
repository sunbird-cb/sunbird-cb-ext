package org.sunbird.user.registration.util;

import java.util.regex.Pattern;

public class Utility {

	/**
	 * Check the email id is valid or not
	 * 
	 * @param email
	 *            String
	 * @return Boolean
	 */
	public static Boolean emailValidation(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";

		Pattern pat = Pattern.compile(emailRegex);
		return (email == null) ? Boolean.FALSE : pat.matcher(email).matches();

	}

}
