package com.cth.job.core.dto;

import com.cth.job.core.enums.PaymentProvider;

import java.math.BigDecimal;

public record RefundRequest(
        String originalTransactionId,
        String refundTransactionId,
        BigDecimal amount,
        PaymentProvider provider,
        String reason
) {}
