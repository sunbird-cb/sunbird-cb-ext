package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.sunbird.common.exceptions.ProjectCommonException;
import org.sunbird.common.exceptions.ResponseCode;
import org.sunbird.core.logger.CbExtLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will contains all the common utility methods.
 *
 * @author Manzarul
 */
public class ProjectUtil {

    public static CbExtLogger logger = new CbExtLogger(ProjectUtil.class.getName());
    private static Pattern pattern;
    public static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static PropertiesCache propertiesCache;
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        pattern = Pattern.compile(EMAIL_PATTERN);
        propertiesCache = PropertiesCache.getInstance();
    }

    public static String getConfigValue(String key) {
        if (StringUtils.isNotBlank(System.getenv(key))) {
            return System.getenv(key);
        }
        return propertiesCache.readProperty(key);
    }

    /**
     * This method will check incoming value is null or empty it will do empty check by doing trim
     * method. in case of null or empty it will return true else false.
     *
     * @param value
     * @return
     */
    public static boolean isStringNullOREmpty(String value) {
        return (value == null || "".equals(value.trim()));
    }

    /**
     * This method will create and return server exception to caller.
     *
     * @param responseCode ResponseCode
     * @return ProjectCommonException
     */
    public static ProjectCommonException createServerError(ResponseCode responseCode) {
        return new ProjectCommonException(
                responseCode.getErrorCode(),
                responseCode.getErrorMessage(),
                ResponseCode.SERVER_ERROR.getResponseCode());
    }

    public static ProjectCommonException createClientException(ResponseCode responseCode) {
        return new ProjectCommonException(
                responseCode.getErrorCode(),
                responseCode.getErrorMessage(),
                ResponseCode.CLIENT_ERROR.getResponseCode());
    }

    public static Map<String, Object> getUserDefaultValue() {
        Map<String, Object> user = new HashMap<>();
        user.put("avatar", null);
        user.put("gender", null);
        user.put("grade", null);
        user.put("language", null);
        user.put("lastLoginTime", null);
        user.put("location", null);
        user.put("profileSummary", null);
        user.put("profileVisibility", null);
        user.put("tempPassword", null);
        user.put("thumbnail", null);
        user.put("registryId", null);
        user.put("accesscode", null);
        user.put("subject", null);
        user.put("webPages", null);
        user.put("currentLoginTime", null);
        user.put("password", null);
        user.put("loginId", null);
        user.put(Constants.EMAIL_VERIFIED, true);
        user.put(Constants.PHONE_VERIFIED, true);
        return user;
    }

    public enum EsType {
        user(getConfigValue("user_index_alias")),
        organisation(getConfigValue("org_index_alias")),
        usernotes("usernotes"),
        location("location"),
        userfeed("userfeed");

        private String typeName;

        EsType(String name) {
            this.typeName = name;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    public static final String[] excludes =
            new String[] {
                    Constants.COMPLETENESS, Constants.MISSING_FIELDS, Constants.PROFILE_VISIBILITY, Constants.LOGIN_ID
            };

    /**
     * Validate email with regular expression
     *
     * @param email
     * @return true valid email, false invalid email
     */
    public static boolean isEmailvalid(final String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }

}