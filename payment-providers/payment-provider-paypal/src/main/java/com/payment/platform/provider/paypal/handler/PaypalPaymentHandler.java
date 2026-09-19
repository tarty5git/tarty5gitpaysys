package com.payment.platform.provider.paypal.handler;

import com.payment.platform.core.contract.PaymentHandler;
import com.payment.platform.core.dto.PaymentRequest;
import com.payment.platform.core.dto.PaymentResponse;
import com.payment.platform.core.dto.RefundRequest;
import com.payment.platform.core.dto.RefundResponse;
import com.payment.platform.core.enums.TransactionStatus;
import com.payment.platform.provider.paypal.config.PaypalProperties;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaypalPaymentHandler implements PaymentHandler {

    private final String handlerId;
    private final PaypalProperties properties;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public PaypalPaymentHandler(String handlerId, PaypalProperties properties) {
        this.handlerId = handlerId;
        this.properties = properties;
    }

    @Override
    public String getHandlerId() {
        return handlerId;
    }

    @Override
    public PaymentResponse execute(PaymentRequest request) {
        String gatewayRef = "PAYID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResponse(
                request.transactionId(),
                gatewayRef,
                TransactionStatus.SUCCESS,
                request.amount(),
                request.currency() != null ? request.currency().name() : "USD",
                handlerId,
                "PayPal payment processed successfully by " + handlerId,
                Instant.now()
        );
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        String gatewayRefundId = "REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new RefundResponse(
                request.refundTransactionId(),
                request.originalTransactionId(),
                gatewayRefundId,
                TransactionStatus.REFUNDED,
                request.amount(),
                handlerId,
                "PayPal refund processed successfully by " + handlerId,
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
