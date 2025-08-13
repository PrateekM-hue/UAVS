package com.company.uavs.vendor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VendorAdapterFactory {
    
    private final List<VendorAdapter> adapters;
    
    public VendorAdapterFactory(List<VendorAdapter> adapters) {
        this.adapters = adapters;
    }
    
    /**
     * Get the appropriate vendor adapter for the given vendor endpoint
     */
    public Optional<VendorAdapter> getAdapter(String vendorEndpoint) {
        return adapters.stream()
                .filter(adapter -> adapter.supports(vendorEndpoint))
                .findFirst();
    }
    
    /**
     * Get all available adapters
     */
    public List<VendorAdapter> getAllAdapters() {
        return adapters;
    }
}