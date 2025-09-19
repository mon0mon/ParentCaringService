package com.lumanlab.parentcaringservice.user.application.service;

import com.lumanlab.parentcaringservice.refreshtoken.application.service.RefreshTokenService;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import com.lumanlab.parentcaringservice.user.port.inp.UpdateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAppService {

    private final QueryUser queryUser;
    private final UpdateUser updateUser;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public void registerUser(String email, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        updateUser.register(email, encodedPassword, Set.of(UserRole.PARENT), null);
    }

    public UserLoginDto loginUser(String email, String password, UserAgent userAgent, String ip) {
        User user = queryUser.findByEmail(email);

        // TODO 추후 커스텀 예외로 처리할 것
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password.");
        }

        // 액세스 토큰 발급
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), null);

        // 리프레시 토큰 발급 및 저장 로직
        RefreshTokenDto refreshTokenDto = refreshTokenProvider.generateRefreshToken(user.getId(), null);
        refreshTokenService.generate(user.getId(), refreshTokenDto.tokenHash(), ip, userAgent,
                refreshTokenDto.issuedAt(), refreshTokenDto.expiredAt());

        return new UserLoginDto(accessToken, refreshTokenDto.token(), refreshTokenDto.expiredAt().toEpochSecond());
    }
}
