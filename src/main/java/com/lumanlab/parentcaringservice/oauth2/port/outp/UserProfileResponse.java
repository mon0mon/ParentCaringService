package com.lumanlab.parentcaringservice.oauth2.port.outp;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;

public record UserProfileResponse(OAuth2Provider provider, String id, String email, String name, Long exp) {
}
