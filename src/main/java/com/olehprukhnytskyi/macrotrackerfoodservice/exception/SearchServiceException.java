package com.olehprukhnytskyi.macrotrackerfoodservice.exception;

public class SearchServiceException extends RuntimeException {
    public SearchServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
