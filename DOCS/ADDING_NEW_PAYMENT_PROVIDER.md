# Adding a New Payment Provider Gateway

This guide details the step-by-step procedure to add a new international payment gateway (such as **Adyen** or **Alipay**) to the payment platform.

---

## Architecture Overview

Each payment gateway is implemented as an independent Maven sub-module inside `payment-providers/`. The gateway produces a standalone JAR containing its own worker pool handlers, round-robin strategy router, Spring configuration properties, and Spring Boot Auto-Configuration class.

---

## Step 1: Create a New Maven Sub-Module

Create a directory under `payment-providers/` named `payment-provider-adyen`.

Add `payment-providers/payment-provider-adyen/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cth.job</groupId>
        <artifactId>payment-providers</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>payment-provider-adyen</artifactId>
    <name>payment-provider-adyen</name>

    <dependencies>
        <dependency>
            <groupId>com.cth.job</groupId>
            <artifactId>payment-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

Add the new module to `payment-providers/pom.xml`:

```xml
<modules>
    ...
    <module>payment-provider-adyen</module>
</modules>
```

---

## Step 2: Register Provider Enum (if applicable)

If adding a new provider enum constant, add it to `PaymentProvider.java` in `payment-core`:

```java
public enum PaymentProvider {
    STRIPE,
    PAYPAL,
    KLARNA,
    RAZORPAY,
    ADYEN
}
```

---

## Step 3: Implement Core Components

### 1. Configuration Properties (`AdyenProperties.java`)

```java
package com.cth.job.provider.adyen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.gateway.adyen")
public class AdyenProperties {
    private boolean enabled = true;
    private int poolSize = 3;
    private String apiKey;
    private String merchantAccount;

    // Getters and setters
}
```

### 2. Payment Handler Worker (`AdyenPaymentHandler.java`)

```java
package com.cth.job.provider.adyen.handler;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.core.dto.*;
import com.cth.job.core.enums.TransactionStatus;
import com.cth.job.provider.adyen.config.AdyenProperties;

import java.time.Instant;
import java.util.UUID;

public class AdyenPaymentHandler implements PaymentHandler {

    private final String handlerId;
    private final AdyenProperties properties;

    public AdyenPaymentHandler(String handlerId, AdyenProperties properties) {
        this.handlerId = handlerId;
        this.properties = properties;
    }

    @Override
    public String getHandlerId() {
        return handlerId;
    }

    @Override
    public PaymentResponse execute(PaymentRequest request) {
        return new PaymentResponse(
                request.transactionId(),
                "adyen_" + UUID.randomUUID(),
                TransactionStatus.SUCCESS,
                request.amount(),
                request.currency().name(),
                handlerId,
                "Processed via Adyen worker " + handlerId,
                Instant.now()
        );
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        return new RefundResponse(
                request.refundTransactionId(),
                request.originalTransactionId(),
                "adyen_ref_" + UUID.randomUUID(),
                TransactionStatus.REFUNDED,
                request.amount(),
                handlerId,
                "Refunded via Adyen worker " + handlerId,
                Instant.now()
        );
    }

    @Override
    public boolean isAvailable() {
        return properties.isEnabled();
    }
}
```

### 3. Payment Gateway Router (`AdyenPaymentRouter.java`)

```java
package com.cth.job.provider.adyen.router;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.core.contract.PaymentRouter;
import com.cth.job.core.dto.*;
import com.cth.job.core.enums.PaymentProvider;
import com.cth.job.core.exception.PaymentCapacityExceededException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AdyenPaymentRouter implements PaymentRouter {

    private final List<PaymentHandler> handlers;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final boolean enabled;

    public AdyenPaymentRouter(List<PaymentHandler> initialHandlers, boolean enabled) {
        this.handlers = new CopyOnWriteArrayList<>(initialHandlers);
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
        return PaymentProvider.ADYEN;
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
        if (handler != null) handlers.add(handler);
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
        List<PaymentHandler> available = handlers.stream().filter(PaymentHandler::isAvailable).toList();
        if (available.isEmpty()) {
            throw new PaymentCapacityExceededException("Adyen capacity exceeded: No available payment handlers in pool.");
        }
        int index = (roundRobinIndex.getAndIncrement() & Integer.MAX_VALUE) % available.size();
        return available.get(index);
    }
}
```

### 4. Auto-Configuration (`AdyenAutoConfiguration.java`)

```java
package com.cth.job.provider.adyen.config;

import com.cth.job.core.contract.PaymentHandler;
import com.cth.job.provider.adyen.handler.AdyenPaymentHandler;
import com.cth.job.provider.adyen.router.AdyenPaymentRouter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(AdyenProperties.class)
@ConditionalOnProperty(prefix = "payment.gateway.adyen", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AdyenAutoConfiguration {

    @Bean
    public AdyenPaymentRouter adyenPaymentRouter(AdyenProperties properties) {
        List<PaymentHandler> handlers = new ArrayList<>();
        for (int i = 1; i <= properties.getPoolSize(); i++) {
            handlers.add(new AdyenPaymentHandler("adyen-handler-" + i, properties));
        }
        return new AdyenPaymentRouter(handlers, properties.isEnabled());
    }
}
```

---

## Step 4: Register Spring Auto-Discovery

Create file `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` in `payment-provider-adyen`:

```text
com.cth.job.provider.adyen.config.AdyenAutoConfiguration
```

---

## Step 5: Add Gateway to Application

1. Add the Maven dependency to `payment-api/pom.xml`:
```xml
<dependency>
    <groupId>com.cth.job</groupId>
    <artifactId>payment-provider-adyen</artifactId>
</dependency>
```

2. Add configuration to `payment-api/src/main/resources/application.yml`:
```yaml
payment:
  gateway:
    adyen:
      enabled: true
      poolSize: 3
      api-key: "adyen_test_key"
      merchant-account: "AdyenTestMerchant"
```

3. Update `PaymentController.java` to support creating dynamic worker instances when scaling:
```java
private PaymentHandler createHandler(PaymentProvider provider, String handlerId) {
    return switch (provider) {
        ...
        case ADYEN -> new AdyenPaymentHandler(handlerId, adyenProperties != null ? adyenProperties : new AdyenProperties());
    };
}
```

---

## Step 6: Verify Build & Test

Run full build to verify integration:
```bash
./mvnw clean package
```
