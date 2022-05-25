package org.sunbird.common.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.service.DataMaskingService;
import org.sunbird.common.service.DecryptionService;
import org.sunbird.common.service.EncryptionService;
import org.sunbird.common.service.impl.ServiceFactory;
import org.sunbird.core.logger.CbExtLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is for utility methods for encrypting user data.
 *
 * @author Amit Kumar
 */
public final class UserUtility {
  public static CbExtLogger logger = new CbExtLogger(ProjectUtil.class.getName());

  private static List<String> userKeyToEncrypt;
  private static List<String> userKeyToDecrypt;
  private static List<String> userKeysToMasked;
  private static DecryptionService decryptionService;
  private static DataMaskingService maskingService;
  private static List<String> phoneMaskedAttributes;
  private static List<String> emailMaskedAttributes;

  static {
    init();
  }

  private UserUtility() {}

  public static Map<String, Object> encryptUserData(Map<String, Object> userMap) {
    return encryptSpecificUserData(userMap, userKeyToEncrypt);
  }

  public static Map<String, Object> encryptSpecificUserData(
      Map<String, Object> userMap, List<String> fieldsToEncrypt) {
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance();
    // Encrypt user basic info
    for (String key : fieldsToEncrypt) {
      if (userMap.containsKey(key)) {
        userMap.put(key, service.encryptData((String) userMap.get(key)));
      }
    }
    return userMap;
  }

  public static Map<String, Object> decryptUserData(Map<String, Object> userMap) {
    return decryptSpecificUserData(userMap, userKeyToEncrypt);
  }

  public static Map<String, Object> decryptSpecificUserData(
      Map<String, Object> userMap, List<String> fieldsToDecrypt) {
    DecryptionService service = ServiceFactory.getDecryptionServiceInstance();
    // Decrypt user basic info
    for (String key : fieldsToDecrypt) {
      if (userMap.containsKey(key)) {
        userMap.put(key, service.decryptData((String) userMap.get(key)));
      }
    }
    return userMap;
  }

  public static boolean isMasked(String data) {
    return maskingService.isMasked(data);
  }

  public static Map<String, Object> decryptUserDataFrmES(Map<String, Object> userMap) {
    DecryptionService service = ServiceFactory.getDecryptionServiceInstance();
    // Decrypt user basic info
    for (String key : userKeyToDecrypt) {
      if (userMap.containsKey(key)) {
        if (userKeysToMasked.contains(key)) {
          userMap.put(key, maskEmailOrPhone((String) userMap.get(key), key));
        } else {
          userMap.put(key, service.decryptData((String) userMap.get(key)));
        }
      }
    }
    return userMap;
  }

  public static Map<String, Object> encryptUserSearchFilterQueryData(Map<String, Object> map) {
    Map<String, Object> filterMap = (Map<String, Object>) map.get(Constants.FILTERS);
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance();
    // Encrypt user basic info
    for (String key : userKeyToEncrypt) {
      if (filterMap.containsKey(key)) {
        filterMap.put(key, service.encryptData((String) filterMap.get(key)));
      }
    }
    return filterMap;
  }

  public static String encryptData(String data) {
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance();
    return service.encryptData(data);
  }

  public static String maskEmailOrPhone(String encryptedEmailOrPhone, String type) {
    if (StringUtils.isEmpty(encryptedEmailOrPhone)) {
      return StringUtils.EMPTY;
    }
    if (phoneMaskedAttributes.contains(type)) {
      return maskingService.maskPhone(decryptionService.decryptData(encryptedEmailOrPhone));
    } else if (emailMaskedAttributes.contains(type)) {
      return maskingService.maskEmail(decryptionService.decryptData(encryptedEmailOrPhone));
    }
    return StringUtils.EMPTY;
  }

  private static void init() {
    decryptionService = ServiceFactory.getDecryptionServiceInstance();
    maskingService = ServiceFactory.getMaskingServiceInstance();
    String userKey = PropertiesCache.getInstance().getProperty("userkey.encryption");
    userKeyToEncrypt = new ArrayList<>(Arrays.asList(userKey.split(",")));
    logger.info("UserUtility:init:user encrypt  attributes got".concat(userKey + ""));
    String userKeyDecrypt = PropertiesCache.getInstance().getProperty("userkey.decryption");
    String userKeyToMasked = PropertiesCache.getInstance().getProperty("userkey.masked");
    userKeyToDecrypt = new ArrayList<>(Arrays.asList(userKeyDecrypt.split(",")));
    userKeysToMasked = new ArrayList<>(Arrays.asList(userKeyToMasked.split(",")));
    String emailTypeAttributeKey =
        PropertiesCache.getInstance().getProperty("userkey.emailtypeattributes");
    String phoneTypeAttributeKey =
        PropertiesCache.getInstance().getProperty("userkey.phonetypeattributes");
    emailMaskedAttributes = new ArrayList<>(Arrays.asList(emailTypeAttributeKey.split(",")));
    logger.info("UserUtility:init:email masked attributes got".concat(emailTypeAttributeKey + ""));
    phoneMaskedAttributes = new ArrayList<>(Arrays.asList(phoneTypeAttributeKey.split(",")));
    logger.info("UserUtility:init:phone masked attributes got".concat(phoneTypeAttributeKey + ""));
  }
}