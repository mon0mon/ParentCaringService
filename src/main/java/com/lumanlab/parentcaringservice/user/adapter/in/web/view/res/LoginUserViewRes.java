package com.lumanlab.parentcaringservice.user.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;

public record LoginUserViewRes(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {

    public LoginUserViewRes(UserLoginDto dto) {
        this(dto.accessToken(), dto.refreshToken(), dto.refreshTokenExpiredAt());
    }
}
