package com.lumanlab.parentcaringservice.totp.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NonceService {

    private static final String NONCE_PREFIX = "mfa:nonce:";
    private static final Duration NONCE_EXPIRY = Duration.ofMinutes(5);
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * MFA 초기화를 위한 nonce를 생성하고 userId를 저장합니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 nonce 값
     */
    public String generateNonce(Long userId) {
        String nonce = UUID.randomUUID().toString();
        String key = NONCE_PREFIX + nonce;

        redisTemplate.opsForValue().set(key, userId, NONCE_EXPIRY);

        return nonce;
    }

    /**
     * nonce 값을 이용하여 저장된 사용자 ID를 조회합니다.
     *
     * @param nonce 사용자 ID를 조회하기 위한 nonce 값
     * @return 조회된 사용자 ID (Long 타입)
     * @throws IllegalArgumentException 유효하지 않은 nonce 값일 경우 발생
     */
    public Long getUserIdByNonce(String nonce) {
        String key = NONCE_PREFIX + nonce;
        Object userId = redisTemplate.opsForValue().get(key);

        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }

        throw new IllegalArgumentException("Invalid nonce.");
    }

    /**
     * nonce를 삭제합니다.
     *
     * @param nonce 삭제할 nonce 값
     */
    public void deleteNonce(String nonce) {
        String key = NONCE_PREFIX + nonce;
        redisTemplate.delete(key);
    }
}
