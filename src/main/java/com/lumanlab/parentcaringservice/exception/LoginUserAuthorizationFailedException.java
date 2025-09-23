package com.lumanlab.parentcaringservice.exception;

public class LoginUserAuthorizationFailedException extends ServiceException {
    public LoginUserAuthorizationFailedException(String errorCode, String message) {
        super(errorCode, message);
    }
}
