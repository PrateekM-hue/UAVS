package com.company.uavs.exception;

import com.company.uavs.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, HttpServletRequest request) {
        logger.error("Product not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatchException(
            CurrencyMismatchException ex, HttpServletRequest request) {
        logger.error("Currency mismatch: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    @ExceptionHandler(VendorTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleVendorTimeoutException(
            VendorTimeoutException ex, HttpServletRequest request) {
        logger.error("Vendor timeout: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }
    
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(
            TransactionNotFoundException ex, HttpServletRequest request) {
        logger.error("Transaction not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(TransactionNotActivatedException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotActivatedException(
            TransactionNotActivatedException ex, HttpServletRequest request) {
        logger.error("Transaction not activated: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ReturnPeriodExceededException.class)
    public ResponseEntity<ErrorResponse> handleReturnPeriodExceededException(
            ReturnPeriodExceededException ex, HttpServletRequest request) {
        logger.error("Return period exceeded: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(UavsException.class)
    public ResponseEntity<ErrorResponse> handleUavsException(
            UavsException ex, HttpServletRequest request) {
        logger.error("UAVS exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.error("Validation errors: {}", errors);
        String message = "Validation failed: " + errors.toString();
        ErrorResponse errorResponse = new ErrorResponse("VAL01", message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.error("Type mismatch: {}", ex.getMessage());
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        ErrorResponse errorResponse = new ErrorResponse("VAL02", message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(
            TimeoutException ex, HttpServletRequest request) {
        logger.error("Timeout exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("SA04", "Request timeout", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: ", ex);
        ErrorResponse errorResponse = new ErrorResponse("SYS01", "Internal server error", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}