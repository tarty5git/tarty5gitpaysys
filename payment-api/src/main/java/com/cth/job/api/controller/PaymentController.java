package com.cth.job.api.controller;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.core.contract.PaymentStrategy;
import com.cth.job.core.dto.PaymentRequest;
import com.cth.job.core.dto.PaymentResponse;
import com.cth.job.core.dto.RefundRequest;
import com.cth.job.core.dto.RefundResponse;
import com.cth.job.core.enums.PaymentProvider;
import com.cth.job.provider.klarna.config.KlarnaProperties;
import com.cth.job.provider.klarna.handler.KlarnaPaymentHandler;
import com.cth.job.provider.paypal.config.PaypalProperties;
import com.cth.job.provider.paypal.handler.PaypalPaymentHandler;
import com.cth.job.provider.razorpay.config.RazorpayProperties;
import com.cth.job.provider.razorpay.handler.RazorpayPaymentHandler;
import com.cth.job.provider.stripe.config.StripeProperties;
import com.cth.job.provider.stripe.handler.StripePaymentHandler;
import com.cth.job.service.PaymentGatewayRegistry;
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
