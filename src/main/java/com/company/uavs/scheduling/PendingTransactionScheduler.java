package com.company.uavs.scheduling;

import com.company.uavs.service.ActivationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PendingTransactionScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(PendingTransactionScheduler.class);
    
    private final ActivationService activationService;
    
    public PendingTransactionScheduler(ActivationService activationService) {
        this.activationService = activationService;
    }
    
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void retryPendingTransactions() {
        logger.info("Starting scheduled retry of pending transactions");
        
        try {
            activationService.retryPendingTransactions();
            logger.info("Completed scheduled retry of pending transactions");
        } catch (Exception e) {
            logger.error("Error during scheduled retry of pending transactions", e);
        }
    }
}