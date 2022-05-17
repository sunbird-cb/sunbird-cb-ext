package org.sunbird.common.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.service.EncryptionService;
import org.sunbird.common.util.BASE64Encoder;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import org.sunbird.core.logger.CbExtLogger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Default data encryption service
 *
 * @author Manzarul
 */
public class DefaultEncryptionServiceImpl implements EncryptionService {

  public static CbExtLogger logger = new CbExtLogger(ProjectUtil.class.getName());

  private static String encryption_key = "";

  private String sunbirdEncryption = "";

  private static Cipher c;

  static {
    try {
      encryption_key = getSalt();
      Key key = generateKey();
      c = Cipher.getInstance(ALGORITHM);
      c.init(Cipher.ENCRYPT_MODE, key);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public DefaultEncryptionServiceImpl() {
    sunbirdEncryption = System.getenv(Constants.SUNBIRD_ENCRYPTION);
    if (StringUtils.isBlank(sunbirdEncryption)) {
      sunbirdEncryption = ProjectUtil.getConfigValue(Constants.SUNBIRD_ENCRYPTION);
    }
  }

  @Override
  public Map<String, Object> encryptData(Map<String, Object> data) {
    if (Constants.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (data == null) {
        return data;
      }
      Iterator<Entry<String, Object>> itr = data.entrySet().iterator();
      while (itr.hasNext()) {
        Entry<String, Object> entry = itr.next();
        if (!(entry.getValue() instanceof Map || entry.getValue() instanceof List)
            && null != entry.getValue()) {
          data.put(entry.getKey(), encrypt(entry.getValue() + ""));
        }
      }
    }
    return data;
  }

  @Override
  public List<Map<String, Object>> encryptData(
      List<Map<String, Object>> data) {
    if (Constants.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (data == null || data.isEmpty()) {
        return data;
      }
      for (Map<String, Object> map : data) {
        encryptData(map);
      }
    }
    return data;
  }

  @Override
  public String encryptData(String data) {
    if (Constants.ON.equalsIgnoreCase(sunbirdEncryption)) {
      if (StringUtils.isNotBlank(data)) {
        return encrypt(data);
      } else {
        return data;
      }
    } else {
      return data;
    }
  }

  /**
   * this method is used to encrypt the password.
   *
   * @param value String password
   * @return encrypted password.
   */
  @SuppressWarnings("restriction")
  public static String encrypt(String value) {
    String valueToEnc = null;
    String eValue = value;
    for (int i = 0; i < ITERATIONS; i++) {
      valueToEnc = encryption_key + eValue;
      byte[] encValue = new byte[0];
      try {
        encValue = c.doFinal(valueToEnc.getBytes(StandardCharsets.UTF_8));
      } catch (Exception e) {
        logger.error(e);
      }
      eValue = new BASE64Encoder().encode(encValue);
    }
    return eValue;
  }

  private static Key generateKey() {
    return new SecretKeySpec(keyValue, ALGORITHM);
  }

  /** @return */
  public static String getSalt() {
    if (!StringUtils.isBlank(encryption_key)) {
      return encryption_key;
    } else {
      encryption_key = System.getenv(Constants.ENCRYPTION_KEY);
      if (StringUtils.isBlank(encryption_key)) {
        logger.info("Salt value is not provided by Env");
        encryption_key = ProjectUtil.getConfigValue(Constants.ENCRYPTION_KEY);
      }
    }
    if (StringUtils.isBlank(encryption_key)) {
      logger.info("throwing exception for invalid salt");
    }
    return encryption_key;
  }
}