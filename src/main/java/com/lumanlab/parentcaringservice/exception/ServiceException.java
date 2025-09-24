package com.lumanlab.parentcaringservice.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * 서비스 계층에서 발생하는 커스텀 예외의 기본 클래스
 */
@Getter
public class ServiceException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> additionalData;

    public ServiceException(String errorCode, String message) {
        this(errorCode, message, Collections.emptyMap());
    }

    public ServiceException(String errorCode, String message, Map<String, Object> additionalData) {
        super(message);
        this.errorCode = errorCode;
        this.additionalData = additionalData != null ? additionalData : Collections.emptyMap();
    }

    public ServiceException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, Collections.emptyMap());
    }

    public ServiceException(String errorCode, String message, Throwable cause, Map<String, Object> additionalData) {
        super(message, cause);
        this.errorCode = errorCode;
        this.additionalData = additionalData != null ? additionalData : Collections.emptyMap();
    }
}
