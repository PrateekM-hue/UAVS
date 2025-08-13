package com.company.uavs.controller;

import com.company.uavs.config.TestSecurityConfig;
import com.company.uavs.dto.SaleActivationRequest;
import com.company.uavs.entity.ProductCatalog;
import com.company.uavs.repository.ProductCatalogRepository;
import com.company.uavs.repository.SaleActivationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false",
    "test.security.permit-all=false"
})
@Import(TestSecurityConfig.class)
@Transactional
class ActivationControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductCatalogRepository productCatalogRepository;
    
    @Autowired
    private SaleActivationRepository saleActivationRepository;
    
    private ProductCatalog testProduct;
    
    @BeforeEach
    void setUp() {
        // Clear repositories
        saleActivationRepository.deleteAll();
        productCatalogRepository.deleteAll();
        
        // Create test product
        testProduct = new ProductCatalog(
            "TEST-PROD-001",
            "TEST",
            "Test Product for Integration Testing",
            "INR",
            "TEST",
            true
        );
        testProduct.setMinAmount(10000L);
        testProduct.setMaxAmount(50000L);
        productCatalogRepository.save(testProduct);
    }
    
    @Test
    void processActivation_Success() throws Exception {
        // Given
        SaleActivationRequest request = new SaleActivationRequest(
            "TEST-PROD-001",
            25000L,
            "INR",
            "1234567890123456",
            "INV-TEST-001",
            "TXN-TEST-001",
            "STR001",
            "POS-001",
            Instant.now()
        );
        
        // When & Then
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .header("X-Correlation-Id", "test-correlation-id")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unique_ref").value("TXN-TEST-001"))
                .andExpect(jsonPath("$.product_ean").value("TEST-PROD-001"))
                .andExpect(jsonPath("$.amount").value(25000))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(header().string("X-Correlation-Id", "test-correlation-id"));
    }
    
    @Test
    void processActivation_ProductNotFound() throws Exception {
        // Given
        SaleActivationRequest request = new SaleActivationRequest(
            "UNKNOWN-PRODUCT",
            25000L,
            "INR",
            "1234567890123456",
            "INV-TEST-002",
            "TXN-TEST-002",
            "STR001",
            "POS-001",
            Instant.now()
        );
        
        // When & Then
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .header("X-Correlation-Id", "test-correlation-id")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error_code").value("SA01"))
                .andExpect(jsonPath("$.message").value("Unknown product_ean: UNKNOWN-PRODUCT"));
    }
    
    @Test
    void processActivation_CurrencyMismatch() throws Exception {
        // Given
        SaleActivationRequest request = new SaleActivationRequest(
            "TEST-PROD-001",
            25000L,
            "USD", // Wrong currency
            "1234567890123456",
            "INV-TEST-003",
            "TXN-TEST-003",
            "STR001",
            "POS-001",
            Instant.now()
        );
        
        // When & Then
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .header("X-Correlation-Id", "test-correlation-id")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error_code").value("SA02"))
                .andExpect(jsonPath("$.message").value(containsString("Currency mismatch")));
    }
    
    @Test
    void processActivation_ValidationError() throws Exception {
        // Given - Request with missing required fields
        SaleActivationRequest request = new SaleActivationRequest();
        request.setProductEan(""); // Empty EAN
        request.setAmount(25000L);
        request.setCurrency("INR");
        
        // When & Then
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .header("X-Correlation-Id", "test-correlation-id")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("VAL01"));
    }
    
    @Test
    void processActivation_IdempotentRequest() throws Exception {
        // Given
        SaleActivationRequest request = new SaleActivationRequest(
            "TEST-PROD-001",
            25000L,
            "INR",
            "1234567890123456",
            "INV-TEST-004",
            "TXN-TEST-004",
            "STR001",
            "POS-001",
            Instant.now()
        );
        
        // When - First request
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .header("X-Correlation-Id", "test-correlation-id-1")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unique_ref").value("TXN-TEST-004"));
        
        // When - Second identical request (idempotent)
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .header("X-Correlation-Id", "test-correlation-id-2")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unique_ref").value("TXN-TEST-004"))
                .andExpect(jsonPath("$.status").exists());
    }
    
    @Test
    void processActivation_Unauthorized() throws Exception {
        // Given
        SaleActivationRequest request = new SaleActivationRequest(
            "TEST-PROD-001",
            25000L,
            "INR",
            "1234567890123456",
            "INV-TEST-005",
            "TXN-TEST-005",
            "STR001",
            "POS-001",
            Instant.now()
        );
        
        // When & Then - No authentication
        mockMvc.perform(post("/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}