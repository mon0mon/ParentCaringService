package com.lumanlab.parentcaringservice.token.application.service.dto;

import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;

public record RefreshAccessTokenDto(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {

    public RefreshAccessTokenDto(String accessToken, RefreshTokenDto refreshTokenDto) {
        this(accessToken, refreshTokenDto.token(), refreshTokenDto.expiredAt().toEpochSecond());
    }
}
