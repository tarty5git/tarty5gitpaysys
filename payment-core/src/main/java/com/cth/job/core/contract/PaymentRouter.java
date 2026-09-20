package com.cth.job.core.contract;

import java.util.List;

public interface PaymentRouter extends PaymentStrategy {
    void registerHandler(PaymentHandler handler);
    void removeHandler(String handlerId);
    List<PaymentHandler> getHandlers();
}
