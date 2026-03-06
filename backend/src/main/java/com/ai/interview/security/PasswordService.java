package com.ai.interview.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class PasswordService implements PasswordEncoder {

    private static final String PBKDF2_PREFIX = "pbkdf2$";

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        validateRawPassword(rawPassword);
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String storedPassword) {
        validateRawPassword(rawPassword);

        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return delegate.matches(rawPassword, storedPassword);
        }

        if (storedPassword.startsWith(PBKDF2_PREFIX)) {
            return matchesLegacyPbkdf2(rawPassword, storedPassword);
        }

        return MessageDigest.isEqual(
                rawPassword.toString().getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public boolean upgradeEncoding(String storedPassword) {
        return needsUpgrade(storedPassword);
    }

    public boolean needsUpgrade(String storedPassword) {
        return storedPassword == null || !isBcryptHash(storedPassword) || delegate.upgradeEncoding(storedPassword);
    }

    private boolean matchesLegacyPbkdf2(CharSequence rawPassword, String storedPassword) {
        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = pbkdf2(rawPassword.toString().toCharArray(), salt, iterations, expectedHash.length);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int hashLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, hashLength * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("密码校验失败", ex);
        }
    }

    private boolean isBcryptHash(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    private void validateRawPassword(CharSequence rawPassword) {
        if (rawPassword == null || rawPassword.toString().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }
}