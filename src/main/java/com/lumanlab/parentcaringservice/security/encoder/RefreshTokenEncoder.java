package com.lumanlab.parentcaringservice.security.encoder;

public interface RefreshTokenEncoder {
    String encode(String token);
    boolean matches(String token, String encodedToken);
}
