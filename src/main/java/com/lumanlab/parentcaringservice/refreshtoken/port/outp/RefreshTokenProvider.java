package com.lumanlab.parentcaringservice.refreshtoken.port.outp;

import java.util.Map;

public interface RefreshTokenProvider {
    RefreshTokenDto generateRefreshToken(Long userId, Map<String, Object> claims);
}
