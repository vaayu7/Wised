package com.wised.helpandsettings.exception;

public class PasswordNotMatchedException extends Exception {
    public PasswordNotMatchedException(String wrong_password) {
        super(wrong_password);
    }
}
