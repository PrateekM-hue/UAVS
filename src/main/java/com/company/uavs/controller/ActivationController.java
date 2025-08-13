package com.company.uavs.controller;

import com.company.uavs.dto.SaleActivationRequest;
import com.company.uavs.dto.SaleActivationResponse;
import com.company.uavs.service.ActivationService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sales")
public class ActivationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivationController.class);
    
    private final ActivationService activationService;
    
    public ActivationController(ActivationService activationService) {
        this.activationService = activationService;
    }
    
    @PostMapping
    @Timed(value = "uavs.activation.duration", description = "Time taken to process activation")
    public ResponseEntity<SaleActivationResponse> processActivation(
            @Valid @RequestBody SaleActivationRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestHeader(value = "X-Retry-Count", required = false, defaultValue = "0") String retryCount,
            HttpServletRequest httpRequest) {
        
        // Set correlation ID for tracing
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        MDC.put("retryCount", retryCount);
        MDC.put("uniqueRef", request.getUniqueRef());
        
        try {
            logger.info("Processing activation request for uniqueRef: {} from {}", 
                       request.getUniqueRef(), httpRequest.getRemoteAddr());
            
            SaleActivationResponse response = activationService.processActivation(request);
            
            logger.info("Activation processed successfully for uniqueRef: {}, status: {}", 
                       request.getUniqueRef(), response.getStatus());
            
            return ResponseEntity.ok()
                    .header("X-Correlation-Id", correlationId)
                    .body(response);
                    
        } finally {
            MDC.clear();
        }
    }
}