package com.company.uavs.vendor.impl;

import com.company.uavs.entity.SaleActivation;
import com.company.uavs.vendor.VendorAdapter;
import com.company.uavs.vendor.VendorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestVendorAdapter implements VendorAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(TestVendorAdapter.class);
    
    @Override
    public String getVendorName() {
        return "TEST";
    }
    
    @Override
    public boolean supports(String vendorEndpoint) {
        return "TEST".equalsIgnoreCase(vendorEndpoint);
    }
    
    @Override
    public VendorResponse activate(SaleActivation transaction) {
        logger.info("Processing TEST activation for uniqueRef: {}", transaction.getUniqueRef());
        
        // Simulate successful activation
        String activationCode = "TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        VendorResponse response = VendorResponse.success(
            activationCode,
            "https://test.example.com/terms",
            "This is a test activation. No real activation required."
        );
        
        response.setRawRequest("{\"test_request\": \"" + transaction.getUniqueRef() + "\"}");
        response.setRawResponse("{\"test_response\": \"" + activationCode + "\", \"status\": \"SUCCESS\"}");
        
        logger.info("TEST activation successful for uniqueRef: {}, code: {}", 
                   transaction.getUniqueRef(), activationCode);
        
        return response;
    }
    
    @Override
    public VendorResponse voidTransaction(SaleActivation transaction) {
        logger.info("Processing TEST void for uniqueRef: {}", transaction.getUniqueRef());
        
        // Simulate successful void
        String voidCode = "VOID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        VendorResponse response = VendorResponse.voidSuccess(voidCode);
        response.setRawRequest("{\"test_void_request\": \"" + transaction.getUniqueRef() + "\"}");
        response.setRawResponse("{\"test_void_response\": \"" + voidCode + "\", \"status\": \"SUCCESS\"}");
        
        logger.info("TEST void successful for uniqueRef: {}, code: {}", 
                   transaction.getUniqueRef(), voidCode);
        
        return response;
    }
}