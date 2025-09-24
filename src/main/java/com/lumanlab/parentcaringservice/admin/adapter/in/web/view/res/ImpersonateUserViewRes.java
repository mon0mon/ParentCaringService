package com.lumanlab.parentcaringservice.admin.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;

public record ImpersonateUserViewRes(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {

    public ImpersonateUserViewRes(UserLoginDto dto) {
        this(dto.accessToken(), dto.refreshToken(), dto.refreshTokenExpiredAt());
    }
}
