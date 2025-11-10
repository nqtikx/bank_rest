package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {
    private static final byte[] KEY = "myamyamyamya123!@#myamyamyamya".substring(0, 32).getBytes(StandardCharsets.UTF_8);
    private static final byte[] IV  = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    @Override
    public String convertToDatabaseColumn(String attr) {
        if (attr == null) return null;
        try {
            var skey = new SecretKeySpec(KEY, "AES");
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skey, new GCMParameterSpec(128, IV));
            return Base64.getEncoder().encodeToString(cipher.doFinal(attr.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String db) {
        if (db == null) return null;
        try {
            var skey = new SecretKeySpec(KEY, "AES");
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skey, new GCMParameterSpec(128, IV));
            return new String(cipher.doFinal(Base64.getDecoder().decode(db)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
