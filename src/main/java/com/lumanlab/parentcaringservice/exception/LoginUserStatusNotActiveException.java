package com.lumanlab.parentcaringservice.exception;

public class LoginUserStatusNotActiveException extends ServiceException {
    public LoginUserStatusNotActiveException(String errorCode, String message) {
        super(errorCode, message);
    }
}
