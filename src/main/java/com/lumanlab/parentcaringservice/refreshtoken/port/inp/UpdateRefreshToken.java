package com.lumanlab.parentcaringservice.refreshtoken.port.inp;

public interface UpdateRefreshToken {
    void generate(Long userId, String ip, String userAgent);
    void rotate(Long userId, String token);
    void revoke(Long userId, String token);
}
