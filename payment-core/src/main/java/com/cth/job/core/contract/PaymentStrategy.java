package com.cth.job.core.contract;

import com.cth.job.core.dto.PaymentRequest;
import com.cth.job.core.dto.PaymentResponse;
import com.cth.job.core.dto.RefundRequest;
import com.cth.job.core.dto.RefundResponse;
import com.cth.job.core.enums.PaymentProvider;

public interface PaymentStrategy {
    PaymentResponse processPayment(PaymentRequest request);
    RefundResponse refundPayment(RefundRequest request);
    PaymentProvider getProvider();
    boolean isEnabled();
    int getActiveHandlerCount();
}
