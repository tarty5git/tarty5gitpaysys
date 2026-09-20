package com.cth.job.core.exception;

public class PaymentProviderNotSupportedException extends RuntimeException {
    public PaymentProviderNotSupportedException(String message) {
        super(message);
    }
}
