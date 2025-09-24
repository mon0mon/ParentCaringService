package com.lumanlab.parentcaringservice.security.jwt.port.outp;

import com.lumanlab.parentcaringservice.security.jwt.domain.JwkData;
import com.lumanlab.parentcaringservice.security.jwt.domain.JwkMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JwkRedisRepository implements JwkRepository {

    private static final String KEY_DATA_PREFIX = "jwk:data:";
    private static final String KEY_METADATA_KEY = "jwk:metadata";
    private static final String CURRENT_KEY_ID_KEY = "jwk:current-key-id";

    private final RedisTemplate<String, JwkData> jwtKeyDataRedisTemplate;
    private final RedisTemplate<String, JwkMetadata> jwtKeyMetadataRedisTemplate;
    private final RedisTemplate<String, String> stringLiteralRedisTemplate;

    /**
     * 현재 키 ID를 Redis에 저장하는 메서드
     *
     * @param keyId 저장하려는 키 ID
     */
    public void saveCurrentKeyId(String keyId) {
        stringLiteralRedisTemplate.opsForValue().set(CURRENT_KEY_ID_KEY, keyId);

        log.debug("현재 키 ID 저장 완료: keyId={}", keyId);
    }

    /**
     * Redis에서 현재 저장된 키 ID를 검색하여 반환하는 메서드
     *
     * @return 현재 저장된 키 ID를 문자열로 반환하며,
     * 데이터가 존재하지 않을 경우 null을 반환함
     */
    public String findCurrentKeyId() {
        return stringLiteralRedisTemplate.opsForValue().get(CURRENT_KEY_ID_KEY);
    }

    /**
     * JWK(Key) 데이터를 Redis에 저장하는 메서드.
     *
     * @param keyData 저장할 JWK 키 데이터 객체
     * @param ttl     키 데이터의 TTL(Time-To-Live) 설정값
     *                null인 경우 TTL 설정 없이 데이터를 저장함
     */
    public void saveKeyData(JwkData keyData, Duration ttl) {
        String key = KEY_DATA_PREFIX + keyData.getKeyId();

        jwtKeyDataRedisTemplate.opsForValue().set(key, keyData);

        if (ttl != null) {
            jwtKeyDataRedisTemplate.expire(key, ttl.toMillis(), TimeUnit.MILLISECONDS);
        }

        log.debug("JWT 키 데이터 저장 완료: keyId={}, ttl={}", keyData.getKeyId(), ttl);
    }

    /**
     * 주어진 키 ID에 해당하는 JWK(Key) 데이터를 Redis에서 검색하여 반환함
     *
     * @param keyId 검색하고자 하는 키 데이터의 ID
     * @return 검색된 JWK(Key) 데이터 객체를 반환하며,
     * 데이터가 존재하지 않을 경우 null을 반환함
     */
    public JwkData findKeyData(String keyId) {
        String key = KEY_DATA_PREFIX + keyId;

        return jwtKeyDataRedisTemplate.opsForValue().get(key);
    }

    /**
     * 주어진 키 ID에 해당하는 키 데이터를 삭제하는 메서드
     *
     * @param keyId 삭제하고자 하는 키 데이터의 ID
     */
    public void deleteKeyData(String keyId) {
        String key = KEY_DATA_PREFIX + keyId;

        jwtKeyDataRedisTemplate.delete(key);

        log.debug("JWT 키 데이터 삭제 완료: keyId={}", keyId);
    }

    /**
     * Redis에서 특정 패턴에 해당하는 키 ID를 검색하여 반환하는 메서드
     *
     * @param pattern 검색을 위한 키의 패턴
     * @return 주어진 패턴에 해당하는 키 ID를 모은 집합
     */
    public Set<String> findKeyIdsByPattern(String pattern) {
        Set<String> keys = jwtKeyDataRedisTemplate.keys(KEY_DATA_PREFIX + pattern);
        Set<String> keyIds = new HashSet<>();

        for (String key : keys) {
            String keyId = key.substring(KEY_DATA_PREFIX.length());
            keyIds.add(keyId);
        }

        return keyIds;
    }

    /**
     * Redis에 저장된 만료된 JWK(Key) 데이터를 정리하는 메서드
     * <p>
     * 이 메서드는 Redis에 저장된 모든 키 ID를 검색한 뒤,
     * 각 키의 만료 여부를 확인하여 만료된 키 데이터를 삭제함
     * <p>
     * 동작 방식:
     * 1. Redis에서 모든 키 ID를 가져옴
     * 2. 각 키 ID에 해당하는 키 데이터를 검색
     * 3. 키 데이터가 존재하며 만료된 경우 해당 키 데이터를 삭제
     */
    public void cleanupExpiredKeys() {
        Set<String> allKeyIds = findKeyIdsByPattern("*");

        for (String keyId : allKeyIds) {
            JwkData keyData = findKeyData(keyId);

            if (keyData != null && keyData.isExpired()) {
                deleteKeyData(keyId);

                log.info("만료된 키 삭제: keyId={}", keyId);
            }
        }
    }

    /**
     * JWK 메타데이터를 Redis에 저장하는 메서드
     *
     * @param metadata 저장하려는 JWK 메타데이터 객체
     */
    public void saveMetadata(JwkMetadata metadata) {
        jwtKeyMetadataRedisTemplate.opsForValue().set(KEY_METADATA_KEY, metadata);

        log.debug("JWT 키 메타데이터 저장 완료: currentKeyId={}, activeKeys={}",
                metadata.getCurrentKeyId(), metadata.getActiveKeyIds().size());
    }

    /**
     * 저장된 JWK 메타데이터를 조회
     *
     * @return 저장된 JWK 메타데이터가 있다면 해당 객체를 반환
     * 저장된 메타데이터가 없을 경우 기본값으로 초기화된 JwkMetadata 객체를 반환
     */
    public JwkMetadata findMetadata() {
        JwkMetadata result = jwtKeyMetadataRedisTemplate.opsForValue().get(KEY_METADATA_KEY);

        if (result != null) {
            return result;
        }

        // 기본 메타데이터 반환
        return JwkMetadata.builder()
                .activeKeyIds(new HashSet<>())
                .totalKeyCount(0)
                .build();
    }

    /**
     * 주어진 키 ID에 해당하는 키 데이터가 존재하는지 확인
     *
     * @param keyId 존재 여부를 확인하려는 키 ID
     * @return 주어진 키 ID에 해당하는 키 데이터가 존재하면 true, 존재하지 않으면 false를 반환
     */
    public boolean existsKeyData(String keyId) {
        String key = KEY_DATA_PREFIX + keyId;

        return jwtKeyDataRedisTemplate.hasKey(key);
    }
}
