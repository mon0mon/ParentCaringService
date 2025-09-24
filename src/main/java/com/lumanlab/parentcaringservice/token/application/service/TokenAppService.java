package com.lumanlab.parentcaringservice.token.application.service;

import com.lumanlab.parentcaringservice.refreshtoken.port.inp.UpdateRefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.token.application.service.dto.RefreshAccessTokenDto;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenAppService {

    private final JwtTokenService jwtTokenService;
    private final UpdateRefreshToken updateRefreshToken;
    private final RefreshTokenProvider refreshTokenProvider;
    private final QueryUser queryUser;

    public RefreshAccessTokenDto refreshAccessToken(String refreshToken, UserAgent userAgent, String ip) {
        // 현재 refreshToken이 유효한지 조회
        Long userId = Long.valueOf(jwtTokenService.extractSubject(refreshToken));
        // 실제로 존재하는 유저인지 체크
        User user = queryUser.findById(userId);

        // 신규 accessToken 생성
        String accessToken = jwtTokenService.generateAccessToken(userId, Map.of("roles", user.getRolesString()));

        // 신규 refreshToken 생성
        RefreshTokenDto refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(user.getId(), Map.of("roles", user.getRolesString()));
        updateRefreshToken.rotate(user.getId(), refreshToken, refreshTokenDto.tokenHash(), ip, userAgent,
                refreshTokenDto.issuedAt(), refreshTokenDto.expiredAt());

        return new RefreshAccessTokenDto(accessToken, refreshTokenDto);
    }
}
