package com.lumanlab.parentcaringservice.user.port.inp;

import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.domain.UserStatus;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateUserTest extends BaseUsecaseTest {

    final String EMAIL = "jhon.doe@example.com";
    final String PASSWORD = "PASSWORD";
    final String NEW_EMAIL = "foo.bar@example.com";
    final String NEW_PASSWORD = "NEW_PASSWORD";
    final String TOTP_SECRET = "TOTP_SECRET";
    @Autowired
    UpdateUser updateUser;
    @Autowired
    UserRepository userRepository;
    User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT)));
    }

    @Test
    @DisplayName("유저 - 등록 - PARENT 역할 유저")
    void registerUserRoleParent() {
        updateUser.register(NEW_EMAIL, NEW_PASSWORD, Set.of(UserRole.PARENT), null);

        var user = userRepository.findByEmail(NEW_EMAIL).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.PARENT);
        assertThat(user.getMfaEnabled()).isFalse();
    }

    @Test
    @DisplayName("유저 - 등록 - ADMIN 역할 유저")
    void registerUserRoleAdmin() {
        updateUser.register(NEW_EMAIL, NEW_PASSWORD, Set.of(UserRole.ADMIN), TOTP_SECRET);

        var user = userRepository.findByEmail(NEW_EMAIL).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.ADMIN);
        assertThat(user.getMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("유저 - 등록 - MASTER 역할 유저")
    void registerUserRoleMaster() {
        updateUser.register(NEW_EMAIL, NEW_PASSWORD, Set.of(UserRole.MASTER), TOTP_SECRET);

        var user = userRepository.findByEmail(NEW_EMAIL).orElseThrow();

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.MASTER);
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
    @DisplayName("유저 - 회원 탈퇴")
    void withdraw() {
        updateUser.withdraw(user.getId());

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("유저 - TOTP 수정")
    void updateTotp() {
        final String NEW_TOTP_SECRET = "NEW_TOTP_SECRET";

        updateUser.updateTotp(user.getId(), NEW_TOTP_SECRET);

        assertThat(user.getTotpSecret()).isEqualTo(NEW_TOTP_SECRET);
        assertThat(user.getMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("유저 - TOTP 삭제")
    void clearTotp() {
        user = userRepository.save(new User(NEW_EMAIL, NEW_PASSWORD, Set.of(UserRole.PARENT), TOTP_SECRET));

        updateUser.clearTotp(user.getId());

        assertThat(user.getTotpSecret()).isNull();
        assertThat(user.getMfaEnabled()).isFalse();
    }
}
