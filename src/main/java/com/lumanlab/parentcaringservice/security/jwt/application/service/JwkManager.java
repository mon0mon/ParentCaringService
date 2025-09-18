package com.lumanlab.parentcaringservice.security.jwt.application.service;

import com.lumanlab.parentcaringservice.security.jwt.JwtProperties;
import com.lumanlab.parentcaringservice.security.jwt.domain.JwkData;
import com.lumanlab.parentcaringservice.security.jwt.domain.JwkMetadata;
import com.lumanlab.parentcaringservice.security.jwt.port.outp.JwkRedisRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 키 관리 및 JWK(Jason Web Key) 작업을 처리하는 클래스
 * <p>
 * 주요 기능:
 * - 현재 사용 중인 키 조회 및 관리
 * - RSA 키 생성 및 로테이션
 * - 키 데이터를 Redis 저장소에 저장/로드
 * - 공개 JWK 집합(JWK Set)을 생성
 * - 키 메타데이터 관리
 */
@Slf4j
@Service
public class JwkManager {

    private final JwkRedisRepository jwkRepository;
    private final JwtProperties jwtProperties;
    private final JwkJsonGenerator jwkJsonGenerator;

    private final Map<String, KeyPair> keyCache = new ConcurrentHashMap<>();

    @Getter
    private volatile String currentKeyId;

    public JwkManager(JwkRedisRepository jwkRepository, JwtProperties jwtProperties,
                      JwkJsonGenerator jwkJsonGenerator) {
        this.jwkRepository = jwkRepository;
        this.jwtProperties = jwtProperties;
        this.jwkJsonGenerator = jwkJsonGenerator;

        initializeKeys();
    }

    public KeyPair getCurrentKeyPair() {
        return keyCache.get(currentKeyId);
    }

    public KeyPair getKeyPair(String keyId) {
        KeyPair keyPair = keyCache.get(keyId);
        if (keyPair == null) {
            keyPair = loadKeyFromRedis(keyId);
            if (keyPair != null) {
                keyCache.put(keyId, keyPair);
            }
        }
        return keyPair;
    }

    public Map<String, Object> getPublicJwkSet() {
        Map<String, RSAPublicKey> publicKeys = new HashMap<>();

        for (Map.Entry<String, KeyPair> entry : keyCache.entrySet()) {
            String keyId = entry.getKey();
            RSAPublicKey publicKey = (RSAPublicKey) entry.getValue().getPublic();
            publicKeys.put(keyId, publicKey);
        }

        return jwkJsonGenerator.generateJwkSet(publicKeys);
    }

    public void rotateKey() {
        log.info("키 로테이션 시작");
        generateNewKey();
        cleanupOldKeys();
        updateMetadata();
        log.info("키 로테이션 완료: 새 키 ID = {}", currentKeyId);
    }

    /**
     * 키 상태 정보 조회
     */
    public JwkMetadata getKeyMetadata() {
        return jwkRepository.findMetadata();
    }

    private void initializeKeys() {
        String storedKeyId = jwkRepository.findCurrentKeyId();

        if (storedKeyId == null || !isKeyValid(storedKeyId)) {
            generateNewKey();
        } else {
            currentKeyId = storedKeyId;
            loadKeysFromRedis();
        }

        // 메타데이터 업데이트
        updateMetadata();
    }

    private void generateNewKey() {
        try {
            String keyId = "key-" + Instant.now().toEpochMilli();
            KeyPair keyPair = generateRsaKeyPair();

            // 캐시에 저장
            keyCache.put(keyId, keyPair);

            // Redis에 저장
            saveKeyToRedis(keyId, keyPair);

            // 현재 키 ID 업데이트
            currentKeyId = keyId;
            jwkRepository.saveCurrentKeyId(keyId);

            log.info("새로운 JWT 키 생성 완료: {}", keyId);

        } catch (Exception e) {
            log.error("JWT 키 생성 중 오류 발생", e);
            throw new RuntimeException("JWT 키 생성 실패", e);
        }
    }

    private KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private void saveKeyToRedis(String keyId, KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getKey().getRotationIntervalDuration().multipliedBy(2));

        JwkData keyData = JwkData.builder()
                .keyId(keyId)
                .publicModulus(publicKey.getModulus().toString())
                .publicExponent(publicKey.getPublicExponent().toString())
                .privateModulus(privateKey.getModulus().toString())
                .privateExponent(privateKey.getPrivateExponent().toString())
                .createdAt(now)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        jwkRepository.saveKeyData(keyData, jwtProperties.getKey().getRotationIntervalDuration().multipliedBy(2));
    }

    private KeyPair loadKeyFromRedis(String keyId) {
        try {
            JwkData keyData = jwkRepository.findKeyData(keyId);

            if (keyData == null || !keyData.isValid()) {
                return null;
            }

            return reconstructKeyPair(keyData);

        } catch (Exception e) {
            log.error("Redis에서 키 로드 중 오류 발생: {}", keyId, e);
            return null;
        }
    }

    private KeyPair reconstructKeyPair(JwkData keyData) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 공개키 복원
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new java.math.BigInteger(keyData.getPublicModulus()),
                    new java.math.BigInteger(keyData.getPublicExponent()));
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            // 개인키 복원
            RSAPrivateKeySpec privateKeySpec =
                    new RSAPrivateKeySpec(new java.math.BigInteger(keyData.getPrivateModulus()),
                            new java.math.BigInteger(keyData.getPrivateExponent()));
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            return new KeyPair(publicKey, privateKey);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("키 복원 중 오류 발생", e);
            return null;
        }
    }

    private void loadKeysFromRedis() {
        // 현재 키 로드
        if (currentKeyId != null) {
            KeyPair keyPair = loadKeyFromRedis(currentKeyId);
            if (keyPair != null) {
                keyCache.put(currentKeyId, keyPair);
            }
        }

        // 활성 키들 로드
        Set<String> allKeyIds = jwkRepository.findKeyIdsByPattern("*");
        for (String keyId : allKeyIds) {
            if (!keyCache.containsKey(keyId)) {
                KeyPair keyPair = loadKeyFromRedis(keyId);
                if (keyPair != null) {
                    keyCache.put(keyId, keyPair);
                }
            }
        }
    }

    private boolean isKeyValid(String keyId) {
        if (keyCache.containsKey(keyId)) {
            return true;
        }

        JwkData keyData = jwkRepository.findKeyData(keyId);
        return keyData != null && keyData.isValid();
    }

    private void cleanupOldKeys() {
        JwkMetadata metadata = jwkRepository.findMetadata();

        if (keyCache.size() > jwtProperties.getKey().getMaxKeys()) {
            Set<String> keysToRemove = new HashSet<>();

            // 가장 오래된 키들 식별 (현재 키는 제외)
            keyCache.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(currentKeyId))
                    .sorted((e1, e2) -> {
                        JwkData data1 = jwkRepository.findKeyData(e1.getKey());
                        JwkData data2 = jwkRepository.findKeyData(e2.getKey());
                        if (data1 == null || data2 == null) return 0;
                        return data1.getCreatedAt().compareTo(data2.getCreatedAt());
                    })
                    .limit(keyCache.size() - jwtProperties.getKey().getMaxKeys())
                    .forEach(entry -> keysToRemove.add(entry.getKey()));

            // 키 제거
            for (String keyId : keysToRemove) {
                keyCache.remove(keyId);
                jwkRepository.deleteKeyData(keyId);
                log.info("오래된 키 제거: {}", keyId);
            }
        }

        // 만료된 키들 정리
        jwkRepository.cleanupExpiredKeys();
    }

    private void updateMetadata() {
        Set<String> activeKeyIds = new HashSet<>(keyCache.keySet());

        JwkMetadata metadata = JwkMetadata.builder()
                .currentKeyId(currentKeyId)
                .activeKeyIds(activeKeyIds)
                .lastRotationAt(Instant.now())
                .nextRotationAt(Instant.now().plus(jwtProperties.getKey().getRotationIntervalDuration()))
                .totalKeyCount(activeKeyIds.size())
                .build();

        jwkRepository.saveMetadata(metadata);
    }
}
