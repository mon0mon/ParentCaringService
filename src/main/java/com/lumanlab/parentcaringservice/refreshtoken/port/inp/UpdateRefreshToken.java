package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

import com.lumanlab.parentcaringservice.user.domain.UserAgent;

import java.time.OffsetDateTime;

public interface UpdateRefreshToken {
    void generate(Long userId, String tokenHash, String ip, UserAgent userAgent, OffsetDateTime issuedAt,
                  OffsetDateTime expiredAt);

    void rotate(Long userId, String oldToken, String renewedTokenHash, String ip, UserAgent userAgent,
                OffsetDateTime issuedAt, OffsetDateTime expiredAt);

    void revoke(Long userId, String token);
}
