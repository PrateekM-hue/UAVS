package com.company.uavs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SaleActivationResponse {
    
    @JsonProperty("product_ean")
    private String productEan;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("card_no")
    private String cardNo;
    
    @JsonProperty("invoice_no")
    private String invoiceNo;
    
    @JsonProperty("unique_ref")
    private String uniqueRef;
    
    @JsonProperty("activation_code")
    private String activationCode;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("tnc")
    private String tnc;
    
    @JsonProperty("activation_steps")
    private String activationSteps;
    
    // Constructors
    public SaleActivationResponse() {}
    
    public SaleActivationResponse(String productEan, Long amount, String currency, String cardNo,
                                String invoiceNo, String uniqueRef, String activationCode,
                                String status, String tnc, String activationSteps) {
        this.productEan = productEan;
        this.amount = amount;
        this.currency = currency;
        this.cardNo = cardNo;
        this.invoiceNo = invoiceNo;
        this.uniqueRef = uniqueRef;
        this.activationCode = activationCode;
        this.status = status;
        this.tnc = tnc;
        this.activationSteps = activationSteps;
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
    
    public String getActivationCode() {
        return activationCode;
    }
    
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTnc() {
        return tnc;
    }
    
    public void setTnc(String tnc) {
        this.tnc = tnc;
    }
    
    public String getActivationSteps() {
        return activationSteps;
    }
    
    public void setActivationSteps(String activationSteps) {
        this.activationSteps = activationSteps;
    }
}