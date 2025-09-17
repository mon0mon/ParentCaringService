package com.lumanlab.parentcaringservice.user.port.inp;

import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

class QueryUserTest extends BaseUsecaseTest {

    @Autowired
    QueryUser queryUser;

    @Autowired
    UserRepository userRepository;

    User user;

    final String EMAIL = "jhon.doe@example.com";
    final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(EMAIL, PASSWORD, UserRole.PARENT));
    }

    @Test
    @DisplayName("유저 조회 - 이메일로 조회")
    void testFindByEmail() {
        var actual = queryUser.findByEmail(EMAIL);

        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("유저 조회 - 이메일로 조회 - 없는 이메일로 조회 시 예외 발생")
    void testFindByEmailThrowException() {
        final String RANDOM_EMAIL = "RANDOM_EMAIL";

        assertThatThrownBy(() -> queryUser.findByEmail(RANDOM_EMAIL))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("유저 조회 - ID로 조회")
    void testFindById() {
        var actual = queryUser.findById(user.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("유저 조회 - ID로 조회 - 없는 ID로 조회 시 예외 발생")
    void testFindByIdThrowException() {
        var nonExistId = Long.MAX_VALUE;

        assertThatThrownBy(() -> queryUser.findById(nonExistId))
                .isInstanceOf(NoSuchElementException.class);
    }
}
