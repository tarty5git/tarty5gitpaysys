package com.payment.platform.core.dto;

import com.payment.platform.core.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundResponse(
        String refundTransactionId,
        String originalTransactionId,
        String gatewayRefundId,
        TransactionStatus status,
        BigDecimal amount,
        String handlerId,
        String message,
        Instant timestamp
) {}
