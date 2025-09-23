package com.lumanlab.parentcaringservice.advice;

import com.lumanlab.parentcaringservice.exception.*;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
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

    /**
     * MFA 인증 실패 예외 처리
     */
    @ExceptionHandler(MfaVerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleMfaVerificationRequired(MfaVerificationFailedException e) {
        log.warn("MFA verification failed: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode(), e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
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
     * Spring Security의 권한 부족 예외 처리
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("Authorization denied: {}", e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(
                "AUTHORIZATION_DENIED", "이 리소스에 접근할 권한이 없습니다."
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }


    /**
     * LoginUserAuthorizationFailedException 처리
     */
    @ExceptionHandler(LoginUserAuthorizationFailedException.class)
    public ResponseEntity<ErrorResponse> handleLoginUserAuthorizationFailedException(
            LoginUserAuthorizationFailedException e
    ) {
        log.warn("User authorization failed: {} - {}", e.getErrorCode(), e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * LoginUserAuthorizationFailedException 처리
     */
    @ExceptionHandler(LoginUserRoleNotMatchWithUserAgentException.class)
    public ResponseEntity<ErrorResponse> handleLoginUserRoleNotMatchWithUserAgentException(
            LoginUserRoleNotMatchWithUserAgentException e
    ) {
        log.warn("User agent and user role are not match: {} - {}", e.getErrorCode(), e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * LoginUserAuthorizationFailedException 처리
     */
    @ExceptionHandler(LoginUserStatusNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleLoginUserStatusNotActiveException(
            LoginUserStatusNotActiveException e
    ) {
        log.warn("User status not active: {} - {}", e.getErrorCode(), e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
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

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
