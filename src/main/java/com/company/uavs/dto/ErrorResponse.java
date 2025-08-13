package com.company.uavs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ErrorResponse {
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("path")
    private String path;
    
    // Constructors
    public ErrorResponse() {
        this.timestamp = Instant.now();
    }
    
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = Instant.now();
    }
    
    public ErrorResponse(String errorCode, String message, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}