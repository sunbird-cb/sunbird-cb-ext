package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.sunbird.common.exceptions.ProjectCommonException;
import org.sunbird.common.exceptions.ResponseCode;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.model.SunbirdApiRespParam;
import org.sunbird.core.logger.CbExtLogger;

/**
 * This class will contains all the common utility methods.
 *
 * @author Manzarul
 */
public class ProjectUtil {

    public static CbExtLogger logger = new CbExtLogger(ProjectUtil.class.getName());

    public static PropertiesCache propertiesCache;
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
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
    
    public static SBApiResponse createDefaultResponse(String api) {
		SBApiResponse response = new SBApiResponse();
		response.setId(api);
		response.setVer(Constants.API_VERSION_1);
		response.setParams(new SunbirdApiRespParam());
		response.getParams().setStatus(Constants.SUCCESS);
		response.setResponseCode(HttpStatus.OK);
		response.setTs(DateTime.now().toString());
		return response;
	}


    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }

}