package com.lumanlab.parentcaringservice.security.jwt.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Redis에 저장되는 JWK 키 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwkData {

    private String keyId;
    private String publicModulus;
    private String publicExponent;
    private String privateModulus;
    private String privateExponent;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean active;

    /**
     * 키가 만료되었는지 확인
     */
    @JsonIgnore
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * 키가 유효한지 확인 (활성 상태이고 만료되지 않음)
     */
    @JsonIgnore
    public boolean isValid() {
        return active && !isExpired();
    }
}
