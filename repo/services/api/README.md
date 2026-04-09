# CampusFit API

Spring Boot REST API for the CampusFit Learning & Inventory Operations System.

## Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** — session-based auth, RBAC
- **Spring Data JPA** — MySQL persistence
- **Flyway** — database migrations
- **Caffeine** — 5-minute TTL caching
- **Apache POI** — Excel import/export
- **MapStruct** — DTO mapping
- **Lombok** — boilerplate reduction

## Modules

| Module | Package | Description |
|--------|---------|-------------|
| Auth | `com.campusfit.auth` | Sign-up/sign-in, bcrypt, lockout, sessions, RBAC |
| Fitness | `com.campusfit.fitness` | Assessments, goals, milestones, check-ins |
| Study | `com.campusfit.study` | Plans, daily completion, forgetting points, streaks |
| Inbound | `com.campusfit.inbound` | Receiving workflow, inspection, discrepancy, putaway |
| Master Data | `com.campusfit.masterdata` | Terms, schools, majors, classes, courses, import/merge |
| Notification | `com.campusfit.notification` | In-app notifications, delivery channels |
| Export | `com.campusfit.export_` | Password-protected exports, retention, account deletion |
| Reporting | `com.campusfit.reporting` | Dashboard aggregates, slow-query logging |
| Shared | `com.campusfit.shared` | Security, encryption, audit, exception handling, pagination |

## Auth / RBAC

- Passwords hashed with bcrypt
- Lockout after 5 failed attempts for 15 minutes
- Session timeout after 30 minutes of inactivity
- Three roles: `REGULAR_USER`, `OPERATIONS_STAFF`, `ADMIN`
- Authorization enforced at controller level via annotations and at service level via ownership checks

## Database Migrations

Migrations are in `src/main/resources/db/migration/`:

| Migration | Description |
|-----------|-------------|
| V1 | Auth tables (users, roles, permissions, sessions) |
| V2 | Seed roles and permissions |
| V3 | Fitness tables (assessments, goals, milestones, check-ins) |
| V4 | Study tables (plans, completions, forgetting points, streaks) |
| V5 | Dashboard summary table |
| V6 | Inbound tables (receipts, lines, inspections, discrepancies) |
| V7 | Master data tables (terms, schools, majors, classes, courses) |
| V8 | Notification tables |
| V9 | Export and audit tables |
| V10 | Add session last_accessed_at for inactivity timeout |
| V11 | Encrypt fitness metrics at rest (make plaintext columns nullable) |

## Running Tests

```bash
# Via Docker
docker build --target test -t campusfit-api-test services/api/

# With local Maven (if available)
mvn test
```

## Docker

```bash
docker build -t campusfit-api .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/campusfit \
  -e SPRING_DATASOURCE_USERNAME=campusfit \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  campusfit-api
```

## OpenAPI

Contract is at `/contracts/openapi.yaml`. When the API is running, Swagger UI is available at `/swagger-ui.html`.
