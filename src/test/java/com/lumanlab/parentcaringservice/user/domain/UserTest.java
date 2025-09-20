package com.lumanlab.parentcaringservice.user.domain;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    final String EMAIL = "john.doe@example.com";
    final String PASSWORD = "password";
    final String TOTP_SECRET = "TOTP_SECRET";

    User user;

    @BeforeEach
    void setUp() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT));
    }

    @Test
    @DisplayName("유저 - 생성 확인")
    void initUser() {
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.PARENT);
        assertThat(user.getMfaEnabled()).isFalse();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("유저 - 생성 - TOTP Secret을 미리 등록")
    void testInitUserAdmin() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT), TOTP_SECRET);

        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.PARENT);
        assertThat(user.getTotpSecret()).isEqualTo(TOTP_SECRET);
        assertThat(user.getMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 이메일 NULL")
    void testInitUserEmailNullThrowException() {
        assertThatThrownBy(() -> new User(null, PASSWORD, Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 이메일 공백")
    void testInitUserEmailBlankThrowException() {
        assertThatThrownBy(() -> new User("", PASSWORD, Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 비밀번호 NULL")
    void testInitUserPasswordNullThrowException() {
        assertThatThrownBy(() -> new User(EMAIL, null, Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 비밀번호 공백")
    void testInitUserPasswordBlankThrowException() {
        assertThatThrownBy(() -> new User(EMAIL, "", Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 역할 NULL")
    void testInitUserRoleNullThrowException() {
        assertThatThrownBy(() -> new User(EMAIL, PASSWORD, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정")
    void testUpdatePasswordSuccess() {
        String newPassword = "NEW_PASSWORD";

        user.updatePassword(newPassword);

        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정 - 예외 발생 (공백 비밀번호 업데이트)")
    void testUpdatePasswordBlankException() {
        String newPassword = "";

        assertThatThrownBy(() -> user.updatePassword(newPassword))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정 - 예외 발생 (NULL 비밀번호 업데이트)")
    void testUpdatePasswordNullException() {
        assertThatThrownBy(() -> user.updatePassword(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - TotpSecret 수정")
    void testUpdateTotpSecretSuccess() {
        String newTotpSecret = "NEW_TOTP_SECRET";

        user.updateTotpSecret(newTotpSecret);

        assertThat(user.getTotpSecret()).isEqualTo(newTotpSecret);
        assertThat(user.getMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("유저 - TotpSecret 수정 - 예외 발생 (공백 TotpSecret 업데이트)")
    void testUpdateTotpSecretBlankThrowException() {
        assertThatThrownBy(() -> user.updateTotpSecret(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - TotpSecret 수정 - 예외 발생 (NULL TotpSecret 업데이트)")
    void testUpdateTotpSecretNullThrowException() {
        String newTotpSecret = "";

        assertThatThrownBy(() -> user.updateTotpSecret(newTotpSecret))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - TotpSecret 초기화")
    void testClearTotpSecretSuccess() {
        user.updateTotpSecret(TOTP_SECRET);

        user.clearTotpSecret();

        assertThat(user.getTotpSecret()).isNull();
        assertThat(user.getMfaEnabled()).isFalse();
    }

    @Test
    @DisplayName("유저 - TotpSecret 초기화 - SuperUser일 경우 예외 발생")
    void testClearTotpSecretSuperUserThrowException() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.MASTER), TOTP_SECRET);

        assertThatThrownBy(user::clearTotpSecret)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("유저 - MFA 등록이 필요한지 확인 - PARENT 역할인 경우 FALSE")
    void parentUserShouldMfaEnabled() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT));

        assertThat(user.shouldInitializeMfa()).isFalse();
    }

    @Test
    @DisplayName("유저 - MFA 등록이 필요한지 확인 - ADMIN 역할인 경우 TRUE")
    void adminUserShouldMfaEnabled() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.ADMIN));

        assertThat(user.shouldInitializeMfa()).isTrue();
    }

    @Test
    @DisplayName("유저 - MFA 등록이 필요한지 확인 - MASTER 역할인 경우 FALSE")
    void masterUserShouldMfaEnabled() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.MASTER));

        assertThat(user.shouldInitializeMfa()).isTrue();
    }

    @Test
    @DisplayName("유저 - OAuth2Provider로 연동된 OAuth2Link를 조회")
    void testFindOAuth2LinkByProvider() {
        user.addOAuth2Link(new OAuth2Link(user, OAuth2Provider.GOOGLE, "OAUTH2_ID"));

        var actual = user.getOAuth2Link(OAuth2Provider.GOOGLE);

        assertThat(actual).isNotNull();
    }

    @Test
    @DisplayName("유저 - OAuth2Provider로 연동된 OAuth2Link를 조회 - 존재하지 않은 경우 예외 발생")
    void testFindOAuth2LinkByProviderThrowException() {

        assertThatThrownBy(() -> user.getOAuth2Link(OAuth2Provider.GOOGLE))
                .isInstanceOf(NoSuchElementException.class);
    }
}
