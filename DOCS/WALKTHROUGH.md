# System Walkthrough & User Guide
## CTH Job Extensible High-Throughput International Payment Processing Platform

---

### 1. Prerequisites
- **Java**: OpenJDK 21 LTS
- **Maven**: 3.9+ (Wrapper `./mvnw` or `mvnw.cmd` included)
- **Database**: Oracle 19c/21c/23c, PostgreSQL 14+, or Microsoft SQL Server 2019+

---

### 2. Building the Platform

#### On Windows:
```cmd
build.bat
```

#### On Linux / macOS:
```bash
./mvnw clean package
```

---

### 3. Running and Stopping the Platform

#### Windows:
- **Start**: `start.bat`
- **Stop**: `stop.bat`

#### Linux / macOS:
- **Start**: `./start.sh`
- **Stop**: `./stop.sh`

---

### 4. System Endpoints & Interactive OpenAPI / Swagger UI

Access Swagger UI in browser:
```
http://localhost:8080/swagger-ui.html
```

#### Core REST Endpoints Summary:
- **App Information**: `GET /api/v1/config/app-info`
- **Authentication Login**: `POST /api/v1/auth/login`
- **User Registration**: `POST /api/v1/auth/register`
- **Lock / Unlock Account**: `POST /api/v1/auth/users/{username}/lock` | `/unlock`
- **Password Reset**: `POST /api/v1/auth/users/{username}/reset-password`
- **MFA Toggle Switch**: `POST /api/v1/auth/mfa/toggle?enabled=true`
- **Process Payment**: `POST /api/v1/payments/process`
- **Process Refund**: `POST /api/v1/payments/refund`
- **Provider Status**: `GET /api/v1/payments/providers`
- **Scale Worker Pool**: `POST /api/v1/payments/providers/{provider}/scale?count={N}`
- **Real-time Dashboard Metrics (SSE)**: `GET /api/v1/dashboard/stream`
- **Reports**: `GET /api/v1/reports/summary` | `/performance`
