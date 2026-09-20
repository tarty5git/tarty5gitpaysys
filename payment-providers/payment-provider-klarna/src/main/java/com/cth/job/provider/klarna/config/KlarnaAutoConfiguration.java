package com.cth.job.provider.klarna.config;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.provider.klarna.handler.KlarnaPaymentHandler;
import com.cth.job.provider.klarna.router.KlarnaPaymentRouter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(KlarnaProperties.class)
@ConditionalOnProperty(prefix = "payment.gateway.klarna", name = "enabled", havingValue = "true", matchIfMissing = false)
public class KlarnaAutoConfiguration {

    @Bean
    public KlarnaPaymentRouter klarnaPaymentRouter(KlarnaProperties properties) {
        List<PaymentHandler> handlers = new ArrayList<>();
        for (int i = 1; i <= properties.getPoolSize(); i++) {
            handlers.add(new KlarnaPaymentHandler("klarna-handler-" + i, properties));
        }
        return new KlarnaPaymentRouter(handlers, properties.isEnabled());
    }
}
