package com.wised.auth.exception;

public class SameUserConnectionException extends RuntimeException {
    public SameUserConnectionException(String message) {
        super(message);
    }
}
