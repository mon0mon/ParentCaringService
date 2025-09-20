package com.lumanlab.parentcaringservice.exception;

import java.util.Map;

/**
 * MFA 초기화가 필요한 경우 발생하는 예외
 */
public class MfaInitializationRequiredException extends ServiceException {

    public MfaInitializationRequiredException(String message, Map<String, Object> additionalData) {
        super("MFA_INITIALIZATION_REQUIRED", message, additionalData);
    }

    public MfaInitializationRequiredException(String message, String nonce) {
        super("MFA_INITIALIZATION_REQUIRED", message, Map.of("nonce", nonce));
    }
}
