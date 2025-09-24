package com.lumanlab.parentcaringservice.security.oauth2;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 커스텀 OAuth2 User 클래스
 * OAuth2 User 정보를 확장하여 사용자 정보를 추가로 저장
 */
public record CustomOAuth2User(OAuth2Provider provider, Long userId, Collection<UserRole> roles, String oAuth2Id,
                               String accessToken, OAuth2User oAuth2User) implements OAuth2User {

    public boolean isAnonymous() {
        return userId == null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (userId != null && roles != null && !roles.isEmpty()) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        }

        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2Id;
    }
}
