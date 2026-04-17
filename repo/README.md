# CampusFit Learning & Inventory Operations System

**Project Type: fullstack**

A full-stack system for campus fitness tracking, study/review planning, warehouse inbound operations, and academic master data management. Designed for **offline-first local deployment**.

## Repository Structure

```
/
├── apps/web/          Vue 3 + TypeScript frontend
├── services/api/      Spring Boot REST API
├── contracts/         OpenAPI specification
├── docker-compose.yml Full-stack orchestration
└── .env.example       Environment variable template
```

## Roles

| Role | Description |
|------|-------------|
| **Regular User** | Students/trainees — fitness goals, study plans, check-ins |
| **Operations Staff** | Warehouse — inbound receiving, inspection, putaway |
| **Administrator** | Master data management, bulk import/export, system performance |

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | Admin@123 |
| Operations Staff | operator | Operator@123 |
| Regular User | student | Student@123 |

Note: These are seeded by the database migration on first startup.

## Quick Start

### Prerequisites
- Docker & Docker Compose

### Run with Docker Compose

```bash
cp .env.example .env
# Edit .env with your values
docker-compose up --build
```

- Frontend: http://localhost:3000
- API: http://localhost:8080
- MySQL: localhost:3306

### Verify the System

After `docker-compose up --build`, verify:

```bash
# Backend health check
curl http://localhost:8080/api/auth/sign-in -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'

# Frontend loads
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000
# Expected: 200
```

### Run Frontend Only (Mock Mode)

The frontend can run in mock mode inside Docker without a backend. Set `VITE_MOCK_MODE=true` in your `.env` file and run:

```bash
docker-compose up --build web
```

A banner is displayed to indicate mock mode is active.

### Run Backend Only

```bash
# Backend builds inside Docker (no local Java required)
docker build -t campusfit-api services/api/
docker run -p 8080:8080 --env-file .env campusfit-api
```

### Run Tests

```bash
./run_tests.sh
```

## Environment Configuration

See `.env.example` for all configuration options:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | MySQL host | `mysql` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `campusfit` |
| `APP_ENCRYPTION_KEY` | AES-256 key for PII encryption | — |
| `APP_SESSION_TIMEOUT_MINUTES` | Session inactivity timeout | `30` |
| `APP_LOCKOUT_ATTEMPTS` | Failed login attempts before lockout | `5` |
| `APP_LOCKOUT_DURATION_MINUTES` | Lockout duration | `15` |
| `NOTIFICATION_EMAIL_ENABLED` | Enable email delivery | `false` |
| `NOTIFICATION_SMS_ENABLED` | Enable SMS delivery | `false` |
| `NOTIFICATION_WECOM_ENABLED` | Enable WeCom delivery | `false` |
| `EXPORT_RETENTION_DAYS` | File retention before cleanup | `30` |

## Offline / Local Deployment

This system is designed for offline-first local deployment:

- All data is persisted in a local MySQL instance
- Authentication is local username/password (no external IdP)
- Notification delivery is in-app only by default
- Email/SMS/WeCom channels are **hard-disabled** until adapters are implemented (config flags are ignored)
- Export files are stored locally with configurable retention

## Security

- Passwords hashed with bcrypt
- Account lockout after 5 failed attempts (15-minute cooldown)
- Session timeout after 30 minutes of inactivity
- Role-based access control (RBAC) at route, function, and object level
- Personal fitness metrics encrypted at rest (AES-256-GCM)
- Sensitive fields masked in logs; export files contain raw profile data inside a password-encrypted (AES-256-CBC) bundle to support round-trip import
- Account deletion hard-deletes PII while retaining de-identified audit trail
