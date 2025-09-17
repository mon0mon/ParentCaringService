package com.lumanlab.parentcaringservice.user.port.inp;

import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.domain.UserStatus;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class UpdateUserTest extends BaseUsecaseTest {

    @Autowired
    UpdateUser updateUser;

    @Autowired
    UserRepository userRepository;

    User user;

    final String EMAIL = "jhon.doe@example.com";
    final String PASSWORD = "PASSWORD";

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(EMAIL, PASSWORD, UserRole.PARENT));
    }

    @Test
    @DisplayName("유저 - 등록 - PARENT 역할 유저")
    void registerUserRoleParent() {
        final String NEW_EMAIL = "foo.bar@example.com";
        final String NEW_PASSWORD = "NEW_PASSWORD";

        updateUser.register(NEW_EMAIL, NEW_PASSWORD, UserRole.PARENT);

        var user = userRepository.findByEmail(NEW_EMAIL).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
        assertThat(user.getRole()).isEqualTo(UserRole.PARENT);
        assertThat(user.getMfaEnabled()).isFalse();
    }

    @Test
    @DisplayName("유저 - 등록 - ADMIN 역할 유저")
    void registerUserRoleAdmin() {
        final String NEW_EMAIL = "foo.bar@example.com";
        final String NEW_PASSWORD = "NEW_PASSWORD";

        updateUser.register(NEW_EMAIL, NEW_PASSWORD, UserRole.ADMIN);

        var user = userRepository.findByEmail(NEW_EMAIL).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getMfaEnabled()).isFalse();
    }

    @Test
    @DisplayName("유저 - 등록 - MASTER 역할 유저")
    void registerUserRoleMaster() {
        final String NEW_EMAIL = "foo.bar@example.com";
        final String NEW_PASSWORD = "NEW_PASSWORD";

        updateUser.register(NEW_EMAIL, NEW_PASSWORD, UserRole.MASTER);

        var user = userRepository.findByEmail(NEW_EMAIL).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
        assertThat(user.getRole()).isEqualTo(UserRole.MASTER);
        assertThat(user.getMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정")
    void updatePassword() {
        final String NEW_PASSWORD = "NEW_PASSWORD";

        updateUser.updatePassword(user.getId(), NEW_PASSWORD);

        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정 에러 - NULL")
    void updatePasswordNull() {
        final String NEW_PASSWORD = null;

        assertThatThrownBy(() -> updateUser.updatePassword(user.getId(), NEW_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 비밀번호 수정 에러 - 공백")
    void updatePasswordBlank() {
        final String NEW_PASSWORD = "";

        assertThatThrownBy(() -> updateUser.updatePassword(user.getId(), NEW_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("유저 - 다단계 인증 설정 수정")
    void updateMfaEnabled() {
        updateUser.updateMfaEnabled(user.getId(), true);

        assertThat(user.getMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("유저 - 회원 탈퇴")
    void withdraw() {
        updateUser.withdraw(user.getId());

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }
}
