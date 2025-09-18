package com.lumanlab.parentcaringservice.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    final String EMAIL = "john.doe@example.com";
    final String PASSWORD = "password";

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
    @DisplayName("유저 - 생성 - ADMIN")
    void testInitUserAdmin() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.ADMIN));

        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.ADMIN);
    }

    @Test
    @DisplayName("유저 - 생성 - MASTER")
    void testInitUserMaster() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.MASTER));

        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.MASTER);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 이메일 NULL")
    void testInitUserEmailNull() {
        assertThatThrownBy(() -> new User(null, PASSWORD, Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 이메일 공백")
    void testInitUserEmailBlank() {
        assertThatThrownBy(() -> new User("", PASSWORD, Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 비밀번호 NULL")
    void testInitUserPasswordNull() {
        assertThatThrownBy(() -> new User(EMAIL, null, Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 비밀번호 공백")
    void testInitUserPasswordBlank() {
        assertThatThrownBy(() -> new User(EMAIL, "", Set.of(UserRole.PARENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 생성 오류 - 역할 NULL")
    void testInitUserRoleNull() {
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
    void testUpdatePassword() {
        String newPassword = "";

        assertThatThrownBy(() -> user.updatePassword(newPassword))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정 - 예외 발생 (NULL 비밀번호 업데이트)")
    void testUpdatePasswordNull() {
        assertThatThrownBy(() -> user.updatePassword(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
