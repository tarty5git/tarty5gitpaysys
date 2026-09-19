package com.payment.platform.provider.stripe;

import com.payment.platform.core.contract.PaymentHandler;
import com.payment.platform.core.dto.PaymentRequest;
import com.payment.platform.core.dto.PaymentResponse;
import com.payment.platform.core.enums.PaymentCurrency;
import com.payment.platform.core.enums.PaymentProvider;
import com.payment.platform.provider.stripe.config.StripeProperties;
import com.payment.platform.provider.stripe.handler.StripePaymentHandler;
import com.payment.platform.provider.stripe.router.StripePaymentRouter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class StripePaymentRouterTest {

    @Test
    void testConcurrentRoundRobinDistributionAcrossThreeWorkers() throws InterruptedException, ExecutionException {
        StripeProperties properties = new StripeProperties();
        properties.setEnabled(true);
        properties.setPoolSize(3);

        List<PaymentHandler> handlers = List.of(
                new StripePaymentHandler("stripe-handler-1", properties),
                new StripePaymentHandler("stripe-handler-2", properties),
                new StripePaymentHandler("stripe-handler-3", properties)
        );

        StripePaymentRouter router = new StripePaymentRouter(handlers, true);

        int totalRequests = 10;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<PaymentResponse>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                PaymentRequest request = new PaymentRequest(
                        "tx-" + index,
                        BigDecimal.valueOf(100.00),
                        PaymentCurrency.USD,
                        PaymentProvider.STRIPE,
                        "cust-123",
                        Map.of()
                );
                return router.processPayment(request);
            }));
        }

        Map<String, Integer> handlerUsageCount = new ConcurrentHashMap<>();

        for (Future<PaymentResponse> future : futures) {
            PaymentResponse response = future.get();
            assertNotNull(response);
            assertNotNull(response.handlerId());
            handlerUsageCount.merge(response.handlerId(), 1, Integer::sum);
        }

        executor.shutdown();

        assertEquals(3, handlerUsageCount.size(), "All 3 workers should have handled requests");
        assertTrue(handlerUsageCount.get("stripe-handler-1") >= 3);
        assertTrue(handlerUsageCount.get("stripe-handler-2") >= 3);
        assertTrue(handlerUsageCount.get("stripe-handler-3") >= 3);
        assertEquals(10, handlerUsageCount.values().stream().mapToInt(Integer::intValue).sum());
    }
}
