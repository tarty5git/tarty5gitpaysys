package com.payment.platform.service;

import com.payment.platform.core.contract.PaymentStrategy;
import com.payment.platform.core.enums.PaymentProvider;
import com.payment.platform.core.exception.PaymentProviderNotSupportedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentGatewayRegistryTest {

    @Test
    void testDisabledGatewayIsExcludedFromRegistry() {
        PaymentStrategy stripeStrategy = mock(PaymentStrategy.class);
        when(stripeStrategy.getProvider()).thenReturn(PaymentProvider.STRIPE);
        when(stripeStrategy.isEnabled()).thenReturn(true);

        PaymentStrategy klarnaStrategy = mock(PaymentStrategy.class);
        when(klarnaStrategy.getProvider()).thenReturn(PaymentProvider.KLARNA);
        when(klarnaStrategy.isEnabled()).thenReturn(false);

        PaymentGatewayRegistry registry = new PaymentGatewayRegistry(List.of(stripeStrategy, klarnaStrategy));

        assertTrue(registry.getAvailableProviders().contains(PaymentProvider.STRIPE));
        assertFalse(registry.getAvailableProviders().contains(PaymentProvider.KLARNA));

        assertNotNull(registry.getStrategy(PaymentProvider.STRIPE));
        assertThrows(PaymentProviderNotSupportedException.class, () -> registry.getStrategy(PaymentProvider.KLARNA));
    }
}
