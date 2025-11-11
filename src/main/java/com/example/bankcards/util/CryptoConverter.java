package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    // 256-битный ключ (если короче — добиваем нулями)
    private static final byte[] KEY = Arrays.copyOf(
            "myamyamyamya123!@#myamyamyamya".getBytes(StandardCharsets.UTF_8), 32);

    // фиксированный IV
    private static final byte[] IV = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    @Override
    public String convertToDatabaseColumn(String attr) {
        if (attr == null || attr.isEmpty()) return null;
        try {
            var keySpec = new SecretKeySpec(KEY, "AES");
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, IV));
            byte[] encrypted = cipher.doFinal(attr.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            var keySpec = new SecretKeySpec(KEY, "AES");
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(128, IV));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
