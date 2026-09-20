package com.cth.job.core.contract;

import com.cth.job.core.dto.PaymentRequest;
import com.cth.job.core.dto.PaymentResponse;
import com.cth.job.core.dto.RefundRequest;
import com.cth.job.core.dto.RefundResponse;

public interface PaymentHandler {
    String getHandlerId();
    PaymentResponse execute(PaymentRequest request);
    RefundResponse refund(RefundRequest request);
    boolean isAvailable(); // Capacity and health check
}
