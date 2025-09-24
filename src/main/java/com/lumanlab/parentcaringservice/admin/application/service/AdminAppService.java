package com.lumanlab.parentcaringservice.admin.application.service;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationType;
import com.lumanlab.parentcaringservice.impersonationlog.port.inp.UpdateImpersonationLog;
import com.lumanlab.parentcaringservice.refreshtoken.application.service.RefreshTokenService;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminAppService {

    private final QueryUser queryUser;
    private final UpdateImpersonationLog updateImpersonationLog;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public UserLoginDto impersonateUser(Long adminUserId, Long impersonateUserId, String ip, UserAgent userAgent) {
        updateImpersonationLog.register(
                adminUserId, impersonateUserId, ip, ImpersonationType.LOGIN, "Admin impersonate user"
        );

        var targetUser = queryUser.findById(impersonateUserId);

        // 액세스 토큰 발급
        String accessToken = jwtTokenService.generateAccessToken(
                targetUser.getId(), Map.of("roles", targetUser.getRolesString(), "impersonatorId", adminUserId)
        );

        // 리프레시 토큰 발급 및 저장 로직
        RefreshTokenDto refreshTokenDto = refreshTokenProvider.generateRefreshToken(
                targetUser.getId(), Map.of("roles", targetUser.getRolesString(), "impersonatorId", adminUserId)
        );
        refreshTokenService.generate(
                targetUser.getId(), refreshTokenDto.tokenHash(), ip, userAgent, refreshTokenDto.issuedAt(),
                refreshTokenDto.expiredAt()
        );

        return new UserLoginDto(accessToken, refreshTokenDto.token(), refreshTokenDto.expiredAt().toEpochSecond());
    }
}
