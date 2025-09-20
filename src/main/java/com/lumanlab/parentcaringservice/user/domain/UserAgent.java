package com.lumanlab.parentcaringservice.user.domain;

import java.util.Set;

/** 사용자 접속 클라이언트 **/
public enum UserAgent {
    MOBILE, // 모바일 앱(보호자)
    PARTNER_ADMIN, // 웹(파트너 어드민 콘솔)
    LUMANLAB_ADMIN; // 웹(루먼랩 어드민 콘솔)

    /**
     * 주어진 사용자 역할 집합을 기반으로 현재 사용자 에이전트가 접근 가능한지 여부를 확인
     *
     * @param userRoles 사용자의 역할 집합
     * @return 사용자 에이전트가 접근 가능하면 true, 그렇지 않으면 false
     */
    public boolean isUserAccessible(Set<UserRole> userRoles) {
        var isAccessible = false;

        if (this == MOBILE) {
            isAccessible = userRoles.contains(UserRole.PARENT);
        } else if (this == PARTNER_ADMIN) {
            isAccessible = userRoles.contains(UserRole.ADMIN);
        } else if (this == LUMANLAB_ADMIN) {
            isAccessible = userRoles.contains(UserRole.MASTER);
        }

        return isAccessible;
    }
}
