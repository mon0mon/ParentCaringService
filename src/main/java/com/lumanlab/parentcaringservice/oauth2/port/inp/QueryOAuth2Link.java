package com.lumanlab.parentcaringservice.oauth2.port.inp;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;

public interface QueryOAuth2Link {
    OAuth2Link findByOAuth2IdOrThrow(String oAuth2Id);
}
