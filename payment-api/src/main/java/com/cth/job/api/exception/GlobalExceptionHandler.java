package com.cth.job.api.exception;

import com.cth.job.core.exception.PaymentCapacityExceededException;
import com.cth.job.core.exception.PaymentProviderNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentProviderNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleProviderNotSupported(PaymentProviderNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "PROVIDER_NOT_SUPPORTED",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(PaymentCapacityExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCapacityExceeded(PaymentCapacityExceededException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "CAPACITY_EXCEEDED",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.",
                "timestamp", Instant.now().toString()
        ));
    }
}
