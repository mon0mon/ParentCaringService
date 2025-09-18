package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;

import java.time.OffsetDateTime;

public interface UpdateRefreshToken {
    RefreshToken generate(Long userId, String token, String ip, String userAgent, OffsetDateTime issuedAt,
                          OffsetDateTime expiredAt);

    void rotate(Long userId, String token, String tokenHash, OffsetDateTime issuedAt, OffsetDateTime expiredAt);

    void revoke(Long userId, String token);
}
