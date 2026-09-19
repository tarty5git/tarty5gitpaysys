package com.payment.platform.core.contract;

import com.payment.platform.core.dto.PaymentRequest;
import com.payment.platform.core.dto.PaymentResponse;
import com.payment.platform.core.dto.RefundRequest;
import com.payment.platform.core.dto.RefundResponse;
import com.payment.platform.core.enums.PaymentProvider;

public interface PaymentStrategy {
    PaymentResponse processPayment(PaymentRequest request);
    RefundResponse refundPayment(RefundRequest request);
    PaymentProvider getProvider();
    boolean isEnabled();
    int getActiveHandlerCount();
}
