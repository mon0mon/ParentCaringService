package com.lumanlab.parentcaringservice.me.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;

import java.time.OffsetDateTime;

public record QuerySessionsData(Long id, String ip, OffsetDateTime issuedAt, OffsetDateTime expiredAt) {

    public QuerySessionsData(RefreshToken refreshToken) {
        this(refreshToken.getId(), refreshToken.getIp(), refreshToken.getIssuedAt(), refreshToken.getExpiredAt());
    }
}
