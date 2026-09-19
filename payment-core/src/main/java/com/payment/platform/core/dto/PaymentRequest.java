package com.payment.platform.core.dto;

import com.payment.platform.core.enums.PaymentCurrency;
import com.payment.platform.core.enums.PaymentProvider;

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
