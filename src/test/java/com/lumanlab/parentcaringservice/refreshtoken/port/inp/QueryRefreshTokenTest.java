package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshTokenStatus;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenRepository;
import com.lumanlab.parentcaringservice.security.encoder.RefreshTokenEncoder;
import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryRefreshTokenTest extends BaseUsecaseTest {

    final String EMAIL = "john.doe@example.com";
    final String PASSWORD = "PASSWORD";
    final String TOKEN = "TOKEN_HASH";
    final String EXPIRED_TOKEN = "EXPIRED_TOKEN_HASH";
    final String REVOKED_TOKEN = "REVOKED_TOKEN_HASH";
    final String IP = "127.0.0.1";
    final UserAgent USER_AGENT = UserAgent.MOBILE;
    @Autowired
    QueryRefreshToken queryRefreshToken;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenEncoder refreshTokenEncoder;
    User user;
    RefreshToken refreshToken;
    RefreshToken expiredRefreshToken;
    RefreshToken revokedRefreshToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT)));

        // 활성 상태 리프레시 토큰
        refreshToken = refreshTokenRepository.save(
                new RefreshToken(user, refreshTokenEncoder.encode(TOKEN), IP, USER_AGENT, OffsetDateTime.now(),
                        OffsetDateTime.now().plusDays(1)));

        // 유효기간이 만료된 리프레시 토큰
        expiredRefreshToken = refreshTokenRepository.save(
                new RefreshToken(user, refreshTokenEncoder.encode(EXPIRED_TOKEN), IP, USER_AGENT,
                        OffsetDateTime.now().minusDays(1), OffsetDateTime.now().minusSeconds(1)));

        // 취소된 리프레시 토큰
        revokedRefreshToken = refreshTokenRepository.save(
                new RefreshToken(null, user, refreshTokenEncoder.encode(REVOKED_TOKEN), null, IP,
                        OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().minusSeconds(1), USER_AGENT));
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저로 조회")
    void queryRefreshTokenByUser() {
        var actual = queryRefreshToken.findByUser(user.getId());
        var actualActiveToken = actual.stream()
                .filter(token -> token.getStatus() == RefreshTokenStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        var actualExpiredTokens =
                actual.stream().filter(token -> token.getStatus() == RefreshTokenStatus.EXPIRED).toList();

        assertThat(actual.size()).isEqualTo(3);

        assertThat(actualActiveToken).isNotNull();
        assertThat(refreshTokenEncoder.matches(TOKEN, actualActiveToken.getTokenHash())).isTrue();
        assertThat(actualActiveToken.getUser()).isEqualTo(user);
        assertThat(actualActiveToken.getIssuedAt()).isEqualTo(refreshToken.getIssuedAt());
        assertThat(actualActiveToken.getExpiredAt()).isEqualTo(refreshToken.getExpiredAt());

        assertThat(actualExpiredTokens).isNotEmpty();
        assertThat(actualExpiredTokens).containsExactlyInAnyOrder(expiredRefreshToken, revokedRefreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저로 조회 - 리프레시 토큰을 발급하지 않은 유저로 조회 NULL")
    void queryRefreshTokenByUserThrowException() {
        var user2 = userRepository.save(new User("email", "PASSWORD", Set.of(UserRole.PARENT)));

        var actual = queryRefreshToken.findByUser(user2.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저ID와 토큰으로 조회")
    void queryRefreshTokenByToken() {
        var actual = queryRefreshToken.findByUserAndToken(user.getId(), TOKEN);

        assertThat(actual).isNotNull();
        assertThat(refreshTokenEncoder.matches(TOKEN, actual.getTokenHash())).isTrue();
        assertThat(actual.getUser()).isEqualTo(user);
        assertThat(actual.getIssuedAt()).isEqualTo(refreshToken.getIssuedAt());
        assertThat(actual.getExpiredAt()).isEqualTo(refreshToken.getExpiredAt());
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저ID와 토큰으로 조회 - 리프레시 토큰을 발급하지 않은 유저로 조회")
    void queryRefreshTokenByTokenThrowException1() {
        refreshTokenRepository.deleteAll();

        assertThatThrownBy(() -> queryRefreshToken.findByUserAndToken(user.getId(),
                refreshTokenEncoder.encode(TOKEN))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저ID와 토큰으로 조회 - 리프레시 토큰이 일치하지 않는 경우")
    void queryRefreshTokenByTokenThrowException2() {
        var nonExistToken = refreshTokenEncoder.encode("NON_EXIST_TOKEN");

        assertThatThrownBy(() -> queryRefreshToken.findByUserAndToken(user.getId(), nonExistToken)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저와 상태로 조회 - 전체 조회")
    void queryRefreshTokenByUserAndStatus1() {
        var actual = queryRefreshToken.findByUserAndStatus(user.getId(), null, OffsetDateTime.now());

        assertThat(actual.size()).isEqualTo(3);
        assertThat(actual).containsExactlyInAnyOrder(refreshToken, expiredRefreshToken, revokedRefreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저와 상태로 조회 - ACTIVE 조회")
    void queryRefreshTokenByUserAndStatus2() {
        var actual =
                queryRefreshToken.findByUserAndStatus(user.getId(), RefreshTokenStatus.ACTIVE, OffsetDateTime.now());

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual).containsExactlyInAnyOrder(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 - 유저와 상태로 조회 - EXPIRED 조회")
    void queryRefreshTokenByUserAndStatus3() {
        var actual =
                queryRefreshToken.findByUserAndStatus(user.getId(), RefreshTokenStatus.EXPIRED, OffsetDateTime.now());

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).containsExactlyInAnyOrder(expiredRefreshToken, revokedRefreshToken);
    }
}
