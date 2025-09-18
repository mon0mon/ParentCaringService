package com.lumanlab.parentcaringservice.refreshtoken.application.service;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshTokenStatus;
import com.lumanlab.parentcaringservice.refreshtoken.port.inp.QueryRefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.inp.UpdateRefreshToken;
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
    public void generate(Long userId, String tokenHash, String ip, String userAgent, OffsetDateTime issuedAt,
                         OffsetDateTime expiredAt) {
        User user = userRepository.findById(userId).orElseThrow();

        refreshTokenRepository.save(new RefreshToken(user, tokenHash, ip, userAgent, issuedAt, expiredAt));
    }

    @Override
    public void rotate(Long userId, String oldToken, String renewedTokenHash, String ip, String userAgent,
                       OffsetDateTime issuedAt,
                       OffsetDateTime expiredAt) {
        RefreshToken refreshToken = findByUserAndToken(userId, oldToken);

        if (refreshToken == null) {
            throw new NoSuchElementException("Refresh token is not found.");
        }

        refreshTokenRepository.save(refreshToken.rotate(renewedTokenHash, issuedAt, expiredAt, refreshToken.getIp(),
                refreshToken.getUserAgent()));
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
