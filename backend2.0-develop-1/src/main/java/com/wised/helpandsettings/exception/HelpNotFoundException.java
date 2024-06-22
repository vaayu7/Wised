package com.wised.helpandsettings.exception;

public class HelpNotFoundException extends Exception {
    public HelpNotFoundException(String no_help_found) {
        super(no_help_found);
    }
}
