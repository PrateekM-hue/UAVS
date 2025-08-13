package com.company.uavs.exception;

public class UavsException extends RuntimeException {
    
    private final String errorCode;
    
    public UavsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public UavsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}