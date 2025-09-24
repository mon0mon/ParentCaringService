package com.lumanlab.parentcaringservice.security.encoder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class DefaultRefreshTokenEncoder implements RefreshTokenEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawToken) {
        // JWT 토큰을 먼저 SHA-256으로 해시하여 길이를 줄임
        String preHashed = preHash(rawToken);
        return encoder.encode(preHashed);
    }

    public boolean matches(String rawToken, String encodedToken) {
        String preHashed = preHash(rawToken);
        return encoder.matches(preHashed, encodedToken);
    }

    private String preHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
