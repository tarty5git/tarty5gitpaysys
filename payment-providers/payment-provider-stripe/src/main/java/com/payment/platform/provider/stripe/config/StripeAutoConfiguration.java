package com.payment.platform.provider.stripe.config;

import com.payment.platform.core.contract.PaymentHandler;
import com.payment.platform.provider.stripe.handler.StripePaymentHandler;
import com.payment.platform.provider.stripe.router.StripePaymentRouter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(StripeProperties.class)
@ConditionalOnProperty(prefix = "payment.gateway.stripe", name = "enabled", havingValue = "true", matchIfMissing = false)
public class StripeAutoConfiguration {

    @Bean
    public StripePaymentRouter stripePaymentRouter(StripeProperties properties) {
        List<PaymentHandler> handlers = new ArrayList<>();
        for (int i = 1; i <= properties.getPoolSize(); i++) {
            handlers.add(new StripePaymentHandler("stripe-handler-" + i, properties));
        }
        return new StripePaymentRouter(handlers, properties.isEnabled());
    }
}
