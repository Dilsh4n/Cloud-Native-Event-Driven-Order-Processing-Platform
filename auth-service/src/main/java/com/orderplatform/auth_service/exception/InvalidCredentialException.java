package com.orderplatform.auth_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialException extends ApplicationException {
    public InvalidCredentialException() {
        super("Invalid Email or Password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}
