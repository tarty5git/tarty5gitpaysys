package com.payment.platform.core.dto;

import com.payment.platform.core.enums.PaymentProvider;

import java.math.BigDecimal;

public record RefundRequest(
        String originalTransactionId,
        String refundTransactionId,
        BigDecimal amount,
        PaymentProvider provider,
        String reason
) {}
