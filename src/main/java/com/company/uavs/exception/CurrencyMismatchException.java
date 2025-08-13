package com.company.uavs.exception;

public class CurrencyMismatchException extends UavsException {
    
    public CurrencyMismatchException(String requestCurrency, String productCurrency) {
        super("SA02", String.format("Currency mismatch: requested %s but product requires %s", 
                                  requestCurrency, productCurrency));
    }
}