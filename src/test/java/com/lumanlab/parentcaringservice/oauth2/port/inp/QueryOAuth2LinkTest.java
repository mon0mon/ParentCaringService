package com.lumanlab.parentcaringservice.oauth2.port.inp;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.oauth2.port.outp.OAuth2LinkRepository;
import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryOAuth2LinkTest extends BaseUsecaseTest {

    private final String EMAIL = "user@example.com";
    private final String PASSWORD = "password";
    private final String OAUTH2_ID = "OAUTH2_ID";
    private final OAuth2Provider OAUTH2_PROVIDER = OAuth2Provider.GOOGLE;
    private User user;
    @Autowired
    private QueryOAuth2Link queryOAuth2Link;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OAuth2LinkRepository oAuth2LinkRepository;

    @BeforeEach
    void setUp() {
        user = new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT));

        userRepository.save(user);
        oAuth2LinkRepository.save(new OAuth2Link(user, OAUTH2_PROVIDER, OAUTH2_ID));
    }

    @Test
    @DisplayName("OAuth2 Link를 OAuth2 ID를 기반으로 조회")
    void queryOAuth2LinkOrThrow() {
        var actual = queryOAuth2Link.findByOAuth2IdOrThrow(OAUTH2_ID);

        assertThat(actual).isNotNull();
        assertThat(actual.getUser()).isEqualTo(user);
        assertThat(actual.getProvider()).isEqualTo(OAUTH2_PROVIDER);
        assertThat(actual.getOAuth2Id()).isEqualTo(OAUTH2_ID);
    }

    @Test
    @DisplayName("OAuth2 Link를 OAuth2 ID를 기반으로 조회 - 존재하지 않은 OAuth2 ID로 조회 시 예외 발생")
    void queryOAuth2LinkOrThrowThrowException() {
        var nonExistId = "NON_EXIST_ID";

        assertThatThrownBy(() -> queryOAuth2Link.findByOAuth2IdOrThrow(nonExistId))
                .isInstanceOf(NoSuchElementException.class);
    }
}
