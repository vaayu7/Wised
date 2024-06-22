package com.wised.auth.exception;

public class PollNotFoundException extends RuntimeException {
    public PollNotFoundException(Integer pollId) {
        super("Poll not found with ID: " + pollId);
    }
}