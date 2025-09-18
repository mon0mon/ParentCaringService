package com.lumanlab.parentcaringservice.security.jwt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * JWK 키의 메타데이터 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwkMetadata {

    private String currentKeyId;
    private Set<String> activeKeyIds;
    private Instant lastRotationAt;
    private Instant nextRotationAt;
    private int totalKeyCount;
}
