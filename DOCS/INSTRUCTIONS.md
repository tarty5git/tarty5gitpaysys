# System Installation & Deployment Instructions
## CTH Job Extensible High-Throughput International Payment Processing Platform

---

### 1. Database Configuration

1. Select your target relational database engine: **Oracle**, **PostgreSQL**, or **MS SQL Server**.
2. Run the corresponding DDL script located in `db/scripts/`:
   - Oracle: `db/scripts/schema-oracle.sql`
   - PostgreSQL: `db/scripts/schema-postgresql.sql`
   - MS SQL Server: `db/scripts/schema-sqlserver.sql`

3. Configure environment variables or update `payment-api/src/main/resources/application.yml`:

#### PostgreSQL Example:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/paymentdb
    username: postgres
    password: secretpassword
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

#### Oracle Example:
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:orcl
    username: cth_user
    password: secretpassword
    driver-class-name: oracle.jdbc.OracleDriver
  jpa:
    database-platform: org.hibernate.dialect.OracleDialect
```

---

### 2. Authentication Mode Configuration

To configure Active Directory / LDAP vs Database authentication, adjust `app.auth.provider` in `application.yml`:

```yaml
app:
  name: "CTH Job Payment Platform"
  auth:
    provider: "DB" # Set to "LDAP" for Active Directory authentication
    ldap:
      url: "ldap://ad.cth.com:389"
      base-dn: "dc=cth,dc=com"
      user-dn-pattern: "uid={0},ou=users"
```

---

### 3. Packaging & Environment Execution

- Run `build.bat` (Windows) or `./mvnw clean package` (Linux) to produce executable JARs.
- Executable Spring Boot JAR location: `payment-api/target/payment-api-1.0.0-SNAPSHOT.jar`.
- Execute `./start.sh` (Linux) or `start.bat` (Windows) to launch application service in background.
- Stop service via `./stop.sh` or `stop.bat`.
