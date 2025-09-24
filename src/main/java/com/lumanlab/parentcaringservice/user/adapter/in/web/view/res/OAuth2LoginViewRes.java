package com.lumanlab.parentcaringservice.user.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;

public record OAuth2LoginViewRes(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {

    public OAuth2LoginViewRes(UserLoginDto dto) {
        this(dto.accessToken(), dto.refreshToken(), dto.refreshTokenExpiredAt());
    }
}
