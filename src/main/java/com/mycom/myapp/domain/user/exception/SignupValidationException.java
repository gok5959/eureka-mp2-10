package com.mycom.myapp.domain.user.exception;

public class SignupValidationException extends RuntimeException {
    public SignupValidationException(String message) {
        super(message);
    }

    public SignupValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
