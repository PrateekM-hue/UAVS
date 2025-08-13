package com.company.uavs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VoidRequest {
    
    @JsonProperty("unique_ref")
    @NotBlank(message = "Unique reference is required")
    @Size(max = 100, message = "Unique reference must not exceed 100 characters")
    private String uniqueRef;
    
    @JsonProperty("invoice_no")
    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNo;
    
    @JsonProperty("product_ean")
    @NotBlank(message = "Product EAN is required")
    @Size(max = 50, message = "Product EAN must not exceed 50 characters")
    private String productEan;
    
    // Constructors
    public VoidRequest() {}
    
    public VoidRequest(String uniqueRef, String invoiceNo, String productEan) {
        this.uniqueRef = uniqueRef;
        this.invoiceNo = invoiceNo;
        this.productEan = productEan;
    }
    
    // Getters and Setters
    public String getUniqueRef() {
        return uniqueRef;
    }
    
    public void setUniqueRef(String uniqueRef) {
        this.uniqueRef = uniqueRef;
    }
    
    public String getInvoiceNo() {
        return invoiceNo;
    }
    
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    
    public String getProductEan() {
        return productEan;
    }
    
    public void setProductEan(String productEan) {
        this.productEan = productEan;
    }
}