# CampusFit Learning & Inventory Operations System

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

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend development)

### Run with Docker Compose

```bash
cp .env.example .env
# Edit .env with your values
docker-compose up --build
```

- Frontend: http://localhost:3000
- API: http://localhost:8080
- MySQL: localhost:3306

### Run Frontend Only (Mock Mode)

```bash
cd apps/web
npm install
npm run dev
```

By default the frontend connects to the backend API. To run without a backend, set `VITE_MOCK_MODE=true` in a local `.env` file inside `apps/web/`, which enables local mock data. A banner is displayed to indicate mock mode is active.

### Run Backend Only

```bash
# Backend builds inside Docker (no local Java required)
docker build -t campusfit-api services/api/
docker run -p 8080:8080 --env-file .env campusfit-api
```

### Run Tests

```bash
# Frontend tests
cd apps/web && npm test

# Backend tests (via Docker)
docker build --target test -t campusfit-api-test services/api/
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
- Email/SMS/WeCom channels are configurable but **disabled by default**
- Export files are stored locally with configurable retention

## Security

- Passwords hashed with bcrypt
- Account lockout after 5 failed attempts (15-minute cooldown)
- Session timeout after 30 minutes of inactivity
- Role-based access control (RBAC) at route, function, and object level
- Personal fitness metrics encrypted at rest (AES-256-GCM)
- Sensitive fields masked in logs and exports
- Export files are password-protected
- Account deletion hard-deletes PII while retaining de-identified audit trail
