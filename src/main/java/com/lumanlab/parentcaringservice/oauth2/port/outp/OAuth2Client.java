package com.lumanlab.parentcaringservice.oauth2.port.outp;

public interface OAuth2Client {
    UserProfileResponse requestProfile(String authorization);
}
