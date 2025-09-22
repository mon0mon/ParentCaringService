package com.lumanlab.parentcaringservice.security;

import com.lumanlab.parentcaringservice.security.domain.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class UserContext {

    /**
     * 현재 인증된 사용자의 ID를 반환하는 메서드
     * <p>
     * 인증된 사용자가 없거나 인증 정보가 유효하지 않은 경우 빈 Optional 반환
     *
     * @return 현재 인증된 사용자의 ID를 포함하는 Optional 객체
     * 인증된 사용자가 없으면 Optional.empty() 반환
     */
    public Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                StringUtils.hasText(authentication.getName()) && !"anonymousUser".equals(authentication.getName())) {

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            return Optional.of(principal.id());
        }

        return Optional.empty();
    }

    /**
     * 현재 사용자가 인증되었는지 확인하는 메서드
     * <p>
     * 인증된 사용자가 있는 경우 true를 반환하고, 없는 경우 false를 반환
     *
     * @return 사용자가 인증된 상태라면 true, 그렇지 않다면 false
     */
    public boolean isAuthenticated() {
        return getCurrentUserId().isPresent();
    }
}
