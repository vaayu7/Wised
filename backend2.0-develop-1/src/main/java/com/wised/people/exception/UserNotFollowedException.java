package com.wised.people.exception;

public class UserNotFollowedException extends Throwable {
    public UserNotFollowedException(String email) {
        super(email);
    }
}
