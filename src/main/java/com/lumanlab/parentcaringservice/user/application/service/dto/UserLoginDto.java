package com.lumanlab.parentcaringservice.user.application.service.dto;

public record UserLoginDto(String accessToken, String refreshToken, Long refreshTokenExpiredAt) {
}
