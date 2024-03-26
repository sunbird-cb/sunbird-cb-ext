package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.common.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
@Component
public class AccessTokenValidator {

    @Autowired
    KeyManager keyManager;
    private static Logger logger = LoggerFactory.getLogger(AccessTokenValidator.class.getName());
    private static ObjectMapper mapper = new ObjectMapper();
    private static PropertiesCache cache = PropertiesCache.getInstance();

    private Map<String, Object> validateToken(String token) throws Exception {
        try {
            String[] tokenElements = token.split("\\.");
            String header = tokenElements[0];
            String body = tokenElements[1];
            String signature = tokenElements[2];
            String payLoad = header + Constants.DOT_SEPARATOR + body;
            Map<Object, Object> headerData =
                    mapper.readValue(new String(decodeFromBase64(header)), Map.class);
            String keyId = headerData.get("kid").toString();
            boolean isValid =
                    CryptoUtil.verifyRSASign(
                            payLoad,
                            decodeFromBase64(signature),
                            keyManager.getPublicKey(keyId).getPublicKey(),
                            Constants.SHA_256_WITH_RSA);
            if (isValid) {
                Map<String, Object> tokenBody =
                        mapper.readValue(new String(decodeFromBase64(body)), Map.class);
                boolean isExp = isExpired((Integer) tokenBody.get("exp"));
                if (isExp) {
                    return Collections.emptyMap();
                }
                return tokenBody;
            }
        } catch (IOException e) {
            return Collections.emptyMap();
        }
        return Collections.emptyMap();
    }


    public String verifyUserToken(String token) {
        String userId = Constants._UNAUTHORIZED;
        try {
            Map<String, Object> payload = validateToken(token);
            if (MapUtils.isNotEmpty(payload) && checkIss((String) payload.get("iss"))) {
                userId = (String) payload.get(Constants.SUB);
                if (StringUtils.isNotBlank(userId)) {
                    int pos = userId.lastIndexOf(":");
                    userId = userId.substring(pos + 1);
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in verifyUserAccessToken: verify ", ex);
        }
        return userId;
    }

	private boolean checkIss(String iss) {
		String realmUrl = cache.getProperty(Constants.SSO_URL) + "realms/" + cache.getProperty(Constants.SSO_REALM);
		if (StringUtils.isBlank(realmUrl))
			return false;
		return (realmUrl.equalsIgnoreCase(iss));
	}

    private boolean isExpired(Integer expiration) {
        return (Time.currentTime() > expiration);
    }

    private byte[] decodeFromBase64(String data) {
        return Base64Util.decode(data, 11);
    }

    public String fetchUserIdFromAccessToken(String accessToken) {
        String clientAccessTokenId = null;
        if (accessToken != null) {
            try {
                clientAccessTokenId = verifyUserToken(accessToken);
                if (Constants._UNAUTHORIZED.equalsIgnoreCase(clientAccessTokenId)) {
                    clientAccessTokenId = null;
                }
            } catch (Exception ex) {
                String errMsg = "Exception occurred while fetching the userid from the access token. Exception: " + ex.getMessage();
                logger.error(errMsg, ex);
                clientAccessTokenId = null;
            }
        }
        return clientAccessTokenId;
    }
}
