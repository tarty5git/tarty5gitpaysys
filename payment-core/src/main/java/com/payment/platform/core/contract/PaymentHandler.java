package com.payment.platform.core.contract;

import com.payment.platform.core.dto.PaymentRequest;
import com.payment.platform.core.dto.PaymentResponse;
import com.payment.platform.core.dto.RefundRequest;
import com.payment.platform.core.dto.RefundResponse;

public interface PaymentHandler {
    String getHandlerId();
    PaymentResponse execute(PaymentRequest request);
    RefundResponse refund(RefundRequest request);
    boolean isAvailable(); // Capacity and health check
}
