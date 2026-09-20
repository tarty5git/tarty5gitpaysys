# Functional Specification
## CTH Job Extensible High-Throughput International Payment Processing Platform

---

### 1. Executive Summary
The CTH Job Payment Processing Platform is an enterprise-grade, multi-tenant capable Java 21 / Spring Boot 3 multi-module application. It supports auto-discoverable international payment gateways, dynamic handler pool scaling, multi-database support (Oracle, PostgreSQL, SQL Server), DB/LDAP Active Directory authentication, SMS/Email OTP Multi-Factor Authentication, and real-time dashboard analytics.

---

### 2. Architecture & Modules

* **`payment-core`**: Core domain DTOs, records, enums, interfaces (`PaymentStrategy`, `PaymentRouter`, `PaymentHandler`), and JPA entity abstractions (`User`).
* **`payment-providers`**: Modular parent aggregator containing isolated gateway sub-modules (`payment-provider-stripe`, `payment-provider-paypal`, `payment-provider-klarna`, `payment-provider-razorpay`).
* **`payment-service`**: Dynamic gateway lookup registry (`PaymentGatewayRegistry`), user management (`UserService`), authentication services (`AuthService`), MFA (`MfaService`), and reporting (`ReportService`).
* **`payment-api`**: Spring Boot application runner, security filter chain (`SecurityConfig`), OpenAPI/Swagger UI endpoints, REST API controllers, and SSE metrics streaming.

---

### 3. Key Functional Features

1. **Multi-Module Classpath Gateway Discovery**:
   - Every payment gateway JAR includes `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
   - Adding a gateway JAR onto the classpath dynamically registers its strategy into `PaymentGatewayRegistry` without modifying platform source code.

2. **Router and Worker Handler Pool Architecture**:
   - Each gateway router (`StripePaymentRouter`, `PaypalPaymentRouter`, etc.) manages a thread-safe pool of worker instances (`PaymentHandler`).
   - Load balances requests using bitwise overflow-safe round-robin logic (`(index & Integer.MAX_VALUE) % poolSize`).
   - Supports live dynamic scaling (`POST /api/v1/payments/providers/{provider}/scale?count={N}`).

3. **Multi-Database Support**:
   - Configurable for **Oracle Database**, **PostgreSQL**, and **Microsoft SQL Server**.
   - Standalone DDL creation scripts provided in `db/scripts/`.

4. **Security & Authentication**:
   - Configurable authentication via `app.auth.provider` (`DB` vs `LDAP` Active Directory).
   - Account creation, locking, unlocking, password expiration enforcement (90 days), and password reset.
   - SMS/Email OTP MFA with toggle switch (`app.mfa.enabled`).

5. **Real-time Dashboard & Reporting**:
   - Server-Sent Events (SSE) stream at `/api/v1/dashboard/stream` providing live TPS and active worker counts.
   - Configurable transaction volume and SLA performance reports.
