package com.company.uavs.controller;

import com.company.uavs.dto.SaleActivationResponse;
import com.company.uavs.entity.SaleActivation;
import com.company.uavs.service.ActivationService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/status")
public class StatusController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);
    
    private final ActivationService activationService;
    
    public StatusController(ActivationService activationService) {
        this.activationService = activationService;
    }
    
    @GetMapping("/{uniqueRef}")
    @Timed(value = "uavs.status.duration", description = "Time taken to get status")
    public ResponseEntity<SaleActivationResponse> getStatus(
            @PathVariable String uniqueRef,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            HttpServletRequest httpRequest) {
        
        // Set correlation ID for tracing
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);
        MDC.put("uniqueRef", uniqueRef);
        
        try {
            logger.info("Getting status for uniqueRef: {} from {}", 
                       uniqueRef, httpRequest.getRemoteAddr());
            
            SaleActivation transaction = activationService.getTransactionStatus(uniqueRef);
            SaleActivationResponse response = mapToStatusResponse(transaction);
            
            logger.info("Status retrieved for uniqueRef: {}, status: {}", 
                       uniqueRef, response.getStatus());
            
            return ResponseEntity.ok()
                    .header("X-Correlation-Id", correlationId)
                    .body(response);
                    
        } finally {
            MDC.clear();
        }
    }
    
    private SaleActivationResponse mapToStatusResponse(SaleActivation transaction) {
        SaleActivationResponse response = new SaleActivationResponse();
        response.setProductEan(transaction.getProductEan());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setCardNo(transaction.getCardNo());
        response.setInvoiceNo(transaction.getInvoiceNo());
        response.setUniqueRef(transaction.getUniqueRef());
        response.setActivationCode(transaction.getActivationCode());
        response.setStatus(transaction.getStatus().name());
        response.setTnc(transaction.getTnc());
        response.setActivationSteps(transaction.getActivationSteps());
        
        return response;
    }
}