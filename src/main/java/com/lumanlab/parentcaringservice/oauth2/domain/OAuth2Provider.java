package com.lumanlab.parentcaringservice.oauth2.domain;

/** OAuth2 로그인 제공자 **/
public enum OAuth2Provider {
    GOOGLE; // 구글

    public static OAuth2Provider parse(String provider) {
        return valueOf(provider.toUpperCase());
    }
}
