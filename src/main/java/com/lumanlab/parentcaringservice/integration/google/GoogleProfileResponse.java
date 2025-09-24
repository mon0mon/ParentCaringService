package com.lumanlab.parentcaringservice.integration.google;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.oauth2.port.outp.UserProfileResponse;

public record GoogleProfileResponse(String id, String email, String name, Long exp) {

    public UserProfileResponse toUserProfileResponse() {
        return new UserProfileResponse(OAuth2Provider.GOOGLE, id, email, name, exp);
    }
}
