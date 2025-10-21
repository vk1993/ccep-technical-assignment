package com.bayer.healthgoal.exceptions;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }
}