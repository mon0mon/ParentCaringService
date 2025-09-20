package com.lumanlab.parentcaringservice.exception;

import java.util.Collections;
import java.util.Map;

public class MfaVerificationFailedException extends ServiceException {

    public MfaVerificationFailedException(String message, Map<String, Object> additionalData) {
        super("MFA_VERIFICATION_FAILED", message, additionalData);
    }

    public MfaVerificationFailedException(String message) {
        super("MFA_VERIFICATION_FAILED", message, Collections.emptyMap());
    }
}
