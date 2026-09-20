package com.cth.job.service;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.core.contract.PaymentRouter;
import com.cth.job.core.contract.PaymentStrategy;
import com.cth.job.core.enums.PaymentProvider;
import com.cth.job.core.exception.PaymentProviderNotSupportedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentGatewayRegistry {

    private final Map<PaymentProvider, PaymentStrategy> strategyRegistry;

    public PaymentGatewayRegistry(List<PaymentStrategy> strategies) {
        if (strategies != null) {
            this.strategyRegistry = new ConcurrentHashMap<>(
                    strategies.stream()
                            .filter(PaymentStrategy::isEnabled)
                            .collect(Collectors.toMap(PaymentStrategy::getProvider, Function.identity()))
            );
        } else {
            this.strategyRegistry = new ConcurrentHashMap<>();
        }
    }

    public PaymentStrategy getStrategy(PaymentProvider provider) {
        PaymentStrategy strategy = strategyRegistry.get(provider);
        if (strategy == null) {
            throw new PaymentProviderNotSupportedException(
                    "Payment provider " + provider + " is not available on classpath or is disabled."
            );
        }
        return strategy;
    }

    public Set<PaymentProvider> getAvailableProviders() {
        return Collections.unmodifiableSet(strategyRegistry.keySet());
    }

    public Map<PaymentProvider, ProviderStatus> getMonitoringStatus() {
        return strategyRegistry.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ProviderStatus(
                                entry.getKey(),
                                entry.getValue().isEnabled(),
                                entry.getValue().getActiveHandlerCount(),
                                getHandlerIds(entry.getValue())
                        )
                ));
    }

    public int scaleProvider(PaymentProvider provider, int targetCount, HandlerFactory handlerFactory) {
        PaymentStrategy strategy = getStrategy(provider);
        if (!(strategy instanceof PaymentRouter router)) {
            throw new IllegalStateException("Strategy for provider " + provider + " does not support dynamic scaling.");
        }

        List<PaymentHandler> currentHandlers = router.getHandlers();
        int currentCount = currentHandlers.size();

        if (targetCount > currentCount) {
            for (int i = currentCount + 1; i <= targetCount; i++) {
                String handlerId = provider.name().toLowerCase() + "-handler-" + i;
                PaymentHandler newHandler = handlerFactory.createHandler(provider, handlerId);
                router.registerHandler(newHandler);
            }
        } else if (targetCount < currentCount) {
            int toRemove = currentCount - targetCount;
            for (int i = 0; i < toRemove; i++) {
                List<PaymentHandler> handlers = router.getHandlers();
                if (!handlers.isEmpty()) {
                    PaymentHandler handlerToRemove = handlers.get(handlers.size() - 1);
                    router.removeHandler(handlerToRemove.getHandlerId());
                }
            }
        }

        return router.getHandlers().size();
    }

    private List<String> getHandlerIds(PaymentStrategy strategy) {
        if (strategy instanceof PaymentRouter router) {
            return router.getHandlers().stream()
                    .map(PaymentHandler::getHandlerId)
                    .toList();
        }
        return Collections.emptyList();
    }

    public interface HandlerFactory {
        PaymentHandler createHandler(PaymentProvider provider, String handlerId);
    }

    public record ProviderStatus(
            PaymentProvider provider,
            boolean enabled,
            int activeHandlerCount,
            List<String> handlerIds
    ) {}
}
