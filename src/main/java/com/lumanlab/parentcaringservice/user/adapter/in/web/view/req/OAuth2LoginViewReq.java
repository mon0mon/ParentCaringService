package com.lumanlab.parentcaringservice.user.adapter.in.web.view.req;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;

public record OAuth2LoginViewReq(String accessToken, OAuth2Provider provider) {
}
