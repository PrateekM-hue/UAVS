package com.company.uavs.controller;

import com.company.uavs.dto.VoidRequest;
import com.company.uavs.dto.VoidResponse;
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
@RequestMapping("/voids")
public class VoidController {
    
    private static final Logger logger = LoggerFactory.getLogger(VoidController.class);
    
    private final ActivationService activationService;
    
    public VoidController(ActivationService activationService) {
        this.activationService = activationService;
    }
    
    @PostMapping
    @Timed(value = "uavs.void.duration", description = "Time taken to process void")
    public ResponseEntity<VoidResponse> processVoid(
            @Valid @RequestBody VoidRequest request,
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
            logger.info("Processing void request for uniqueRef: {} from {}", 
                       request.getUniqueRef(), httpRequest.getRemoteAddr());
            
            VoidResponse response = activationService.processVoid(request);
            
            logger.info("Void processed for uniqueRef: {}, status: {}", 
                       request.getUniqueRef(), response.getStatus());
            
            return ResponseEntity.ok()
                    .header("X-Correlation-Id", correlationId)
                    .body(response);
                    
        } finally {
            MDC.clear();
        }
    }
}