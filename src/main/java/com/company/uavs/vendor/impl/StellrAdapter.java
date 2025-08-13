package com.company.uavs.vendor.impl;

import com.company.uavs.entity.SaleActivation;
import com.company.uavs.exception.VendorTimeoutException;
import com.company.uavs.vendor.VendorAdapter;
import com.company.uavs.vendor.VendorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class StellrAdapter implements VendorAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(StellrAdapter.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${uavs.stellr.base-url}")
    private String baseUrl;
    
    @Value("${uavs.stellr.timeout:3000}")
    private int timeout;
    
    public StellrAdapter(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getVendorName() {
        return "STELLR";
    }
    
    @Override
    public boolean supports(String vendorEndpoint) {
        return "STELLR".equalsIgnoreCase(vendorEndpoint);
    }
    
    @Override
    public VendorResponse activate(SaleActivation transaction) {
        logger.info("Processing Stellr activation for uniqueRef: {}", transaction.getUniqueRef());
        
        try {
            // Prepare request payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("product_code", transaction.getProductEan());
            requestPayload.put("amount", transaction.getAmount());
            requestPayload.put("currency", transaction.getCurrency());
            requestPayload.put("external_ref", transaction.getUniqueRef());
            requestPayload.put("card_number", transaction.getCardNo());
            
            String rawRequest = objectMapper.writeValueAsString(requestPayload);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getApiKey());
            headers.set("X-Request-ID", UUID.randomUUID().toString());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            // Make API call
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/activate",
                HttpMethod.POST,
                request,
                Map.class
            );
            
            String rawResponse = objectMapper.writeValueAsString(response.getBody());
            
            // Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if ("SUCCESS".equals(responseBody.get("status"))) {
                    VendorResponse vendorResponse = VendorResponse.success(
                        (String) responseBody.get("activation_code"),
                        (String) responseBody.get("terms_and_conditions"),
                        (String) responseBody.get("activation_steps")
                    );
                    vendorResponse.setRawRequest(rawRequest);
                    vendorResponse.setRawResponse(rawResponse);
                    
                    logger.info("Stellr activation successful for uniqueRef: {}", transaction.getUniqueRef());
                    return vendorResponse;
                } else {
                    String errorCode = (String) responseBody.get("error_code");
                    String errorMessage = (String) responseBody.get("error_message");
                    
                    VendorResponse vendorResponse = VendorResponse.failure(errorCode, errorMessage);
                    vendorResponse.setRawRequest(rawRequest);
                    vendorResponse.setRawResponse(rawResponse);
                    
                    logger.error("Stellr activation failed for uniqueRef: {}, error: {}", 
                               transaction.getUniqueRef(), errorMessage);
                    return vendorResponse;
                }
            } else {
                logger.error("Stellr activation failed with HTTP status: {}", response.getStatusCode());
                return VendorResponse.failure("STELLR_HTTP_ERROR", "HTTP Error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error calling Stellr activation API for uniqueRef: {}", transaction.getUniqueRef(), e);
            
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                throw new VendorTimeoutException("STELLR", e);
            }
            
            return VendorResponse.failure("STELLR_API_ERROR", "API call failed: " + e.getMessage());
        }
    }
    
    @Override
    public VendorResponse voidTransaction(SaleActivation transaction) {
        logger.info("Processing Stellr void for uniqueRef: {}", transaction.getUniqueRef());
        
        try {
            // Prepare request payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("external_ref", transaction.getUniqueRef());
            requestPayload.put("activation_code", transaction.getActivationCode());
            requestPayload.put("reason", "POS_VOID");
            
            String rawRequest = objectMapper.writeValueAsString(requestPayload);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + getApiKey());
            headers.set("X-Request-ID", UUID.randomUUID().toString());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            // Make API call
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/void",
                HttpMethod.POST,
                request,
                Map.class
            );
            
            String rawResponse = objectMapper.writeValueAsString(response.getBody());
            
            // Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if ("SUCCESS".equals(responseBody.get("status"))) {
                    VendorResponse vendorResponse = VendorResponse.voidSuccess(
                        (String) responseBody.get("void_code")
                    );
                    vendorResponse.setRawRequest(rawRequest);
                    vendorResponse.setRawResponse(rawResponse);
                    
                    logger.info("Stellr void successful for uniqueRef: {}", transaction.getUniqueRef());
                    return vendorResponse;
                } else {
                    String errorCode = (String) responseBody.get("error_code");
                    String errorMessage = (String) responseBody.get("error_message");
                    
                    VendorResponse vendorResponse = VendorResponse.failure(errorCode, errorMessage);
                    vendorResponse.setRawRequest(rawRequest);
                    vendorResponse.setRawResponse(rawResponse);
                    
                    logger.error("Stellr void failed for uniqueRef: {}, error: {}", 
                               transaction.getUniqueRef(), errorMessage);
                    return vendorResponse;
                }
            } else {
                logger.error("Stellr void failed with HTTP status: {}", response.getStatusCode());
                return VendorResponse.failure("STELLR_HTTP_ERROR", "HTTP Error: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error calling Stellr void API for uniqueRef: {}", transaction.getUniqueRef(), e);
            
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                throw new VendorTimeoutException("STELLR", e);
            }
            
            return VendorResponse.failure("STELLR_API_ERROR", "API call failed: " + e.getMessage());
        }
    }
    
    private String getApiKey() {
        // In a real implementation, this would fetch from Vault
        // For now, return a placeholder
        return "stellr-api-key-from-vault";
    }
}