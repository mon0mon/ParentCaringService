package com.lumanlab.parentcaringservice.security.jwt.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwkJsonGenerator {

    public Map<String, Object> generateJwkSet(Map<String, RSAPublicKey> publicKeys) {
        List<Map<String, Object>> keys = new ArrayList<>();

        for (Map.Entry<String, RSAPublicKey> entry : publicKeys.entrySet()) {
            String keyId = entry.getKey();
            RSAPublicKey publicKey = entry.getValue();

            Map<String, Object> jwk = new HashMap<>();
            jwk.put("kty", "RSA");
            jwk.put("use", "sig");
            jwk.put("alg", "RS256");
            jwk.put("kid", keyId);

            // RSA 공개키의 modulus와 exponent를 Base64 URL 인코딩
            jwk.put("n", base64UrlEncode(publicKey.getModulus().toByteArray()));
            jwk.put("e", base64UrlEncode(publicKey.getPublicExponent().toByteArray()));

            keys.add(jwk);
        }

        Map<String, Object> jwkSet = new HashMap<>();
        jwkSet.put("keys", keys);

        return jwkSet;
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(removeLeadingZeros(data));
    }

    private byte[] removeLeadingZeros(byte[] data) {
        if (data.length == 0 || data[0] != 0) {
            return data;
        }

        int firstNonZero = 0;
        while (firstNonZero < data.length && data[firstNonZero] == 0) {
            firstNonZero++;
        }

        return Arrays.copyOfRange(data, firstNonZero, data.length);
    }
}
