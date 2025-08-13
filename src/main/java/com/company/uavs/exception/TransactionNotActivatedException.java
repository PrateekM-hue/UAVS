package com.company.uavs.exception;

public class TransactionNotActivatedException extends UavsException {
    
    public TransactionNotActivatedException(String uniqueRef) {
        super("VD01", "Product is not activated: " + uniqueRef);
    }
}