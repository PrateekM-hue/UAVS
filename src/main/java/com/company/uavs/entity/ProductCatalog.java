package com.company.uavs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "product_catalog")
public class ProductCatalog {
    
    @Id
    @Column(name = "product_ean", length = 50)
    @NotBlank
    @Size(max = 50)
    private String productEan;
    
    @Column(name = "category", length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String category;
    
    @Column(name = "description", length = 255)
    @Size(max = 255)
    private String description;
    
    @Column(name = "currency", length = 3, nullable = false)
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
    
    @Column(name = "vendor_endpoint", length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String vendorEndpoint;
    
    @Column(name = "is_posa", nullable = false)
    @NotNull
    private Boolean isPosa;
    
    @Column(name = "active", nullable = false)
    @NotNull
    private Boolean active = true;
    
    @Column(name = "min_amount")
    private Long minAmount;
    
    @Column(name = "max_amount")
    private Long maxAmount;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // Constructors
    public ProductCatalog() {}
    
    public ProductCatalog(String productEan, String category, String description, 
                         String currency, String vendorEndpoint, Boolean isPosa) {
        this.productEan = productEan;
        this.category = category;
        this.description = description;
        this.currency = currency;
        this.vendorEndpoint = vendorEndpoint;
        this.isPosa = isPosa;
    }
    
    // Getters and Setters
    public String getProductEan() {
        return productEan;
    }
    
    public void setProductEan(String productEan) {
        this.productEan = productEan;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getVendorEndpoint() {
        return vendorEndpoint;
    }
    
    public void setVendorEndpoint(String vendorEndpoint) {
        this.vendorEndpoint = vendorEndpoint;
    }
    
    public Boolean getIsPosa() {
        return isPosa;
    }
    
    public void setIsPosa(Boolean isPosa) {
        this.isPosa = isPosa;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Long getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }
    
    public Long getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}