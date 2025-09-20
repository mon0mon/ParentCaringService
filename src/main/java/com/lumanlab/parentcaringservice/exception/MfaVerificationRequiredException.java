package com.lumanlab.parentcaringservice.exception;

import java.util.Map;

/**
 * 로그인 시 MFA 인증이 필요한 경우 발생하는 예외
 */
public class MfaVerificationRequiredException extends ServiceException {

    public MfaVerificationRequiredException(String message, Map<String, Object> additionalData) {
        super("MFA_VERIFICATION_REQUIRED", message, additionalData);
    }

    public MfaVerificationRequiredException(String message, String nonce) {
        super("MFA_VERIFICATION_REQUIRED", message, Map.of("nonce", nonce));
    }
}
