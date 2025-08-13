package com.company.uavs.exception;

public class TransactionNotFoundException extends UavsException {
    
    public TransactionNotFoundException(String uniqueRef) {
        super("TXN01", "Unique reference not found: " + uniqueRef);
    }
}