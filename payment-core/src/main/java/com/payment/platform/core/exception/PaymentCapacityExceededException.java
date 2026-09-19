package com.payment.platform.core.exception;

public class PaymentCapacityExceededException extends RuntimeException {
    public PaymentCapacityExceededException(String message) {
        super(message);
    }
}
