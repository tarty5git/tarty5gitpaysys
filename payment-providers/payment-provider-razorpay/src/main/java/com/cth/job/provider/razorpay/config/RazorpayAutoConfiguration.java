package com.cth.job.provider.razorpay.config;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.provider.razorpay.handler.RazorpayPaymentHandler;
import com.cth.job.provider.razorpay.router.RazorpayPaymentRouter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(RazorpayProperties.class)
@ConditionalOnProperty(prefix = "payment.gateway.razorpay", name = "enabled", havingValue = "true", matchIfMissing = false)
public class RazorpayAutoConfiguration {

    @Bean
    public RazorpayPaymentRouter razorpayPaymentRouter(RazorpayProperties properties) {
        List<PaymentHandler> handlers = new ArrayList<>();
        for (int i = 1; i <= properties.getPoolSize(); i++) {
            handlers.add(new RazorpayPaymentHandler("razorpay-handler-" + i, properties));
        }
        return new RazorpayPaymentRouter(handlers, properties.isEnabled());
    }
}
