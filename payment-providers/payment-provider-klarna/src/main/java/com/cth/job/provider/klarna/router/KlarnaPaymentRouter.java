package com.cth.job.provider.klarna.router;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.core.contract.PaymentRouter;
import com.cth.job.core.dto.PaymentRequest;
import com.cth.job.core.dto.PaymentResponse;
import com.cth.job.core.dto.RefundRequest;
import com.cth.job.core.dto.RefundResponse;
import com.cth.job.core.enums.PaymentProvider;
import com.cth.job.core.exception.PaymentCapacityExceededException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class KlarnaPaymentRouter implements PaymentRouter {

    private final List<PaymentHandler> handlers;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final boolean enabled;

    public KlarnaPaymentRouter(List<PaymentHandler> initialHandlers) {
        this(initialHandlers, true);
    }

    public KlarnaPaymentRouter(List<PaymentHandler> initialHandlers, boolean enabled) {
        this.handlers = new CopyOnWriteArrayList<>(initialHandlers != null ? initialHandlers : Collections.emptyList());
        this.enabled = enabled;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        PaymentHandler handler = selectAvailableHandler();
        return handler.execute(request);
    }

    @Override
    public RefundResponse refundPayment(RefundRequest request) {
        PaymentHandler handler = selectAvailableHandler();
        return handler.refund(request);
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.KLARNA;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getActiveHandlerCount() {
        return (int) handlers.stream().filter(PaymentHandler::isAvailable).count();
    }

    @Override
    public void registerHandler(PaymentHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }

    @Override
    public void removeHandler(String handlerId) {
        handlers.removeIf(h -> h.getHandlerId().equals(handlerId));
    }

    @Override
    public List<PaymentHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    private PaymentHandler selectAvailableHandler() {
        List<PaymentHandler> availableHandlers = handlers.stream()
                .filter(PaymentHandler::isAvailable)
                .toList();

        if (availableHandlers.isEmpty()) {
            throw new PaymentCapacityExceededException("Klarna capacity exceeded: No available payment handlers in pool.");
        }

        int index = (roundRobinIndex.getAndIncrement() & Integer.MAX_VALUE) % availableHandlers.size();
        return availableHandlers.get(index);
    }
}
