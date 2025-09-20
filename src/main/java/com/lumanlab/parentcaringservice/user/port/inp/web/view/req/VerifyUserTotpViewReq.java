package com.lumanlab.parentcaringservice.user.port.inp.web.view.req;

public record VerifyUserTotpViewReq(String nonce, Integer verificationCode) {
}
