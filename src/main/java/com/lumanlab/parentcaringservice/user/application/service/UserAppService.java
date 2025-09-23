package com.lumanlab.parentcaringservice.user.application.service;

import com.lumanlab.parentcaringservice.exception.*;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.oauth2.port.inp.QueryOAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.port.inp.UpdateOAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.port.outp.OAuth2Client;
import com.lumanlab.parentcaringservice.oauth2.port.outp.UserProfileResponse;
import com.lumanlab.parentcaringservice.refreshtoken.application.service.RefreshTokenService;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.security.jwt.application.service.JwtTokenService;
import com.lumanlab.parentcaringservice.totp.application.service.NonceService;
import com.lumanlab.parentcaringservice.totp.application.service.TotpProvider;
import com.lumanlab.parentcaringservice.totp.application.service.TotpService;
import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;
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

import java.util.Map;
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
    private final NonceService nonceService;
    private final TotpProvider totpProvider;
    private final TotpService totpService;
    private final OAuth2Client oAuth2GoogleProfileClient;
    private final QueryOAuth2Link queryOAuth2Link;
    private final UpdateOAuth2Link updateOAuth2Link;

    public void registerUser(String email, String password, UserAgent userAgent) {
        String encodedPassword = passwordEncoder.encode(password);

        switch (userAgent) {
            case UserAgent.MOBILE:
                updateUser.register(email, encodedPassword, Set.of(UserRole.PARENT));
                break;
            case UserAgent.PARTNER_ADMIN:
                updateUser.register(email, encodedPassword, Set.of(UserRole.ADMIN));
                break;
            case UserAgent.LUMANLAB_ADMIN:
                updateUser.register(email, encodedPassword, Set.of(UserRole.MASTER));
                break;
            default:
                throw new IllegalArgumentException("Invalid user agent.");
        }
    }

    public UserLoginDto loginUser(String email, String password, UserAgent userAgent, String ip) {
        User user = queryUser.findByEmail(email);

        // UserAgent와 UserRole이 일치하지 않는 경우 예외처리
        if (!userAgent.isUserAccessible(user.getRoles())) {
            throw new LoginUserRoleNotMatchWithUserAgentException(
                    "USER_ROLE_NOT_MATCH_WITH_USER_AGENT", "User roles is not accessible."
            );
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LoginUserAuthorizationFailedException("USER_AUTHORIZATION_FAILED", "Invalid password.");
        }

        // MFA 초기화가 필요한 경우 nonce를 생성하여 예외와 함께 반환
        if (user.shouldInitializeMfa()) {
            String nonce = nonceService.generateNonce(user.getId());
            throw new MfaInitializationRequiredException("MFA 초기화가 필요합니다.", nonce);
        }

        // MFA 인증이 필요한 경우 nonce를 생성하여 예외와 함께 반환
        if (user.getMfaEnabled()) {
            String nonce = nonceService.generateNonce(user.getId());
            throw new MfaVerificationRequiredException("MFA 인증이 필요합니다.", nonce);
        }

        // 유저 상태에 따라서 예외 발생
        if (!user.isActive()) {
            throw new LoginUserStatusNotActiveException("USER_STATUS_NOT_ACTIVE", "User is not active.");
        }

        // 액세스 토큰 발급
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), Map.of("roles", user.getRolesString()));

        // 리프레시 토큰 발급 및 저장 로직
        RefreshTokenDto refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(user.getId(), Map.of("roles", user.getRolesString()));
        refreshTokenService.generate(user.getId(), refreshTokenDto.tokenHash(), ip, userAgent,
                refreshTokenDto.issuedAt(), refreshTokenDto.expiredAt());

        return new UserLoginDto(accessToken, refreshTokenDto.token(), refreshTokenDto.expiredAt().toEpochSecond());
    }

    public UserLoginDto oAuth2LoginUser(String oAuth2AccessToken, OAuth2Provider provider, UserAgent userAgent,
                                        String ip) {
        var profile = switch (provider) {
            case GOOGLE -> oAuth2GoogleProfileClient.requestProfile(oAuth2AccessToken);
            default -> throw new IllegalArgumentException("Invalid OAuth2 provider.");
        };

        OAuth2Link link = queryOAuth2Link.findByOAuth2IdOrThrow(profile.id());
        User user = link.getUser();

        // MFA 인증이 필요한 경우 nonce를 생성하여 예외와 함께 반환
        if (user.getMfaEnabled()) {
            String nonce = nonceService.generateNonce(user.getId());
            throw new MfaVerificationRequiredException("MFA 인증이 필요합니다.", nonce);
        }

        // 유저 상태에 따라서 예외 발생
        if (!user.isActive()) {
            throw new LoginUserStatusNotActiveException("USER_STATUS_NOT_ACTIVE", "User is not active.");
        }

        // 액세스 토큰 발급
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), Map.of("roles", user.getRolesString()));

        // 리프레시 토큰 발급 및 저장 로직
        RefreshTokenDto refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(user.getId(), Map.of("roles", user.getRolesString()));
        refreshTokenService.generate(user.getId(), refreshTokenDto.tokenHash(), ip, userAgent,
                refreshTokenDto.issuedAt(), refreshTokenDto.expiredAt());

        return new UserLoginDto(accessToken, refreshTokenDto.token(), refreshTokenDto.expiredAt().toEpochSecond());
    }

    public GenerateTotpDto updateUserTotp(Long userId) {
        User user = queryUser.findById(userId);

        return totpService.generateTotp(userId);
    }

    public UserLoginDto verifyUserTotp(String nonce, Integer totpVerificationCode, UserAgent userAgent, String ip) {
        Long userId = nonceService.getUserIdByNonce(nonce);
        User user = queryUser.findById(userId);

        // User가 MFA 인증이 켜져있지 않으면 예외 발생
        if (!user.getMfaEnabled()) {
            throw new IllegalArgumentException("MFA is not enabled.");
        }

        // 인증 코드 검증이 실패한 경우 예외 발생
        if (!totpProvider.verifyTotp(user.getTotpSecret(), totpVerificationCode)) {
            throw new MfaVerificationFailedException("Invalid TOTP code.");
        }

        // 액세스 토큰 발급
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), Map.of("roles", user.getRolesString()));

        // 리프레시 토큰 발급 및 저장 로직
        RefreshTokenDto refreshTokenDto =
                refreshTokenProvider.generateRefreshToken(user.getId(), Map.of("roles", user.getRolesString()));
        refreshTokenService.generate(user.getId(), refreshTokenDto.tokenHash(), ip, userAgent,
                refreshTokenDto.issuedAt(), refreshTokenDto.expiredAt());

        return new UserLoginDto(accessToken, refreshTokenDto.token(), refreshTokenDto.expiredAt().toEpochSecond());
    }

    public void linkOAuth2(Long userId, OAuth2Provider provider, String oAuth2AccessToken) {
        User user = queryUser.findById(userId);

        UserProfileResponse profile = switch (provider) {
            case GOOGLE -> oAuth2GoogleProfileClient.requestProfile(oAuth2AccessToken);
            default -> throw new IllegalArgumentException("Invalid OAuth2 provider.");
        };

        updateOAuth2Link.register(user.getId(), provider, profile.id());
    }

    public void unlinkOAuth2(Long userId, OAuth2Provider provider) {
        User user = queryUser.findById(userId);

        updateOAuth2Link.delete(user.getId(), provider);
    }
}
