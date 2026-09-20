package com.cth.job.core.exception;

public class PaymentCapacityExceededException extends RuntimeException {
    public PaymentCapacityExceededException(String message) {
        super(message);
    }
}
