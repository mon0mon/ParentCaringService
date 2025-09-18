package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

import java.time.OffsetDateTime;

public interface UpdateRefreshToken {
    void generate(Long userId, String tokenHash, String ip, String userAgent, OffsetDateTime issuedAt,
                  OffsetDateTime expiredAt);

    void rotate(Long userId, String oldToken, String renewedTokenHash, String ip, String userAgent,
                OffsetDateTime issuedAt, OffsetDateTime expiredAt);

    void revoke(Long userId, String token);
}
