package com.orderplatform.auth_service.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApplicationException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email, HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }
}
