package com.lumanlab.parentcaringservice.security.jwt.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumanlab.parentcaringservice.security.jwt.JwtProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증 서비스
 * <p>
 * JWK 키를 사용하여 JWT 토큰을 서명하고 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwkManager jwkManager;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    /**
     * JWT 액세스 토큰을 생성하는 메서드
     *
     * @param userId JWT 토큰에 포함될 사용자 ID
     * @param claims 추가적으로 토큰에 포함될 클레임 정보. null일 경우 빈 데이터로 처리됨
     * @return 생성된 JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(Long userId, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getAccessToken().getExpirationDuration());

        return generateJwtToken(userId, claims, now, expiration);
    }

    /**
     * 사용자 ID와 클레임 정보를 기반으로 JWT 리프레시 토큰을 생성하는 메서드
     *
     * @param userId JWT 토큰에 포함될 사용자 ID
     * @param claims 추가적으로 토큰에 포함될 클레임 정보. null일 경우 빈 데이터로 처리됨
     * @return 생성된 JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Long userId, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getRefreshToken().getExpirationDuration());

        return generateJwtToken(userId, claims, now, expiration);
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증하고 해당 토큰의 클레임 정보를 반환
     *
     * @param jwtToken 검증할 JWT 토큰 문자열
     * @return 검증된 JWT 토큰에 포함된 클레임 정보 (Claims 객체)
     * @throws JwtException 유효하지 않은 토큰, 만료된 토큰, 서명 오류 등 검증 실패 시 발생
     */
    public Claims validateJwtToken(String jwtToken) {
        try {
            String jwkKeyId = extractJwkKeyIdFromJwt(jwtToken);
            if (jwkKeyId == null) {
                throw new JwtException("JWT 토큰에서 JWK 키 ID를 찾을 수 없습니다");
            }

            KeyPair jwkKeyPair = jwkManager.getKeyPair(jwkKeyId);
            if (jwkKeyPair == null) {
                throw new JwtException("유효하지 않은 JWK 키 ID입니다: " + jwkKeyId);
            }

            PublicKey jwkPublicKey = jwkKeyPair.getPublic();

            return Jwts.parser()
                    .verifyWith(jwkPublicKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰: {}", e.getMessage());
            throw new JwtException("만료된 JWT 토큰입니다", e);
        } catch (UnsupportedJwtException e) {
            log.debug("지원하지 않는 JWT 토큰: {}", e.getMessage());
            throw new JwtException("지원하지 않는 JWT 토큰입니다", e);
        } catch (MalformedJwtException e) {
            log.debug("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            throw new JwtException("잘못된 형식의 JWT 토큰입니다", e);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.debug("유효하지 않은 JWT 토큰 서명: {}", e.getMessage());
            throw new JwtException("유효하지 않은 JWT 토큰 서명입니다", e);
        } catch (IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new JwtException("유효하지 않은 JWT 토큰입니다", e);
        }
    }

    /**
     * /**
     * JWT에서 subject를 추출하는 함수
     *
     * @param jwtToken JWT 토큰 문자열
     * @return 추출된 subject 값
     * @throws JwtException JWT 토큰이 유효하지 않거나 subject를 추출할 수 없는 경우
     */
    public String extractSubject(String jwtToken) {
        Claims claims = validateJwtToken(jwtToken);

        return claims.getSubject();
    }

    /**
     * 주어진 JWT 토큰의 헤더에서 JWK(Key ID, kid)를 추출
     *
     * @param jwtToken JWK 키 ID를 추출할 대상 JWT 토큰 문자열
     * @return 추출된 JWK 키 ID 문자열. 토큰이 적절한 형식이 아니거나 kid가 없으면 null 반환
     */
    private String extractJwkKeyIdFromJwt(String jwtToken) {
        try {
            String[] chunks = jwtToken.split("\\.");
            if (chunks.length != 3) {
                return null;
            }

            byte[] headerBytes = Base64.getUrlDecoder().decode(chunks[0]);
            JsonNode headerNode = objectMapper.readTree(headerBytes);

            JsonNode kidNode = headerNode.get("kid");
            return kidNode != null ? kidNode.asText() : null;

        } catch (IllegalArgumentException | IOException e) {
            log.debug("JWT 토큰에서 JWK 키 ID 추출 중 오류 발생", e);
            return null;
        }
    }

    /**
     * JWT 토큰을 생성하는 메서드
     *
     * @param userId     JWT 토큰에 포함될 사용자 ID
     * @param claims     추가적으로 토큰에 포함될 클레임 정보. null일 경우 빈 데이터로 처리됨
     * @param now        토큰의 발급 시간 (issuedAt으로 설정)
     * @param expiration 토큰의 만료 시간 (expiration으로 설정)
     * @return 생성된 JWT 토큰 문자열
     */
    private String generateJwtToken(Long userId, Map<String, Object> claims, Instant now, Instant expiration) {
        String jwkKeyId = jwkManager.getCurrentKeyId();
        PrivateKey jwkPrivateKey = jwkManager.getCurrentKeyPair().getPrivate();

        Map claimsMap = claims == null ? Map.of() : claims;

        return Jwts.builder()
                .setHeader(Map.of("alg", "RS256", "typ", "JWT", "kid", jwkKeyId))
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claims(claimsMap)
                .signWith(jwkPrivateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
