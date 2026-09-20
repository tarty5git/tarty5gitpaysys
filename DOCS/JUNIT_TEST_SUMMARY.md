# JUnit Test Cases Summary & Verification Matrix

---

## 1. Overview
The platform contains automated JUnit 5 test suites covering concurrent worker pool distribution, strategy registry filtering, security, and exception handling.

---

## 2. Test Execution Commands

```bash
# Execute all unit and integration tests across all modules
./mvnw test
```

---

## 3. Test Cases Summary

| Module | Test Class | Test Case Method | Description / Expected Result |
| :--- | :--- | :--- | :--- |
| `payment-provider-stripe` | `StripePaymentRouterTest` | `testConcurrentRoundRobinDistributionAcrossThreeWorkers` | Simulates 10 concurrent payment requests across 3 active Stripe worker instances. Asserts overflow-safe round-robin distribution and verifies all workers process requests. |
| `payment-service` | `PaymentGatewayRegistryTest` | `testDisabledGatewayIsExcludedFromRegistry` | Mocks active and disabled payment strategies (`enabled: false`). Verifies disabled gateways are excluded from active strategy lookup and throws `PaymentProviderNotSupportedException`. |
