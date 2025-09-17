package com.lumanlab.parentcaringservice.security;

import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenDto;
import com.lumanlab.parentcaringservice.refreshtoken.port.outp.RefreshTokenProvider;
import com.lumanlab.parentcaringservice.security.encoder.RefreshTokenEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultRefreshTokenProvider implements RefreshTokenProvider {

    private final RefreshTokenEncoder refreshTokenEncoder;

    /**
     * 리프레시 토큰을 생성하는 메서드
     *
     * @return RefreshTokenDto 객체로, 생성된 토큰, 발급 시점, 만료 시점을 포함함
     */
    public RefreshTokenDto generateRefreshToken() {
        // TODO 추후에 issuedAt, expiredAt은 properties에서 기간을 주입받는 것으로 변경할 것
        String token = UUID.randomUUID().toString();
        OffsetDateTime issuedAt = OffsetDateTime.now();
        OffsetDateTime expiredAt = issuedAt.plusDays(1);

        return new RefreshTokenDto(generateHashedToken(token), issuedAt, expiredAt);
    }

    /**
     * 주어진 토큰 문자열을 해싱하여 반환하는 메서드
     *
     * @param token 해싱할 원본 토큰 문자열
     * @return 해싱된 토큰 문자열
     */
    private String generateHashedToken(String token) {
        return refreshTokenEncoder.encode(token);
    }
}
