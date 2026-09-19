package com.payment.platform.api.controller;

import com.payment.platform.core.contract.PaymentHandler;
import com.payment.platform.core.contract.PaymentStrategy;
import com.payment.platform.core.dto.PaymentRequest;
import com.payment.platform.core.dto.PaymentResponse;
import com.payment.platform.core.dto.RefundRequest;
import com.payment.platform.core.dto.RefundResponse;
import com.payment.platform.core.enums.PaymentProvider;
import com.payment.platform.provider.klarna.config.KlarnaProperties;
import com.payment.platform.provider.klarna.handler.KlarnaPaymentHandler;
import com.payment.platform.provider.paypal.config.PaypalProperties;
import com.payment.platform.provider.paypal.handler.PaypalPaymentHandler;
import com.payment.platform.provider.razorpay.config.RazorpayProperties;
import com.payment.platform.provider.razorpay.handler.RazorpayPaymentHandler;
import com.payment.platform.provider.stripe.config.StripeProperties;
import com.payment.platform.provider.stripe.handler.StripePaymentHandler;
import com.payment.platform.service.PaymentGatewayRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentGatewayRegistry registry;

    @Autowired(required = false)
    private StripeProperties stripeProperties;

    @Autowired(required = false)
    private PaypalProperties paypalProperties;

    @Autowired(required = false)
    private KlarnaProperties klarnaProperties;

    @Autowired(required = false)
    private RazorpayProperties razorpayProperties;

    public PaymentController(PaymentGatewayRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        PaymentStrategy strategy = registry.getStrategy(request.provider());
        PaymentResponse response = strategy.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> refundPayment(@RequestBody RefundRequest request) {
        PaymentStrategy strategy = registry.getStrategy(request.provider());
        RefundResponse response = strategy.refundPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/providers")
    public ResponseEntity<Map<PaymentProvider, PaymentGatewayRegistry.ProviderStatus>> getProviders() {
        return ResponseEntity.ok(registry.getMonitoringStatus());
    }

    @PostMapping("/providers/{provider}/scale")
    public ResponseEntity<Map<String, Object>> scaleProvider(
            @PathVariable PaymentProvider provider,
            @RequestParam("count") int targetCount) {

        int newCount = registry.scaleProvider(provider, targetCount, this::createHandler);

        return ResponseEntity.ok(Map.of(
                "provider", provider,
                "targetCount", targetCount,
                "activeHandlerCount", newCount,
                "message", "Provider " + provider + " handler pool scaled to " + newCount
        ));
    }

    private PaymentHandler createHandler(PaymentProvider provider, String handlerId) {
        return switch (provider) {
            case STRIPE -> new StripePaymentHandler(handlerId, stripeProperties != null ? stripeProperties : new StripeProperties());
            case PAYPAL -> new PaypalPaymentHandler(handlerId, paypalProperties != null ? paypalProperties : new PaypalProperties());
            case KLARNA -> new KlarnaPaymentHandler(handlerId, klarnaProperties != null ? klarnaProperties : new KlarnaProperties());
            case RAZORPAY -> new RazorpayPaymentHandler(handlerId, razorpayProperties != null ? razorpayProperties : new RazorpayProperties());
        };
    }
}
