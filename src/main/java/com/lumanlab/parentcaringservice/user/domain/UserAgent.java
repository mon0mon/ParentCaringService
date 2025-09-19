package com.lumanlab.parentcaringservice.user.domain;

/** 사용자 접속 클라이언트 **/
public enum UserAgent {
    MOBILE, // 모바일 앱(보호자)
    PARTNER_ADMIN, // 웹(파트너 어드민 콘솔)
    LUMANLAB_ADMIN; // 웹(루먼랩 어드민 콘솔)
}
