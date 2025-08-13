package com.company.uavs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sale_activation",
       indexes = {
           @Index(name = "idx_invoice_product", columnList = "invoice_no, product_ean"),
           @Index(name = "idx_status_updated", columnList = "status, updated_at"),
           @Index(name = "idx_unique_ref", columnList = "unique_ref", unique = true),
           @Index(name = "idx_txn_date", columnList = "txn_date")
       })
public class SaleActivation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "unique_ref", length = 100, nullable = false, unique = true)
    @NotBlank
    @Size(max = 100)
    private String uniqueRef;
    
    @Column(name = "product_ean", length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String productEan;
    
    @Column(name = "amount", nullable = false)
    @NotNull
    private Long amount;
    
    @Column(name = "currency", length = 3, nullable = false)
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
    
    @Column(name = "card_no", length = 100)
    @Size(max = 100)
    private String cardNo;
    
    @Column(name = "invoice_no", length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String invoiceNo;
    
    @Column(name = "store_code", length = 20, nullable = false)
    @NotBlank
    @Size(max = 20)
    private String storeCode;
    
    @Column(name = "device_id", length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String deviceId;
    
    @Column(name = "txn_date", nullable = false)
    @NotNull
    private Instant txnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private TransactionStatus status = TransactionStatus.NEW;
    
    @Column(name = "activation_code", length = 255)
    @Size(max = 255)
    private String activationCode;
    
    @Column(name = "void_code", length = 255)
    @Size(max = 255)
    private String voidCode;
    
    @Column(name = "tnc", columnDefinition = "TEXT")
    private String tnc;
    
    @Column(name = "activation_steps", columnDefinition = "TEXT")
    private String activationSteps;
    
    @Column(name = "vendor_raw_request", columnDefinition = "TEXT")
    private String vendorRawRequest;
    
    @Column(name = "vendor_raw_response", columnDefinition = "TEXT")
    private String vendorRawResponse;
    
    @Column(name = "error_code", length = 10)
    @Size(max = 10)
    private String errorCode;
    
    @Column(name = "error_message", length = 500)
    @Size(max = 500)
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // Many-to-one relationship with ProductCatalog
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_ean", referencedColumnName = "product_ean", insertable = false, updatable = false)
    private ProductCatalog product;
    
    // Constructors
    public SaleActivation() {}
    
    public SaleActivation(String uniqueRef, String productEan, Long amount, String currency,
                         String invoiceNo, String storeCode, String deviceId, Instant txnDate) {
        this.uniqueRef = uniqueRef;
        this.productEan = productEan;
        this.amount = amount;
        this.currency = currency;
        this.invoiceNo = invoiceNo;
        this.storeCode = storeCode;
        this.deviceId = deviceId;
        this.txnDate = txnDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUniqueRef() {
        return uniqueRef;
    }
    
    public void setUniqueRef(String uniqueRef) {
        this.uniqueRef = uniqueRef;
    }
    
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
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getActivationCode() {
        return activationCode;
    }
    
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
    
    public String getVoidCode() {
        return voidCode;
    }
    
    public void setVoidCode(String voidCode) {
        this.voidCode = voidCode;
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
    
    public String getVendorRawRequest() {
        return vendorRawRequest;
    }
    
    public void setVendorRawRequest(String vendorRawRequest) {
        this.vendorRawRequest = vendorRawRequest;
    }
    
    public String getVendorRawResponse() {
        return vendorRawResponse;
    }
    
    public void setVendorRawResponse(String vendorRawResponse) {
        this.vendorRawResponse = vendorRawResponse;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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
    
    public ProductCatalog getProduct() {
        return product;
    }
    
    public void setProduct(ProductCatalog product) {
        this.product = product;
    }
}