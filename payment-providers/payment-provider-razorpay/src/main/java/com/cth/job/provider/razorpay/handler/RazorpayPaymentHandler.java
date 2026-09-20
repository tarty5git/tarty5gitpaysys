package com.cth.job.provider.razorpay.handler;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.core.dto.PaymentRequest;
import com.cth.job.core.dto.PaymentResponse;
import com.cth.job.core.dto.RefundRequest;
import com.cth.job.core.dto.RefundResponse;
import com.cth.job.core.enums.TransactionStatus;
import com.cth.job.provider.razorpay.config.RazorpayProperties;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class RazorpayPaymentHandler implements PaymentHandler {

    private final String handlerId;
    private final RazorpayProperties properties;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public RazorpayPaymentHandler(String handlerId, RazorpayProperties properties) {
        this.handlerId = handlerId;
        this.properties = properties;
    }

    @Override
    public String getHandlerId() {
        return handlerId;
    }

    @Override
    public PaymentResponse execute(PaymentRequest request) {
        String gatewayRef = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentResponse(
                request.transactionId(),
                gatewayRef,
                TransactionStatus.SUCCESS,
                request.amount(),
                request.currency() != null ? request.currency().name() : "INR",
                handlerId,
                "Razorpay payment processed successfully by " + handlerId,
                Instant.now()
        );
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        String gatewayRefundId = "rfnd_" + UUID.randomUUID().toString().substring(0, 8);
        return new RefundResponse(
                request.refundTransactionId(),
                request.originalTransactionId(),
                gatewayRefundId,
                TransactionStatus.REFUNDED,
                request.amount(),
                handlerId,
                "Razorpay refund processed successfully by " + handlerId,
                Instant.now()
        );
    }

    @Override
    public boolean isAvailable() {
        return active.get() && properties.isEnabled();
    }

    public void setAvailable(boolean available) {
        this.active.set(available);
    }
}
