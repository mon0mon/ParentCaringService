package com.lumanlab.parentcaringservice.security.oauth2;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.oauth2.port.inp.QueryOAuth2Link;
import com.lumanlab.parentcaringservice.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final QueryOAuth2Link queryOAuth2Link;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2Provider provider = OAuth2Provider.parse(userRequest.getClientRegistration().getRegistrationId());
        OAuth2Link findOAuth2Link = queryOAuth2Link.findByOAuth2Id(oAuth2User.getName());

        // 등록되지 않은 사용자의 경우, ROLE_ANONYMOUSE로 로그인 처리
        if (findOAuth2Link == null) {
            log.debug("User is not registered. {}", oAuth2User.getName());
            return new CustomOAuth2User(provider, null, null, oAuth2User.getName(),
                    userRequest.getAccessToken().getTokenValue(), oAuth2User);
        }

        User findUser = findOAuth2Link.getUser();

        // 회원 탈퇴한 유저의 경우 예외 처리
        if (!findUser.isActive()) {
            log.debug("User is not active. {}", oAuth2User.getName());
            throw new IllegalStateException("User is not active.");
        }

        // Roles를 사용하는 곳이 없기 때문에, Hibernate Lazy Initialize 예외를 방지하기 위해 null로 처리
        return new CustomOAuth2User(provider, findUser.getId(), null, oAuth2User.getName(),
                userRequest.getAccessToken().getTokenValue(), oAuth2User);
    }
}
