package com.src.main.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DataEncryptionService {

    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.data.encrypt.key}")
    private String dataEncryptKeyRaw;

    private SecretKeySpec aesKey;
    private SecretKeySpec hmacKey;

    @PostConstruct
    void init() {
        if (dataEncryptKeyRaw == null || dataEncryptKeyRaw.isBlank()) {
            throw new IllegalStateException("app.data.encrypt.key (or DATA_ENCRYPT_KEY env var) is required");
        }
        byte[] keyBytes = normalizeKeyBytes(dataEncryptKeyRaw.trim());
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
        this.hmacKey = new SecretKeySpec(keyBytes, HMAC_SHA_256);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packed = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
            return Base64.getEncoder().encodeToString(packed);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to encrypt sensitive data", ex);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            byte[] packed = Base64.getDecoder().decode(ciphertext);
            if (packed.length <= GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[packed.length - GCM_IV_LENGTH];

            System.arraycopy(packed, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(packed, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            // Backward compatibility for legacy plain-text rows
            return ciphertext;
        }
    }

    public String hashForLookup(String value) {
        if (value == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(hmacKey);
            byte[] digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to hash sensitive data", ex);
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

        // Derive deterministic 256-bit key from arbitrary input.
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(utf8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to derive encryption key", ex);
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

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
