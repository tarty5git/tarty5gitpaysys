package com.cth.job.core.dto;

import com.cth.job.core.enums.PaymentCurrency;
import com.cth.job.core.enums.PaymentProvider;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentRequest(
        String transactionId,
        BigDecimal amount,
        PaymentCurrency currency,
        PaymentProvider provider,
        String customerId,
        Map<String, String> metadata
) {}
