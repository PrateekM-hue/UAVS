package com.company.uavs.vendor;

public class VendorResponse {
    
    private boolean success;
    private String activationCode;
    private String voidCode;
    private String errorCode;
    private String errorMessage;
    private String tnc;
    private String activationSteps;
    private String rawRequest;
    private String rawResponse;
    
    // Constructors
    public VendorResponse() {}
    
    public static VendorResponse success(String activationCode, String tnc, String activationSteps) {
        VendorResponse response = new VendorResponse();
        response.success = true;
        response.activationCode = activationCode;
        response.tnc = tnc;
        response.activationSteps = activationSteps;
        return response;
    }
    
    public static VendorResponse voidSuccess(String voidCode) {
        VendorResponse response = new VendorResponse();
        response.success = true;
        response.voidCode = voidCode;
        return response;
    }
    
    public static VendorResponse failure(String errorCode, String errorMessage) {
        VendorResponse response = new VendorResponse();
        response.success = false;
        response.errorCode = errorCode;
        response.errorMessage = errorMessage;
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getActivationCode() {
        return activationCode;
    }
    
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
    
    public String getVoidCode() {
        return voidCode;
    }
    
    public void setVoidCode(String voidCode) {
        this.voidCode = voidCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getTnc() {
        return tnc;
    }
    
    public void setTnc(String tnc) {
        this.tnc = tnc;
    }
    
    public String getActivationSteps() {
        return activationSteps;
    }
    
    public void setActivationSteps(String activationSteps) {
        this.activationSteps = activationSteps;
    }
    
    public String getRawRequest() {
        return rawRequest;
    }
    
    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}