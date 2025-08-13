# Universal Activation & Void Service (UAVS)

A microservice for software license and gift card activation/void operations that abstracts differences among multiple vendor platforms.

## Overview

UAVS provides a unified REST API interface for Point-of-Sale (POS) systems to:
- Activate software licenses and gift/voucher products
- Void previously activated products
- Query transaction status
- Support multiple vendor platforms (Stellr, McAfee, SafeHouse, Qwiksilver)

## Architecture

### Key Components
- **REST Controllers**: Three main APIs (Sale/Activation, Void, Get Status)
- **Service Layer**: Business logic and transaction management
- **Vendor Adapter Layer**: Strategy pattern for vendor-specific integrations
- **Data Layer**: JPA entities with PostgreSQL database
- **Security**: OAuth 2.0 JWT authentication
- **Monitoring**: Prometheus metrics, health checks, distributed tracing

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL with Liquibase migrations
- **Security**: Spring Security with OAuth 2.0 Resource Server
- **Monitoring**: Micrometer + Prometheus, OpenTelemetry
- **Documentation**: Spring REST Docs
- **Testing**: JUnit 5, TestContainers, WireMock

## API Specification

### Base URL
```
https://api.yourcompany.com/v1
```

### Authentication
All endpoints require OAuth 2.0 JWT Bearer token:
```http
Authorization: Bearer <JWT_TOKEN>
```

### API Endpoints

#### 1. Sale/Activation
```http
POST /v1/sales
Content-Type: application/json
X-Correlation-Id: <UUID>
X-Retry-Count: 0

{
  "product_ean": "MCF-AV-12M-001",
  "amount": 199900,
  "currency": "INR",
  "card_no": "1234567890123456",
  "invoice_no": "INV-2025-001",
  "unique_ref": "TXN-2025-001-001",
  "store_code": "STR001",
  "device_id": "POS-001",
  "txn_date": "2025-07-29T10:45:00Z"
}
```

**Success Response (200):**
```json
{
  "product_ean": "MCF-AV-12M-001",
  "amount": 199900,
  "currency": "INR",
  "card_no": "1234567890123456",
  "invoice_no": "INV-2025-001",
  "unique_ref": "TXN-2025-001-001",
  "activation_code": "MCF-ABC123-DEF456",
  "status": "ACTIVATED",
  "tnc": "https://mcafee.com/terms",
  "activation_steps": "Visit mcafee.com/activate and enter code"
}
```

#### 2. Void
```http
POST /v1/voids
Content-Type: application/json
X-Correlation-Id: <UUID>

{
  "unique_ref": "TXN-2025-001-001",
  "invoice_no": "INV-2025-001",
  "product_ean": "MCF-AV-12M-001"
}
```

**Success Response (200):**
```json
{
  "response_code": "00",
  "message": "Void successful",
  "status": "VOIDED",
  "unique_ref": "TXN-2025-001-001",
  "void_code": "VOID-ABC123"
}
```

#### 3. Get Status
```http
GET /v1/status/{unique_ref}
X-Correlation-Id: <UUID>
```

**Response (200):**
```json
{
  "product_ean": "MCF-AV-12M-001",
  "amount": 199900,
  "currency": "INR",
  "card_no": "1234567890123456",
  "invoice_no": "INV-2025-001",
  "unique_ref": "TXN-2025-001-001",
  "activation_code": "MCF-ABC123-DEF456",
  "status": "ACTIVATED",
  "tnc": "https://mcafee.com/terms",
  "activation_steps": "Visit mcafee.com/activate and enter code"
}
```

### Error Responses

#### Validation Error (400)
```json
{
  "error_code": "VAL01",
  "message": "Validation failed: {field: message}",
  "timestamp": "2025-07-29T10:45:00Z",
  "path": "/v1/sales"
}
```

#### Product Not Found (422)
```json
{
  "error_code": "SA01",
  "message": "Unknown product_ean: INVALID-EAN",
  "timestamp": "2025-07-29T10:45:00Z",
  "path": "/v1/sales"
}
```

#### Vendor Timeout (504)
```json
{
  "error_code": "SA04",
  "message": "Vendor timeout – pending: STELLR",
  "timestamp": "2025-07-29T10:45:00Z",
  "path": "/v1/sales"
}
```

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17+ (for local development)
- Maven 3.8+ (for local development)

### Run with Docker Compose
```bash
# Clone the repository
git clone <repository-url>
cd uavs

# Start all services
docker-compose up -d

# Check health
curl http://localhost:8080/v1/actuator/health

# View logs
docker-compose logs -f uavs
```

### Local Development Setup
```bash
# Start dependencies
docker-compose up -d postgres vault

# Run the application
mvn spring-boot:run

# Or build and run
mvn clean package
java -jar target/uavs-1.0.0.jar
```

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=uavs
DB_USERNAME=uavs_user
DB_PASSWORD=uavs_password

# Vault
VAULT_HOST=localhost
VAULT_PORT=8200
VAULT_TOKEN=dev-token

# Vendor URLs
STELLR_BASE_URL=https://api.stellr.com
MCAFEE_BASE_URL=https://api.mcafee.com
SAFEHOUSE_BASE_URL=https://api.safehouse.com
QWIKSILVER_BASE_URL=https://api.qwiksilver.com

# Logging
LOG_LEVEL=INFO
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -Dtest=**/*IntegrationTest
```

### API Testing with curl
```bash
# Get access token (replace with your OAuth setup)
export ACCESS_TOKEN="your-jwt-token"

# Test activation
curl -X POST http://localhost:8080/v1/sales \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: $(uuidgen)" \
  -d '{
    "product_ean": "MCF-AV-12M-001",
    "amount": 199900,
    "currency": "INR",
    "invoice_no": "INV-TEST-001",
    "unique_ref": "TXN-TEST-001",
    "store_code": "STR001",
    "device_id": "POS-001",
    "txn_date": "2025-07-29T10:45:00Z"
  }'

# Test status check
curl -X GET http://localhost:8080/v1/status/TXN-TEST-001 \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "X-Correlation-Id: $(uuidgen)"

# Test void
curl -X POST http://localhost:8080/v1/voids \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: $(uuidgen)" \
  -d '{
    "unique_ref": "TXN-TEST-001",
    "invoice_no": "INV-TEST-001",
    "product_ean": "MCF-AV-12M-001"
  }'
```

## Monitoring

### Metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Application Metrics**: http://localhost:8080/v1/actuator/prometheus

### Health Checks
- **Application Health**: http://localhost:8080/v1/actuator/health
- **Readiness**: http://localhost:8080/v1/actuator/health/readiness
- **Liveness**: http://localhost:8080/v1/actuator/health/liveness

### Key Metrics
- `uavs_activation_requests_total` - Total activation requests
- `uavs_activation_duration` - Activation processing time
- `uavs_vendor_timeouts_total` - Vendor timeout count
- `uavs_transactions_pending` - Pending transaction count

## Database Schema

### Product Catalog
```sql
CREATE TABLE product_catalog (
    product_ean VARCHAR(50) PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    currency VARCHAR(3) NOT NULL,
    vendor_endpoint VARCHAR(100) NOT NULL,
    is_posa BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    min_amount BIGINT,
    max_amount BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Sale Activation
```sql
CREATE TABLE sale_activation (
    id BIGSERIAL PRIMARY KEY,
    unique_ref VARCHAR(100) UNIQUE NOT NULL,
    product_ean VARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    card_no VARCHAR(100),
    invoice_no VARCHAR(50) NOT NULL,
    store_code VARCHAR(20) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    txn_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    activation_code VARCHAR(255),
    void_code VARCHAR(255),
    tnc TEXT,
    activation_steps TEXT,
    vendor_raw_request TEXT,
    vendor_raw_response TEXT,
    error_code VARCHAR(10),
    error_message VARCHAR(500),
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| SA01 | Unknown product_ean | 422 |
| SA02 | Currency mismatch | 422 |
| SA03 | Amount validation failed | 422 |
| SA04 | Vendor timeout | 504 |
| VD01 | Product not activated | 400 |
| VD02 | Return period exceeded | 409 |
| VD03 | Invoice/product mismatch | 400 |
| VAL01 | Validation error | 400 |
| VAL02 | Type mismatch | 400 |
| SYS01 | Internal server error | 500 |

## Security Considerations

### PCI DSS Compliance
- Card numbers are encrypted at rest
- Sensitive data has 30-day retention policy
- All transactions are logged immutably
- TLS 1.3 encryption for data in transit

### Data Protection
- Personal data (card numbers) automatically purged after 30 days
- Audit logs retained for 7 years
- All API calls require authentication
- Rate limiting and request validation implemented

## Deployment

### Kubernetes
```yaml
# See k8s/ directory for complete manifests
apiVersion: apps/v1
kind: Deployment
metadata:
  name: uavs
spec:
  replicas: 3
  selector:
    matchLabels:
      app: uavs
  template:
    spec:
      containers:
      - name: uavs
        image: uavs:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          value: postgres-service
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

### Production Configuration
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.yourcompany.com

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

logging:
  level:
    com.company.uavs: INFO
    org.springframework.security: WARN
```

## Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Make changes with tests
4. Run `mvn clean verify`
5. Submit pull request

### Code Quality
- Minimum 80% test coverage
- SonarQube quality gates
- Spotless code formatting
- Security scan with OWASP

## Support

### Getting Help
- **Documentation**: See `/docs` folder
- **Issues**: GitHub Issues
- **API Documentation**: http://localhost:8080/v1/swagger-ui.html

### SLA
- **Availability**: 99.9% monthly uptime
- **Response Time**: P95 ≤ 500ms under 100 TPS
- **Support Hours**: 24/7 for production issues

## License

Copyright (c) 2025 Your Company. All rights reserved.