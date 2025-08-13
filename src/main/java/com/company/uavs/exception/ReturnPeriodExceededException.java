package com.company.uavs.exception;

public class ReturnPeriodExceededException extends UavsException {
    
    public ReturnPeriodExceededException(String uniqueRef) {
        super("VD02", "Return period exceeded: " + uniqueRef);
    }
}