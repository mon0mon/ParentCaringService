package com.lumanlab.parentcaringservice.refreshtoken.port.outp;

import java.time.OffsetDateTime;

public record RefreshTokenDto(String token, String tokenHash, OffsetDateTime issuedAt, OffsetDateTime expiredAt) {}
