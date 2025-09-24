package com.lumanlab.parentcaringservice.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final long timestamp;
    private final Map<String, Object> additionalData;

    public ErrorResponse(String errorCode, String message) {
        this(errorCode, message, Instant.now().getEpochSecond(), null);
    }

    public ErrorResponse(String errorCode, String message, Map<String, Object> additionalData) {
        this(errorCode, message, Instant.now().getEpochSecond(), additionalData);
    }
}
