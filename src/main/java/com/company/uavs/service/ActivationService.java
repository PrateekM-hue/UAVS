package com.company.uavs.service;

import com.company.uavs.dto.SaleActivationRequest;
import com.company.uavs.dto.SaleActivationResponse;
import com.company.uavs.dto.VoidRequest;
import com.company.uavs.dto.VoidResponse;
import com.company.uavs.entity.SaleActivation;

public interface ActivationService {
    
    /**
     * Process sale/activation request
     */
    SaleActivationResponse processActivation(SaleActivationRequest request);
    
    /**
     * Process void request
     */
    VoidResponse processVoid(VoidRequest request);
    
    /**
     * Get transaction status by unique reference
     */
    SaleActivation getTransactionStatus(String uniqueRef);
    
    /**
     * Retry pending transactions
     */
    void retryPendingTransactions();
}