package com.payment.platform.core.exception;

public class PaymentProviderNotSupportedException extends RuntimeException {
    public PaymentProviderNotSupportedException(String message) {
        super(message);
    }
}
