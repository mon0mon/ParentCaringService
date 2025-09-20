package com.lumanlab.parentcaringservice.user.port.inp.web.view.res;

import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;

public record VerifyUserTotpViewRes(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {

    public VerifyUserTotpViewRes(UserLoginDto dto) {
        this(dto.accessToken(), dto.refreshToken(), dto.refreshTokenExpiredAt());
    }
}
