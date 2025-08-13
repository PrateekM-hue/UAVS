package com.company.uavs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoidResponse {
    
    @JsonProperty("response_code")
    private String responseCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("unique_ref")
    private String uniqueRef;
    
    @JsonProperty("void_code")
    private String voidCode;
    
    // Constructors
    public VoidResponse() {}
    
    public VoidResponse(String responseCode, String message, String status) {
        this.responseCode = responseCode;
        this.message = message;
        this.status = status;
    }
    
    public VoidResponse(String responseCode, String message, String status, String uniqueRef, String voidCode) {
        this.responseCode = responseCode;
        this.message = message;
        this.status = status;
        this.uniqueRef = uniqueRef;
        this.voidCode = voidCode;
    }
    
    // Getters and Setters
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getUniqueRef() {
        return uniqueRef;
    }
    
    public void setUniqueRef(String uniqueRef) {
        this.uniqueRef = uniqueRef;
    }
    
    public String getVoidCode() {
        return voidCode;
    }
    
    public void setVoidCode(String voidCode) {
        this.voidCode = voidCode;
    }
}