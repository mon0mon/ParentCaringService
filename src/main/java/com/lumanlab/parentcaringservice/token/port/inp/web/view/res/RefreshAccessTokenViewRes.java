package com.lumanlab.parentcaringservice.token.port.inp.web.view.res;

import com.lumanlab.parentcaringservice.token.application.service.dto.RefreshAccessTokenDto;

public record RefreshAccessTokenViewRes(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {

    public RefreshAccessTokenViewRes(RefreshAccessTokenDto dto) {
        this(dto.accessToken(), dto.refreshToken(), dto.refreshTokenExpiredAt());
    }
}
