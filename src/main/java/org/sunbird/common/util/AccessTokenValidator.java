package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.common.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class AccessTokenValidator {
    private static Logger logger = LoggerFactory.getLogger(AccessTokenValidator.class.getName());
    private static ObjectMapper mapper = new ObjectMapper();

    private static Map<String, Object> validateToken(String token) throws Exception {
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
                            KeyManager.getPublicKey(keyId).getPublicKey(),
                            Constants.SHA_256_WITH_RSA);
            if (isValid) {
                Map<String, Object> tokenBody =
                        mapper.readValue(new String(decodeFromBase64(body)), Map.class);
                boolean isExp = isExpired((Integer) tokenBody.get("exp"));
                if (isExp) {
                    return Collections.EMPTY_MAP;
                }
                return tokenBody;
            }
        } catch (IOException e) {
            return Collections.EMPTY_MAP;
        }
        return Collections.EMPTY_MAP;
    }


    public static String verifyUserToken(String token) {
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

    private static boolean checkIss(String iss) {
        String realmUrl =
                KeyCloakConnectionProvider.SSO_URL + "realms/" + KeyCloakConnectionProvider.SSO_REALM;
        return (realmUrl.equalsIgnoreCase(iss));
    }

    private static boolean isExpired(Integer expiration) {
        return (Time.currentTime() > expiration);
    }

    private static byte[] decodeFromBase64(String data) {
        return Base64Util.decode(data, 11);
    }
}
