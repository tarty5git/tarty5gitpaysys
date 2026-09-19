package com.payment.platform.provider.paypal.config;

import com.payment.platform.core.contract.PaymentHandler;
import com.payment.platform.provider.paypal.handler.PaypalPaymentHandler;
import com.payment.platform.provider.paypal.router.PaypalPaymentRouter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(PaypalProperties.class)
@ConditionalOnProperty(prefix = "payment.gateway.paypal", name = "enabled", havingValue = "true", matchIfMissing = false)
public class PaypalAutoConfiguration {

    @Bean
    public PaypalPaymentRouter paypalPaymentRouter(PaypalProperties properties) {
        List<PaymentHandler> handlers = new ArrayList<>();
        for (int i = 1; i <= properties.getPoolSize(); i++) {
            handlers.add(new PaypalPaymentHandler("paypal-handler-" + i, properties));
        }
        return new PaypalPaymentRouter(handlers, properties.isEnabled());
    }
}
