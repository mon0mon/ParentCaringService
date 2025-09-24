package com.lumanlab.parentcaringservice.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();
    private Key key = new Key();
    private String issuer;

    @Data
    public static class AccessToken {
        private long expirationTime; // 초 단위

        public Duration getExpirationDuration() {
            return Duration.ofSeconds(expirationTime);
        }

        public long getExpirationTimeMillis() {
            return expirationTime * 1000;
        }
    }

    @Data
    public static class RefreshToken {
        private long expirationTime; // 초 단위

        public Duration getExpirationDuration() {
            return Duration.ofSeconds(expirationTime);
        }

        public long getExpirationTimeMillis() {
            return expirationTime * 1000;
        }
    }

    @Data
    public static class Key {
        private long rotationInterval; // 초 단위
        private int maxKeys;
        private long gracePeriod; // 그레이스 기간 (초 단위)

        public Duration getRotationIntervalDuration() {
            return Duration.ofSeconds(rotationInterval);
        }

        public long getRotationIntervalMillis() {
            return rotationInterval * 1000;
        }

        public Duration getGracePeriodDuration() {
            return Duration.ofSeconds(gracePeriod);
        }

        public long getGracePeriodMillis() {
            return gracePeriod * 1000;
        }
    }
}
