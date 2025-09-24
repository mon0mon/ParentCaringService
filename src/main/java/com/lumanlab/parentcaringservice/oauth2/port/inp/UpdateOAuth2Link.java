package com.lumanlab.parentcaringservice.oauth2.port.inp;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;

public interface UpdateOAuth2Link {
    void register(Long userId, OAuth2Provider provider, String oAuth2Id);

    void delete(Long userId, OAuth2Provider provider);
}
