package org.sunbird.core.cipher;

import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sunbird.common.util.Constants;

@Component
public class DecryptServiceImpl {

	private int ITERATIONS = 3;
	
	@Value("${sb.env.chiper.password}")
	private String sbChiperPassword;
	
	private Cipher decryptCipher;
	private Cipher encryptCipher;

	@Autowired
	private SecretKeySpec secretKeySpec;

	@Value("${user.report.store.path}")
	private String userStorePath;

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	@PostConstruct
	private void postConstruct() {
		try {
			decryptCipher = Cipher.getInstance(Constants.CIPHER_ALGORITHM);
			decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			encryptCipher = Cipher.getInstance(Constants.CIPHER_ALGORITHM);
			encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		} catch (Exception e) {
			log.error("Failed to construct DecryptServiceImpl object.");
		}
	}

	public String decryptString(String encStr) {
		try {
			String dValue = null;
			String valueToDecrypt = encStr.trim();
			for (int i = 0; i < ITERATIONS; i++) {
				byte[] decodedValue = new BASE64Decoder().decodeBuffer(valueToDecrypt);
				byte[] decValue = decryptCipher.doFinal(decodedValue);
				dValue = new String(decValue, StandardCharsets.UTF_8).substring(sbChiperPassword.length());
				valueToDecrypt = dValue;
			}
			return dValue;
		} catch (Exception ex) {
			log.error("Failed to decrypt value. Exception: ", ex);
		}
		return null;
	}
}
