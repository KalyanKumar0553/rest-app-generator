package com.src.main.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class IdentifierLookupHashService {

	private static final String HMAC_SHA_256 = "HmacSHA256";

	@Value("${app.data.encrypt.key}")
	private String dataEncryptKeyRaw;

	private SecretKeySpec hmacKey;

	@PostConstruct
	void init() {
		if (dataEncryptKeyRaw == null || dataEncryptKeyRaw.isBlank()) {
			throw new IllegalStateException("app.data.encrypt.key (or DATA_ENCRYPT_KEY env var) is required");
		}
		this.hmacKey = new SecretKeySpec(normalizeKeyBytes(dataEncryptKeyRaw.trim()), HMAC_SHA_256);
	}

	public String hash(String value) {
		if (value == null) {
			return null;
		}
		try {
			Mac mac = Mac.getInstance(HMAC_SHA_256);
			mac.init(hmacKey);
			byte[] digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		} catch (GeneralSecurityException ex) {
			throw new IllegalStateException("Failed to hash identifier", ex);
		}
	}

	private byte[] normalizeKeyBytes(String raw) {
		byte[] decoded = tryBase64(raw);
		if (decoded != null && isValidAesKeyLength(decoded.length)) {
			return decoded;
		}
		byte[] utf8 = raw.getBytes(StandardCharsets.UTF_8);
		if (isValidAesKeyLength(utf8.length)) {
			return utf8;
		}
		try {
			return MessageDigest.getInstance("SHA-256").digest(utf8);
		} catch (GeneralSecurityException ex) {
			throw new IllegalStateException("Unable to derive identifier hash key", ex);
		}
	}

	private byte[] tryBase64(String raw) {
		try {
			return Base64.getDecoder().decode(raw);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private boolean isValidAesKeyLength(int length) {
		return length == 16 || length == 24 || length == 32;
	}
}
