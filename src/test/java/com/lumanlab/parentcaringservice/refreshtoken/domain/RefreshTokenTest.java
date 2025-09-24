package com.lumanlab.parentcaringservice.refreshtoken.domain;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenTest {

    final String EMAIL = "john.doe@example.com";
    final String PASSWORD = "PASSWORD";
    final String TOKEN = "TOKEN";
    final String IP = "127.0.0.1";
    final UserAgent USER_AGENT = UserAgent.MOBILE;

    User user;
    RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT));

        refreshToken =
                new RefreshToken(user, TOKEN, IP, USER_AGENT, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 확인")
    void initRefreshToken() {
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getUser()).isEqualTo(user);
        assertThat(refreshToken.getTokenHash()).isEqualTo(TOKEN);
        assertThat(refreshToken.getIp()).isEqualTo(IP);
        assertThat(refreshToken.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(refreshToken.getIssuedAt()).isNotNull();
        assertThat(refreshToken.getExpiredAt()).isNotNull();
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 오류 - 사용자 NULL")
    void initRefreshTokenUserNull() {
        assertThatThrownBy(() -> new RefreshToken(null, TOKEN, IP, USER_AGENT, OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 오류 - TokenHash NULL")
    void initRefreshTokenTokenHashNull() {
        assertThatThrownBy(() -> new RefreshToken(user, null, IP, USER_AGENT, OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 오류 - TokenHash 공백")
    void initRefreshTokenTokenHashBlank() {
        assertThatThrownBy(() -> new RefreshToken(user, "", IP, USER_AGENT, OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 오류 - IssuedAt NULL")
    void initRefreshTokenIssuedAtNull() {
        assertThatThrownBy(() -> new RefreshToken(user, TOKEN, IP, USER_AGENT, null,
                OffsetDateTime.now().plusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 오류 - ExpiredAt NULL")
    void initRefreshTokenExpiredAtNull() {
        assertThatThrownBy(
                () -> new RefreshToken(user, TOKEN, IP, USER_AGENT, OffsetDateTime.now(), null)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 오류 - ExpiredAt이 IssuedAt보다 과거일 때")
    void initRefreshTokenExpiredAtBeforeIssuedAt() {
        assertThatThrownBy(() -> new RefreshToken(user, TOKEN, IP, USER_AGENT, OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 상태 조회 - ACTIVE")
    void getRefreshTokenStatusActive() {
        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
    }

    @Test
    @DisplayName("리프레시 토큰 - 상태 조회 - 토큰 유효시간이 만료된 경우 - EXPIRED")
    void getRefreshTokenStatusExpired1() {
        refreshToken =
                new RefreshToken(user, TOKEN, IP, USER_AGENT, OffsetDateTime.now().minusDays(1),
                        OffsetDateTime.now().minusSeconds(1));

        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.EXPIRED);
    }

    @Test
    @DisplayName("리프레시 토큰 - 상태 조회 - 토큰이 취소된 경우 - EXPIRED")
    void getRefreshTokenStatusExpired2() {
        refreshToken =
                new RefreshToken(null, user, TOKEN, null, IP, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now(), USER_AGENT);

        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.EXPIRED);
    }

    @Test
    @DisplayName("리프레시 토큰 - 회전")
    void refreshTokenRotation() {
        final String NEW_TOKEN = "NEW_TOKEN";
        final OffsetDateTime issuedAt = OffsetDateTime.now();
        final OffsetDateTime expiredAt = issuedAt.plusDays(1);

        var renewedToken = refreshToken.rotate(NEW_TOKEN, issuedAt, expiredAt, refreshToken.getIp(),
                refreshToken.getUserAgent());

        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.EXPIRED);

        assertThat(renewedToken.getTokenHash()).isEqualTo(NEW_TOKEN);
        assertThat(renewedToken.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(renewedToken.getExpiredAt()).isEqualTo(expiredAt);
    }

    @Test
    @DisplayName("리프레시 토큰 - 취소")
    void refreshTokenRevoke() {
        refreshToken.revoke();

        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.EXPIRED);
        assertThat(refreshToken.getRevokedAt()).isNotNull();
    }

    @Test
    @DisplayName("리프레시 토큰 - 취소 - 이미 취소된 상태에서 다시 취소할 경우 예외 발생")
    void refreshTokenRevokeThrowException() {
        refreshToken =
                new RefreshToken(null, user, TOKEN, null, IP, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now(), USER_AGENT);

        assertThatThrownBy(refreshToken::revoke).isInstanceOf(IllegalStateException.class);
    }
}
