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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateOAuth2LinkTest extends BaseUsecaseTest {

    private final String EMAIL = "user@example.com";
    private final String PASSWORD = "password";
    private final String OAUTH2_ID = "OAUTH2_ID";
    private final OAuth2Provider OAUTH2_PROVIDER = OAuth2Provider.GOOGLE;
    private User user;
    @Autowired
    private UpdateOAuth2Link updateOAuth2Link;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OAuth2LinkRepository oAuth2LinkRepository;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT)));

        OAuth2Link oAuth2Link = oAuth2LinkRepository.save(new OAuth2Link(user, OAUTH2_PROVIDER, OAUTH2_ID));
        user.addOAuth2Link(oAuth2Link);
    }

    @Test
    @DisplayName("OAuth2Link 등록")
    void register() {
        userRepository.deleteAll();
        oAuth2LinkRepository.deleteAll();
        user = userRepository.save(new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT)));

        updateOAuth2Link.register(user.getId(), OAUTH2_PROVIDER, OAUTH2_ID);

        var actual = oAuth2LinkRepository.findByOAuth2Id(OAUTH2_ID).orElseThrow();

        assertThat(user.getOAuth2Links()).isNotEmpty();
        assertThat(actual.getUser()).isEqualTo(user);
        assertThat(actual.getProvider()).isEqualTo(OAUTH2_PROVIDER);
        assertThat(actual.getOAuth2Id()).isEqualTo(OAUTH2_ID);
    }

    @Test
    @DisplayName("OAuth2Link 등록 - 등록된 OAuth2 Provider로 등록 시 예외 발생")
    void registerAlreadyRegisteredOAuth2ProviderThrowException() {
        assertThatThrownBy(() -> updateOAuth2Link.register(user.getId(), OAUTH2_PROVIDER, OAUTH2_ID))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("OAuth2Link 삭제")
    void deleteOAuth2Link() {
        updateOAuth2Link.delete(user.getId(), OAUTH2_PROVIDER);

        Optional<OAuth2Link> found = oAuth2LinkRepository.findByOAuth2Id(OAUTH2_ID);

        assertThat(found).isEmpty();
        assertThat(user.getOAuth2Links()).isEmpty();
    }

    @Test
    @DisplayName("OAuth2Link 삭제 - 존재하지 않는 OAuth2 Provider로 삭제 시 예외 발생")
    void deleteOAuth2LinkNotRegisteredOAuth2ProviderThrowException() {
        userRepository.deleteAll();
        oAuth2LinkRepository.deleteAll();
        user = userRepository.save(new User(EMAIL, PASSWORD, Set.of(UserRole.PARENT)));

        assertThatThrownBy(() -> updateOAuth2Link.delete(user.getId(), OAUTH2_PROVIDER))
                .isInstanceOf(NoSuchElementException.class);
    }
}
