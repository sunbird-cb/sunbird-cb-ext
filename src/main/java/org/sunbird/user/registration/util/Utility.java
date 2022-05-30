package org.sunbird.user.registration.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.common.util.CbExtServerProperties;

@Service
public class Utility {

	static CbExtServerProperties serverProperties;

	@Autowired
	Utility(CbExtServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

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
		if (pat.matcher(email).matches()) {
			List<String> domainList = Arrays.asList(serverProperties.getUserRegistrationDomain().split(","));
			return domainList.contains(email.split("@")[1]);
		}
		return Boolean.FALSE;

	}

}
