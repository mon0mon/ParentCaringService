package com.lumanlab.parentcaringservice.user.domain;

/** 유저 역할 **/
public enum UserRole {
    PARENT, // 보호자
    ADMIN, // 파트너 어드민
    MASTER; // 루먼랩 어드민

    /**
     * 사용자가 슈퍼 유저(Super User)인지 여부를 확인
     * ADMIN 또는 MASTER 역할일 경우 true를 반환
     *
     * @return 사용자가 슈퍼 유저이면 true, 그렇지 않으면 false
     */
    public boolean isSuperUser() {
        return this == ADMIN || this == MASTER;
    }
}
