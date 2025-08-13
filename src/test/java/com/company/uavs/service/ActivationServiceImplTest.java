package com.company.uavs.service;

import com.company.uavs.dto.SaleActivationRequest;
import com.company.uavs.dto.SaleActivationResponse;
import com.company.uavs.dto.VoidRequest;
import com.company.uavs.dto.VoidResponse;
import com.company.uavs.entity.ProductCatalog;
import com.company.uavs.entity.SaleActivation;
import com.company.uavs.entity.TransactionStatus;
import com.company.uavs.exception.ProductNotFoundException;
import com.company.uavs.exception.CurrencyMismatchException;
import com.company.uavs.exception.TransactionNotFoundException;
import com.company.uavs.repository.ProductCatalogRepository;
import com.company.uavs.repository.SaleActivationRepository;
import com.company.uavs.service.impl.ActivationServiceImpl;
import com.company.uavs.vendor.VendorAdapter;
import com.company.uavs.vendor.VendorAdapterFactory;
import com.company.uavs.vendor.VendorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationServiceImplTest {
    
    @Mock
    private SaleActivationRepository saleActivationRepository;
    
    @Mock
    private ProductCatalogRepository productCatalogRepository;
    
    @Mock
    private VendorAdapterFactory vendorAdapterFactory;
    
    @Mock
    private VendorAdapter vendorAdapter;
    
    @InjectMocks
    private ActivationServiceImpl activationService;
    
    private SaleActivationRequest validRequest;
    private ProductCatalog product;
    private SaleActivation transaction;
    
    @BeforeEach
    void setUp() {
        validRequest = new SaleActivationRequest(
            "MCF-AV-12M-001",
            199900L,
            "INR",
            "1234567890123456",
            "INV-2025-001",
            "TXN-2025-001-001",
            "STR001",
            "POS-001",
            Instant.now()
        );
        
        product = new ProductCatalog(
            "MCF-AV-12M-001",
            "ANTIVIRUS",
            "McAfee Antivirus 12 Month License",
            "INR",
            "MCAFEE",
            true
        );
        
        transaction = new SaleActivation(
            "TXN-2025-001-001",
            "MCF-AV-12M-001",
            199900L,
            "INR",
            "INV-2025-001",
            "STR001",
            "POS-001",
            Instant.now()
        );
    }
    
    @Test
    void processActivation_Success() {
        // Given
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.empty());
        when(productCatalogRepository.findByProductEanAndActiveTrue(anyString())).thenReturn(Optional.of(product));
        when(vendorAdapterFactory.getAdapter(anyString())).thenReturn(Optional.of(vendorAdapter));
        when(saleActivationRepository.save(any(SaleActivation.class))).thenReturn(transaction);
        
        VendorResponse vendorResponse = VendorResponse.success(
            "MCF-ABC123-DEF456", 
            "https://mcafee.com/terms", 
            "Visit mcafee.com/activate"
        );
        when(vendorAdapter.activate(any(SaleActivation.class))).thenReturn(vendorResponse);
        
        // When
        SaleActivationResponse response = activationService.processActivation(validRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("TXN-2025-001-001", response.getUniqueRef());
        assertEquals("ACTIVATED", response.getStatus());
        assertEquals("MCF-ABC123-DEF456", response.getActivationCode());
        
        verify(saleActivationRepository, times(2)).save(any(SaleActivation.class));
        verify(vendorAdapter).activate(any(SaleActivation.class));
    }
    
    @Test
    void processActivation_ProductNotFound() {
        // Given
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.empty());
        when(productCatalogRepository.findByProductEanAndActiveTrue(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            activationService.processActivation(validRequest);
        });
        
        verify(saleActivationRepository, never()).save(any(SaleActivation.class));
    }
    
    @Test
    void processActivation_CurrencyMismatch() {
        // Given
        product.setCurrency("USD"); // Different currency
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.empty());
        when(productCatalogRepository.findByProductEanAndActiveTrue(anyString())).thenReturn(Optional.of(product));
        
        // When & Then
        assertThrows(CurrencyMismatchException.class, () -> {
            activationService.processActivation(validRequest);
        });
        
        verify(saleActivationRepository, never()).save(any(SaleActivation.class));
    }
    
    @Test
    void processActivation_IdempotentRequest() {
        // Given
        transaction.setStatus(TransactionStatus.ACTIVATED);
        transaction.setActivationCode("MCF-ABC123-DEF456");
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.of(transaction));
        
        // When
        SaleActivationResponse response = activationService.processActivation(validRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("TXN-2025-001-001", response.getUniqueRef());
        assertEquals("ACTIVATED", response.getStatus());
        assertEquals("MCF-ABC123-DEF456", response.getActivationCode());
        
        verify(productCatalogRepository, never()).findByProductEanAndActiveTrue(anyString());
        verify(vendorAdapter, never()).activate(any(SaleActivation.class));
    }
    
    @Test
    void processVoid_Success() {
        // Given
        VoidRequest voidRequest = new VoidRequest("TXN-2025-001-001", "INV-2025-001", "MCF-AV-12M-001");
        transaction.setStatus(TransactionStatus.ACTIVATED);
        transaction.setActivationCode("MCF-ABC123-DEF456");
        
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.of(transaction));
        when(productCatalogRepository.findByProductEanAndActiveTrue(anyString())).thenReturn(Optional.of(product));
        when(vendorAdapterFactory.getAdapter(anyString())).thenReturn(Optional.of(vendorAdapter));
        
        VendorResponse vendorResponse = VendorResponse.voidSuccess("VOID-ABC123");
        when(vendorAdapter.voidTransaction(any(SaleActivation.class))).thenReturn(vendorResponse);
        
        // When
        VoidResponse response = activationService.processVoid(voidRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("Void successful", response.getMessage());
        assertEquals("VOIDED", response.getStatus());
        assertEquals("TXN-2025-001-001", response.getUniqueRef());
        assertEquals("VOID-ABC123", response.getVoidCode());
        
        verify(vendorAdapter).voidTransaction(any(SaleActivation.class));
        verify(saleActivationRepository).save(any(SaleActivation.class));
    }
    
    @Test
    void processVoid_TransactionNotFound() {
        // Given
        VoidRequest voidRequest = new VoidRequest("TXN-NOT-FOUND", "INV-2025-001", "MCF-AV-12M-001");
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(TransactionNotFoundException.class, () -> {
            activationService.processVoid(voidRequest);
        });
        
        verify(vendorAdapter, never()).voidTransaction(any(SaleActivation.class));
    }
    
    @Test
    void getTransactionStatus_Success() {
        // Given
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.of(transaction));
        
        // When
        SaleActivation result = activationService.getTransactionStatus("TXN-2025-001-001");
        
        // Then
        assertNotNull(result);
        assertEquals("TXN-2025-001-001", result.getUniqueRef());
        assertEquals("MCF-AV-12M-001", result.getProductEan());
    }
    
    @Test
    void getTransactionStatus_NotFound() {
        // Given
        when(saleActivationRepository.findByUniqueRef(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(TransactionNotFoundException.class, () -> {
            activationService.getTransactionStatus("TXN-NOT-FOUND");
        });
    }
}