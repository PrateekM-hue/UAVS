package com.company.uavs.exception;

public class ProductNotFoundException extends UavsException {
    
    public ProductNotFoundException(String productEan) {
        super("SA01", "Unknown product_ean: " + productEan);
    }
}