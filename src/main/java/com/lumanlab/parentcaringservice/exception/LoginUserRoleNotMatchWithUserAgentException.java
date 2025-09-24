package com.lumanlab.parentcaringservice.exception;

public class LoginUserRoleNotMatchWithUserAgentException extends ServiceException {
    public LoginUserRoleNotMatchWithUserAgentException(String errorCode, String message) {
        super(errorCode, message);
    }
}
