package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshTokenStatus;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenRepository;
import com.lumanlab.parentcaringservice.security.encoder.RefreshTokenEncoder;
import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UpdateRefreshTokenTest extends BaseUsecaseTest {

    @Autowired
    UpdateRefreshToken updateRefreshToken;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    @MockitoSpyBean
    RefreshTokenEncoder refreshTokenEncoder;

    User user;
    RefreshToken refreshToken;

    final String EMAIL = "john.doe@example.com";
    final String PASSWORD = "PASSWORD";
    final String TOKEN = "TOKEN_HASH";
    final String IP = "127.0.0.1";
    final String USER_AGENT = "Chrome/80.0.3987.132";

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(EMAIL, PASSWORD, UserRole.PARENT));

        refreshToken = refreshTokenRepository.save(
                new RefreshToken(user, refreshTokenEncoder.encode(TOKEN), IP, USER_AGENT, OffsetDateTime.now(),
                        OffsetDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성")
    void generateRefreshToken() {
        final String ENCODED_TOKEN = "ENCODED_TOKEN_HASH";

        // 이 테스트에서만 Mock 동작 설정
        doReturn(ENCODED_TOKEN).when(refreshTokenEncoder).encode(any(String.class));
        doReturn(true).when(refreshTokenEncoder).matches(eq(TOKEN), eq(ENCODED_TOKEN));
        doReturn(false).when(refreshTokenEncoder).matches(argThat(token -> !TOKEN.equals(token)), any(String.class));

        refreshTokenRepository.deleteAll();
        refreshTokenRepository.flush();

        updateRefreshToken.generate(user.getId(), IP, USER_AGENT);

        RefreshToken actual = refreshTokenRepository.findByUser(user).stream().findFirst().orElse(null);

        assertThat(actual).isNotNull();
        assertThat(refreshTokenEncoder.matches(TOKEN, actual.getTokenHash())).isTrue();
        assertThat(actual.getUser()).isEqualTo(user);
        assertThat(actual.getIssuedAt()).isNotNull();
        assertThat(actual.getExpiredAt()).isNotNull();
        assertThat(actual.getIssuedAt().isBefore(OffsetDateTime.now())).isTrue();
        assertThat(actual.getExpiredAt().isAfter(OffsetDateTime.now())).isTrue();
        assertThat(actual.getIssuedAt().isBefore(actual.getExpiredAt())).isTrue();
    }

    @Test
    @DisplayName("리프레시 토큰 - 생성 - 존재하지 않는 유저로 생성 시 예외 발생")
    void generateRefreshTokenThrowException() {
        var nonExistId = Long.MAX_VALUE;

        assertThatThrownBy(() -> updateRefreshToken.generate(nonExistId, IP, USER_AGENT))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 회전")
    void rotateRefreshToken() {
        updateRefreshToken.rotate(user.getId(), TOKEN);

        var actual = refreshTokenRepository.findByUser(user);
        var actualActiveToken = actual.stream()
                .filter(token -> token.getStatus() == RefreshTokenStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        var actualExpiredTokens =
                actual.stream()
                        .filter(token -> token.getStatus() == RefreshTokenStatus.EXPIRED)
                        .findFirst()
                        .orElse(null);

        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.EXPIRED);
        assertThat(refreshToken.getRevokedAt()).isNotNull();

        assertThat(actual.size()).isEqualTo(2);

        assertThat(actualActiveToken).isNotNull();
        assertThat(actualActiveToken).isNotEqualTo(refreshToken);
        assertThat(actualActiveToken.getRotatedFrom()).isEqualTo(refreshToken);

        assertThat(actualExpiredTokens).isNotNull();
        assertThat(actualExpiredTokens).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 - 회전 - 리프레시 토큰이 없는 경우 예외 발생")
    void rotateRefreshTokenThrowException() {
        refreshTokenRepository.deleteAll();
        refreshTokenRepository.flush();

        assertThatThrownBy(() -> updateRefreshToken.rotate(user.getId(), TOKEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("리프레시 토큰 - 취소")
    void revokeRefreshToken() {
        updateRefreshToken.revoke(user.getId(), TOKEN);

        var actual = refreshTokenRepository.findByUser(user);
        var actualExpiredToken =
                actual.stream()
                        .filter(token -> token.getStatus() == RefreshTokenStatus.EXPIRED)
                        .findFirst()
                        .orElse(null);

        assertThat(refreshToken.getStatus()).isEqualTo(RefreshTokenStatus.EXPIRED);
        assertThat(refreshToken.getRevokedAt()).isNotNull();

        assertThat(actualExpiredToken).isNotNull();
        assertThat(actualExpiredToken).isEqualTo(refreshToken);
    }
}
