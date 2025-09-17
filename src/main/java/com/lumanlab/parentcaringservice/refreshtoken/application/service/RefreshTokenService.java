package com.lumanlab.parentcaringservice.refreshtoken.application.service;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshTokenStatus;
import com.lumanlab.parentcaringservice.refreshtoken.port.inp.QueryRefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.inp.UpdateRefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenRepository;
import com.lumanlab.parentcaringservice.security.encoder.RefreshTokenEncoder;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService implements QueryRefreshToken, UpdateRefreshToken {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenEncoder refreshTokenEncoder;

    @Override
    public List<RefreshToken> findByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        return refreshTokenRepository.findByUser(user);
    }

    @Override
    public RefreshToken findByUserAndToken(Long userId, String token) {
        User user = userRepository.findById(userId).orElseThrow();

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUser(user)
                .stream()
                .filter(item -> item.getStatus().equals(RefreshTokenStatus.ACTIVE))
                .toList();

        return activeTokens.stream()
                .filter(item -> refreshTokenEncoder.matches(token, item.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not valid."));
    }

    @Override
    public List<RefreshToken> findByUserAndStatus(Long userId, RefreshTokenStatus status, OffsetDateTime time) {
        User user = userRepository.findById(userId).orElseThrow();
        // 주어진 시간이 없을 경우, 현재 시간을 기준으로 조회
        OffsetDateTime targetTime = time == null ? OffsetDateTime.now() : time;

        // 주어진 상태가 없을 경우 전체 조회
        if (status == null) {
            return refreshTokenRepository.findByUser(user);
        }

        // 주어진 상태에 따라 개별 조회 쿼리 실행
        return switch (status) {
            case ACTIVE -> refreshTokenRepository.findActiveTokensByUser(user, targetTime);
            case EXPIRED -> refreshTokenRepository.findExpiredTokensByUser(user, targetTime);
        };
    }

    @Override
    public void generate(Long userId, String ip, String userAgent) {
        User user = userRepository.findById(userId).orElseThrow();
        RefreshTokenDto dto = refreshTokenProvider.generateRefreshToken();

        refreshTokenRepository.save(
                new RefreshToken(user, dto.tokenHash(), ip, userAgent, dto.issuedAt(), dto.expiredAt())
        );
    }

    @Override
    public void rotate(Long userId, String token) {
        RefreshToken refreshToken = findByUserAndToken(userId, token);

        if (refreshToken == null) {
            throw new NoSuchElementException("Refresh token is not found.");
        }

        RefreshTokenDto dto = refreshTokenProvider.generateRefreshToken();

        refreshTokenRepository.save(refreshToken.rotate(dto.tokenHash(), dto.issuedAt(), dto.expiredAt()));
    }

    @Override
    public void revoke(Long userId, String token) {
        RefreshToken refreshToken = findByUserAndToken(userId, token);

        if (refreshToken == null) {
            throw new NoSuchElementException("Refresh token is not found.");
        }

        refreshToken.revoke();
    }
}
