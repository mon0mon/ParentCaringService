package com.lumanlab.parentcaringservice.advice;

import com.lumanlab.parentcaringservice.exception.MfaInitializationRequiredException;
import com.lumanlab.parentcaringservice.exception.MfaVerificationFailedException;
import com.lumanlab.parentcaringservice.exception.MfaVerificationRequiredException;
import com.lumanlab.parentcaringservice.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * MFA 초기화 필요 예외 처리
     */
    @ExceptionHandler(MfaInitializationRequiredException.class)
    public ResponseEntity<ErrorResponse> handleMfaInitializationRequired(MfaInitializationRequiredException e) {
        log.warn("MFA initialization required: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(), e.getMessage(), e.getAdditionalData()
        );

        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(errorResponse);
    }

    /**
     * MFA 초기화 필요 예외 처리
     */
    @ExceptionHandler(MfaVerificationRequiredException.class)
    public ResponseEntity<ErrorResponse> handleMfaVerificationRequired(MfaVerificationRequiredException e) {
        log.warn("MFA verification required: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(), e.getMessage(), e.getAdditionalData()
        );

        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument error occurred", e);

        ErrorResponse errorResponse = new ErrorResponse(
                "ILLEGAL_ARGUMENT", "잘못된 요청 값입니다."
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.error("Illegal state error occurred", e);

        ErrorResponse errorResponse = new ErrorResponse(
                "ILLEGAL_STATE", "처리할 수 없는 상태입니다."
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * JwtException 처리
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {
        log.warn("JWT exception occurred: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                "JWT_ERROR", e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * ServiceException 처리
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException e) {
        log.warn("Service exception occurred: {} - {}", e.getErrorCode(), e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(), e.getMessage(), e.getAdditionalData()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred", e);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR", "내부 서버 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
