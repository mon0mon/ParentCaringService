package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshTokenStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface QueryRefreshToken {
    List<RefreshToken> findByUser(Long userId);
    RefreshToken findByUserAndToken(Long userId, String token);
    List<RefreshToken> findByUserAndStatus(Long userId, RefreshTokenStatus status, OffsetDateTime time);
}
