package com.tianji.aigc.infrastructure.utils;

import com.tianji.aigc.infrastructure.exception.BusinessException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    public static void notNull(Object value, String paramName) {
        if (value == null) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }

    public static void notEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }

    public static void notEmpty(Collection<?> collection, String paramName) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }

    public static void length(String value, int min, int max, String paramName) {
        if (value == null) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }

        int length = value.length();
        if (length < min || length > max) {
            throw new IllegalArgumentException(paramName + String.format("长度必须在%d-%d之间，当前长度: %d", min, max, length));
        }
    }

    public static void range(int value, int min, int max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(paramName + String.format("必须在%d-%d之间，当前值: %d", min, max, value));
        }
    }

    public static void validVersionFormat(String version, String paramName) {
        notEmpty(version, paramName);
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException(paramName + "版本号格式不正确，应为 X.Y.Z 格式，例如 1.0.0");
        }
    }

    public static class EncryptUtils {

        private static final String ALGORITHM = "AES";
        private static final String SECRET_KEY = "1234567890123456";

        private EncryptUtils() {
        }

        public static String encrypt(String data) {
            try {
                if (data == null) {
                    return null;
                }
                SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encryptedBytes = cipher.doFinal(data.getBytes());
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                throw new BusinessException("加密失败" + e.getMessage(), e);
            }
        }

        public static String decrypt(String encryptedData) {
            try {
                if (encryptedData == null) {
                    return null;
                }
                SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
                return new String(decryptedBytes);
            } catch (Exception e) {
                throw new BusinessException("解密失败:" + e.getMessage(), e);
            }
        }
    }
}
