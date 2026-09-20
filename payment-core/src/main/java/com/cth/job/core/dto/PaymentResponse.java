package com.cth.job.core.dto;

import com.cth.job.core.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String transactionId,
        String gatewayReferenceId,
        TransactionStatus status,
        BigDecimal amount,
        String currency,
        String handlerId,
        String message,
        Instant timestamp
) {}
