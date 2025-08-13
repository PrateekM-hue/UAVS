package com.company.uavs.vendor;

import com.company.uavs.entity.SaleActivation;

public interface VendorAdapter {
    
    /**
     * Get the vendor name this adapter supports
     */
    String getVendorName();
    
    /**
     * Activate a product with the vendor
     */
    VendorResponse activate(SaleActivation transaction);
    
    /**
     * Void a previously activated product
     */
    VendorResponse voidTransaction(SaleActivation transaction);
    
    /**
     * Check if this adapter supports the given vendor endpoint
     */
    boolean supports(String vendorEndpoint);
}