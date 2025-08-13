package com.company.uavs.service.impl;

import com.company.uavs.dto.SaleActivationRequest;
import com.company.uavs.dto.SaleActivationResponse;
import com.company.uavs.dto.VoidRequest;
import com.company.uavs.dto.VoidResponse;
import com.company.uavs.entity.ProductCatalog;
import com.company.uavs.entity.SaleActivation;
import com.company.uavs.entity.TransactionStatus;
import com.company.uavs.exception.*;
import com.company.uavs.repository.ProductCatalogRepository;
import com.company.uavs.repository.SaleActivationRepository;
import com.company.uavs.service.ActivationService;
import com.company.uavs.vendor.VendorAdapter;
import com.company.uavs.vendor.VendorAdapterFactory;
import com.company.uavs.vendor.VendorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActivationServiceImpl implements ActivationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivationServiceImpl.class);
    
    private final SaleActivationRepository saleActivationRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final VendorAdapterFactory vendorAdapterFactory;
    
    public ActivationServiceImpl(SaleActivationRepository saleActivationRepository,
                               ProductCatalogRepository productCatalogRepository,
                               VendorAdapterFactory vendorAdapterFactory) {
        this.saleActivationRepository = saleActivationRepository;
        this.productCatalogRepository = productCatalogRepository;
        this.vendorAdapterFactory = vendorAdapterFactory;
    }
    
    @Override
    public SaleActivationResponse processActivation(SaleActivationRequest request) {
        logger.info("Processing activation for uniqueRef: {}, productEan: {}", 
                   request.getUniqueRef(), request.getProductEan());
        
        // Check for idempotency - if duplicate unique_ref, return original result
        Optional<SaleActivation> existingTransaction = saleActivationRepository.findByUniqueRef(request.getUniqueRef());
        if (existingTransaction.isPresent()) {
            logger.info("Duplicate request detected for uniqueRef: {}, returning existing result", request.getUniqueRef());
            return mapToActivationResponse(existingTransaction.get());
        }
        
        // Validate product exists and is active
        ProductCatalog product = productCatalogRepository.findByProductEanAndActiveTrue(request.getProductEan())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductEan()));
        
        // Validate currency matches
        if (!product.getCurrency().equals(request.getCurrency())) {
            throw new CurrencyMismatchException(request.getCurrency(), product.getCurrency());
        }
        
        // Validate amount range if specified
        if (product.getMinAmount() != null && request.getAmount() < product.getMinAmount()) {
            throw new UavsException("SA03", "Amount below minimum: " + product.getMinAmount());
        }
        if (product.getMaxAmount() != null && request.getAmount() > product.getMaxAmount()) {
            throw new UavsException("SA03", "Amount above maximum: " + product.getMaxAmount());
        }
        
        // Create and save transaction record
        SaleActivation transaction = new SaleActivation(
            request.getUniqueRef(),
            request.getProductEan(),
            request.getAmount(),
            request.getCurrency(),
            request.getInvoiceNo(),
            request.getStoreCode(),
            request.getDeviceId(),
            request.getTxnDate()
        );
        transaction.setCardNo(request.getCardNo());
        transaction.setStatus(TransactionStatus.NEW);
        
        try {
            saleActivationRepository.save(transaction);
            logger.info("Transaction record created for uniqueRef: {}", request.getUniqueRef());
        } catch (DataIntegrityViolationException e) {
            // Handle race condition - another thread might have created the same transaction
            logger.warn("Duplicate key violation for uniqueRef: {}, checking for existing transaction", request.getUniqueRef());
            Optional<SaleActivation> existingTxn = saleActivationRepository.findByUniqueRef(request.getUniqueRef());
            if (existingTxn.isPresent()) {
                return mapToActivationResponse(existingTxn.get());
            }
            throw e;
        }
        
        // Get vendor adapter
        VendorAdapter adapter = vendorAdapterFactory.getAdapter(product.getVendorEndpoint())
                .orElseThrow(() -> new UavsException("VND01", "No adapter found for vendor: " + product.getVendorEndpoint()));
        
        try {
            // Call vendor API
            VendorResponse vendorResponse = adapter.activate(transaction);
            
            // Update transaction with vendor response
            transaction.setVendorRawRequest(vendorResponse.getRawRequest());
            transaction.setVendorRawResponse(vendorResponse.getRawResponse());
            
            if (vendorResponse.isSuccess()) {
                transaction.setStatus(TransactionStatus.ACTIVATED);
                transaction.setActivationCode(vendorResponse.getActivationCode());
                transaction.setTnc(vendorResponse.getTnc());
                transaction.setActivationSteps(vendorResponse.getActivationSteps());
                
                logger.info("Activation successful for uniqueRef: {}", request.getUniqueRef());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorCode(vendorResponse.getErrorCode());
                transaction.setErrorMessage(vendorResponse.getErrorMessage());
                
                logger.error("Activation failed for uniqueRef: {}, error: {}", 
                           request.getUniqueRef(), vendorResponse.getErrorMessage());
            }
            
            saleActivationRepository.save(transaction);
            return mapToActivationResponse(transaction);
            
        } catch (VendorTimeoutException e) {
            // Mark as pending and save
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setErrorCode(e.getErrorCode());
            transaction.setErrorMessage(e.getMessage());
            transaction.setRetryCount(transaction.getRetryCount() + 1);
            saleActivationRepository.save(transaction);
            
            logger.warn("Vendor timeout for uniqueRef: {}, marked as pending", request.getUniqueRef());
            throw e;
            
        } catch (Exception e) {
            // Mark as failed and save
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorCode("VND02");
            transaction.setErrorMessage("Vendor error: " + e.getMessage());
            saleActivationRepository.save(transaction);
            
            logger.error("Unexpected vendor error for uniqueRef: {}", request.getUniqueRef(), e);
            throw new UavsException("VND02", "Vendor error: " + e.getMessage(), e);
        }
    }
    
    @Override
    public VoidResponse processVoid(VoidRequest request) {
        logger.info("Processing void for uniqueRef: {}", request.getUniqueRef());
        
        // Find the original transaction
        SaleActivation transaction = saleActivationRepository.findByUniqueRef(request.getUniqueRef())
                .orElseThrow(() -> new TransactionNotFoundException(request.getUniqueRef()));
        
        // Validate transaction can be voided
        if (transaction.getStatus() != TransactionStatus.ACTIVATED) {
            throw new TransactionNotActivatedException(request.getUniqueRef());
        }
        
        // Check if already voided
        if (transaction.getStatus() == TransactionStatus.VOIDED) {
            logger.info("Transaction already voided for uniqueRef: {}", request.getUniqueRef());
            return new VoidResponse("00", "Already voided", "VOIDED", request.getUniqueRef(), transaction.getVoidCode());
        }
        
        // Validate invoice and product match
        if (!transaction.getInvoiceNo().equals(request.getInvoiceNo()) ||
            !transaction.getProductEan().equals(request.getProductEan())) {
            throw new UavsException("VD03", "Invoice or product mismatch");
        }
        // No local return window enforcement; vendor decides eligibility
        
        // Get product info
        ProductCatalog product = productCatalogRepository.findByProductEanAndActiveTrue(transaction.getProductEan())
                .orElseThrow(() -> new ProductNotFoundException(transaction.getProductEan()));
        
        // Get vendor adapter
        VendorAdapter adapter = vendorAdapterFactory.getAdapter(product.getVendorEndpoint())
                .orElseThrow(() -> new UavsException("VND01", "No adapter found for vendor: " + product.getVendorEndpoint()));
        
        try {
            // Call vendor void API
            VendorResponse vendorResponse = adapter.voidTransaction(transaction);
            
            if (vendorResponse.isSuccess()) {
                transaction.setStatus(TransactionStatus.VOIDED);
                transaction.setVoidCode(vendorResponse.getVoidCode());
                saleActivationRepository.save(transaction);
                
                logger.info("Void successful for uniqueRef: {}", request.getUniqueRef());
                return new VoidResponse("00", "Void successful", "VOIDED", request.getUniqueRef(), vendorResponse.getVoidCode());
                
            } else {
                String errorCode = vendorResponse.getErrorCode();
                String errorMessage = vendorResponse.getErrorMessage();
                
                logger.error("Void failed for uniqueRef: {}, error: {}", request.getUniqueRef(), errorMessage);
                return new VoidResponse("02", errorMessage != null ? errorMessage : "Vendor void failed", "FAILED");
            }
            
        } catch (VendorTimeoutException e) {
            logger.warn("Vendor timeout during void for uniqueRef: {}", request.getUniqueRef());
            throw e;
            
        } catch (Exception e) {
            logger.error("Unexpected error during void for uniqueRef: {}", request.getUniqueRef(), e);
            return new VoidResponse("02", "Vendor error: " + e.getMessage(), "FAILED");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public SaleActivation getTransactionStatus(String uniqueRef) {
        logger.info("Getting transaction status for uniqueRef: {}", uniqueRef);
        
        return saleActivationRepository.findByUniqueRef(uniqueRef)
                .orElseThrow(() -> new TransactionNotFoundException(uniqueRef));
    }
    
    @Override
    public void retryPendingTransactions() {
        logger.info("Starting retry of pending transactions");
        
        Instant cutoffTime = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<SaleActivation> pendingTransactions = saleActivationRepository
                .findPendingTransactionsForRetry(3, cutoffTime);
        
        logger.info("Found {} pending transactions to retry", pendingTransactions.size());
        
        for (SaleActivation transaction : pendingTransactions) {
            try {
                retryTransaction(transaction);
            } catch (Exception e) {
                logger.error("Failed to retry transaction {}", transaction.getUniqueRef(), e);
            }
        }
        
        logger.info("Completed retry of pending transactions");
    }
    
    private void retryTransaction(SaleActivation transaction) {
        logger.info("Retrying transaction {}", transaction.getUniqueRef());
        
        ProductCatalog product = productCatalogRepository.findByProductEanAndActiveTrue(transaction.getProductEan())
                .orElse(null);
        
        if (product == null) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage("Product no longer available");
            saleActivationRepository.save(transaction);
            return;
        }
        
        VendorAdapter adapter = vendorAdapterFactory.getAdapter(product.getVendorEndpoint())
                .orElse(null);
        
        if (adapter == null) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage("Vendor adapter not available");
            saleActivationRepository.save(transaction);
            return;
        }
        
        try {
            VendorResponse vendorResponse = adapter.activate(transaction);
            
            transaction.setRetryCount(transaction.getRetryCount() + 1);
            
            if (vendorResponse.isSuccess()) {
                transaction.setStatus(TransactionStatus.ACTIVATED);
                transaction.setActivationCode(vendorResponse.getActivationCode());
                transaction.setTnc(vendorResponse.getTnc());
                transaction.setActivationSteps(vendorResponse.getActivationSteps());
                
                logger.info("Retry successful for transaction {}", transaction.getUniqueRef());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorCode(vendorResponse.getErrorCode());
                transaction.setErrorMessage(vendorResponse.getErrorMessage());
                
                logger.error("Retry failed for transaction {}: {}", 
                           transaction.getUniqueRef(), vendorResponse.getErrorMessage());
            }
            
        } catch (VendorTimeoutException e) {
            transaction.setRetryCount(transaction.getRetryCount() + 1);
            if (transaction.getRetryCount() >= 3) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage("Max retries exceeded");
            }
            
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage("Retry failed: " + e.getMessage());
        }
        
        saleActivationRepository.save(transaction);
    }
    
    private SaleActivationResponse mapToActivationResponse(SaleActivation transaction) {
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