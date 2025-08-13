package com.company.uavs.exception;

public class VendorTimeoutException extends UavsException {
    
    public VendorTimeoutException(String vendor) {
        super("SA04", "Vendor timeout – pending: " + vendor);
    }
    
    public VendorTimeoutException(String vendor, Throwable cause) {
        super("SA04", "Vendor timeout – pending: " + vendor, cause);
    }
}