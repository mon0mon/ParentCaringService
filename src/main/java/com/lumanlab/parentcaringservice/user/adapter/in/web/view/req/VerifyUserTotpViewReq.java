package com.lumanlab.parentcaringservice.user.adapter.in.web.view.req;

public record VerifyUserTotpViewReq(String nonce, Integer verificationCode) {
}
