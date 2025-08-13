package com.company.uavs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class SaleActivationRequest {
    
    @JsonProperty("product_ean")
    @NotBlank(message = "Product EAN is required")
    @Size(max = 50, message = "Product EAN must not exceed 50 characters")
    private String productEan;
    
    @JsonProperty("amount")
    @NotNull(message = "Amount is required")
    private Long amount;
    
    @JsonProperty("currency")
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    private String currency;
    
    @JsonProperty("card_no")
    @Size(max = 100, message = "Card number must not exceed 100 characters")
    private String cardNo;
    
    @JsonProperty("invoice_no")
    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNo;
    
    @JsonProperty("unique_ref")
    @NotBlank(message = "Unique reference is required")
    @Size(max = 100, message = "Unique reference must not exceed 100 characters")
    private String uniqueRef;
    
    @JsonProperty("store_code")
    @NotBlank(message = "Store code is required")
    @Size(max = 20, message = "Store code must not exceed 20 characters")
    private String storeCode;
    
    @JsonProperty("device_id")
    @NotBlank(message = "Device ID is required")
    @Size(max = 50, message = "Device ID must not exceed 50 characters")
    private String deviceId;
    
    @JsonProperty("txn_date")
    @NotNull(message = "Transaction date is required")
    private Instant txnDate;
    
    // Constructors
    public SaleActivationRequest() {}
    
    public SaleActivationRequest(String productEan, Long amount, String currency, String cardNo,
                               String invoiceNo, String uniqueRef, String storeCode, 
                               String deviceId, Instant txnDate) {
        this.productEan = productEan;
        this.amount = amount;
        this.currency = currency;
        this.cardNo = cardNo;
        this.invoiceNo = invoiceNo;
        this.uniqueRef = uniqueRef;
        this.storeCode = storeCode;
        this.deviceId = deviceId;
        this.txnDate = txnDate;
    }
    
    // Getters and Setters
    public String getProductEan() {
        return productEan;
    }
    
    public void setProductEan(String productEan) {
        this.productEan = productEan;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCardNo() {
        return cardNo;
    }
    
    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    
    public String getInvoiceNo() {
        return invoiceNo;
    }
    
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    
    public String getUniqueRef() {
        return uniqueRef;
    }
    
    public void setUniqueRef(String uniqueRef) {
        this.uniqueRef = uniqueRef;
    }
    
    public String getStoreCode() {
        return storeCode;
    }
    
    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public Instant getTxnDate() {
        return txnDate;
    }
    
    public void setTxnDate(Instant txnDate) {
        this.txnDate = txnDate;
    }
}