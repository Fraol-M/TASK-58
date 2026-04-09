# CampusFit Learning & Inventory Operations — Design Document

## 1. System Overview

CampusFit is an offline-first, locally deployed web application for managing campus fitness programs, academic study planning, warehouse inbound operations, and administrative master data — all backed by a single MySQL database instance with no cloud dependency.

**Primary roles:**

- Regular User (students/trainees)
- Operations Staff (warehouse/receiving personnel)
- Administrator

**Core capabilities:**

- local authentication with session-based RBAC
- fitness assessment tracking with encrypted body metrics
- goal setting with automatic recalculation on missed check-ins
- study plan management tied to a full academic hierarchy
- spaced repetition (SM-2) for forgetting-point review
- inbound receipt processing with state-machine-driven workflow
- discrepancy detection with supervisor review and separation of duty
- master data administration with bulk import/export, merge, and change history
- unified in-app notification center with delivery tracking
- password-encrypted data export with cross-account import
- account hard-deletion with de-identified audit retention
- role-based dashboards with pre-aggregated metrics and caching

The system runs as a Vue 3 SPA consuming REST APIs from a Spring Boot backend, with all data persisted in MySQL. A Docker Compose stack provides single-command local deployment.

---

## 2. Design Goals

- **Offline-first local deployment** — the entire system runs on a single machine via Docker Compose; no cloud accounts, SaaS subscriptions, or internet access required
- **Decoupled frontend/backend** — Vue SPA communicates exclusively through a typed REST adapter interface; a mock adapter enables fully offline frontend development
- **Server-side authority** — all validation, authorization, encryption, and state transitions are enforced in the backend; the frontend is a presentation layer
- **Domain-driven module boundaries** — each functional area (auth, fitness, study, inbound, masterdata, notification, export, reporting) is a self-contained Java package with its own controller, service, repository, entity, and DTO classes
- **Deterministic workflows** — inbound receipt processing follows a strict state machine; fitness goal recalculation follows a codified policy; master data changes are append-only versioned
- **Encryption at rest** — personal fitness metrics are AES-256-GCM encrypted in the database; export files are AES-256-CBC encrypted with a user-supplied password
- **Scalable data access** — pagination is database-backed (not in-memory), dashboards use pre-aggregated tables, and high-volume tables use MySQL partitioning
- **Testable architecture** — backend services are constructor-injected and mockable; frontend stores are composable and adapter-injectable

---

## 3. High-Level Architecture

```
┌──────────────────────────────────────────────────────┐
│                   Vue 3 SPA (Vite)                   │
│  ┌─────────┐  ┌──────────┐  ┌──────────────────────┐│
│  │ Router + │  │  Pinia   │  │  Adapter Interface   ││
│  │  Guards  │  │  Stores  │  │  (HttpAdapter /      ││
│  │         │  │          │  │   MockAdapter)        ││
│  └────┬────┘  └────┬─────┘  └──────────┬───────────┘│
│       │            │                    │            │
│  ┌────┴────────────┴────────────────────┴──────────┐ │
│  │        Pages / Components / Composables         │ │
│  └─────────────────────────────────────────────────┘ │
└──────────────────────────┬───────────────────────────┘
                           │  HTTP (Bearer token)
┌──────────────────────────┴───────────────────────────┐
│              Spring Boot REST API                     │
│  ┌───────────────────────────────────────────────┐   │
│  │  SessionAuthenticationFilter (sliding window) │   │
│  └──────────────────┬────────────────────────────┘   │
│  ┌──────────────────┴────────────────────────────┐   │
│  │           Controllers (@RestController)       │   │
│  └──────────────────┬────────────────────────────┘   │
│  ┌──────────────────┴────────────────────────────┐   │
│  │  Services + Policies (business logic layer)   │   │
│  └──────────────────┬────────────────────────────┘   │
│  ┌──────────────────┴────────────────────────────┐   │
│  │     JPA Repositories (Spring Data)            │   │
│  └──────────────────┬────────────────────────────┘   │
│  ┌──────────────────┴────────────────────────────┐   │
│  │  Encryption Layer (AES-256-GCM converters)    │   │
│  └──────────────────┬────────────────────────────┘   │
└──────────────────────┬───────────────────────────────┘
                       │
              ┌────────┴────────┐
              │    MySQL 8.0    │
              │  (Flyway-managed│
              │   20 migrations)│
              └─────────────────┘
```

### Architecture Principles

**Server-side authority:** All business logic resides in backend services. Controllers are thin HTTP adapters. The frontend never enforces domain invariants — it only provides UX feedback.

**Adapter abstraction:** The frontend defines an `ApiAdapter` interface with 40+ methods covering all domains. At runtime, an `AdapterFactory` instantiates either `HttpAdapter` (real API calls) or `MockAdapter` (local fake data) based on `VITE_MOCK_MODE`. This allows full frontend development without a running backend.

**Module isolation:** Each backend package (auth, fitness, study, inbound, masterdata, notification, export_, reporting, shared) contains its own controller/service/repository/entity/DTO/policy classes. Cross-module dependencies flow through repository interfaces, never through direct entity imports between unrelated domains.

---

## 4. Deployment Architecture

### Docker Compose Stack

| Service | Image | Port | Role |
|---------|-------|------|------|
| `web` | Custom (Vite build → nginx) | 3000 | Serves the Vue SPA |
| `api` | Custom (Spring Boot fat JAR) | 8080 | REST API server |
| `mysql` | mysql:8.0 | 3306 | Persistent storage |

```
docker-compose up
```

MySQL starts with a health check; the API waits for MySQL readiness before booting. Flyway applies all 20 migrations on first start. The frontend connects to the API via `VITE_API_BASE_URL`.

### Environment Configuration

All runtime behavior is controlled via environment variables (see `.env.example`). No configuration files need manual editing for a default local deployment.

---

## 5. Frontend Architecture

### 5.1 Framework & Tooling

| Concern | Technology |
|---------|-----------|
| Framework | Vue 3 (Composition API) |
| Build | Vite |
| State | Pinia stores |
| Routing | Vue Router |
| HTTP | Axios via `http-client.ts` |
| Testing | Vitest + @vue/test-utils |
| Styling | Scoped CSS per component |

### 5.2 Route Structure

| Path | Role Guard |
|------|-----------|
| `/sign-in`, `/sign-up` | Public |
| `/dashboard` | Authenticated |
| `/fitness/*` | `REGULAR_USER` |
| `/study/*` | `REGULAR_USER` |
| `/operations/*` | `OPERATIONS_STAFF` or `ADMIN` |
| `/admin/*` | `ADMIN` |
| `/notifications`, `/profile`, `/exports` | Authenticated |

Route guards read the user object from localStorage and check role membership. The `roleGuard` function accepts both single roles and arrays.

### 5.3 Module Structure

Each feature module follows a consistent layout:

```
modules/{feature}/
  ├── pages/           # Route-level page components
  ├── components/      # Feature-specific UI components
  ├── composables/     # Reusable logic (useXxx hooks)
  ├── store.ts         # Pinia store
  ├── api.ts           # Adapter accessor
  ├── types.ts         # Module-specific TypeScript types
  └── __tests__/       # Vitest specs
```

### 5.4 Adapter Pattern

The `ApiAdapter` interface defines the contract between the frontend and any data source:

```typescript
export interface ApiAdapter {
  signIn(payload: SignInRequest): Promise<ApiResponse<LoginResponse>>
  getGoals(params?: PaginationParams): Promise<ApiResponse<FitnessGoal[]>>
  getReceipts(params?: PaginationParams): Promise<ApiResponse<ReceivingReceipt[]>>
  // ... 40+ methods
}
```

- `HttpAdapter` — makes real Axios calls to the Spring Boot API
- `MockAdapter` — returns deterministic fake data for offline UI development

The factory reads `VITE_MOCK_MODE` at startup and caches the singleton.

### 5.5 Reusable Component Library

| Category | Components |
|----------|-----------|
| Common | `AppCard`, `AppButton`, `AppModal` |
| Forms | `FormField`, `FormInput`, `FormTextarea`, `FormSelect`, `SubmitButton` |
| Data Display | `ProgressBar`, `StatusIndicator`, `StatCard` |
| Feedback | `ErrorState`, `EmptyState`, `LoadingState`, `Toast` |

---

## 6. Backend Architecture

### 6.1 Framework & Tooling

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 3 |
| Language | Java 17 |
| ORM | Spring Data JPA (Hibernate) |
| Migrations | Flyway (17 versioned SQL scripts) |
| Database | MySQL 8.0 |
| Security | Spring Security + custom session filter |
| Cache | Caffeine (1000 entries, 5-min TTL) |
| Build | Maven |
| Testing | JUnit 5 + Mockito + AssertJ |

### 6.2 Package Structure

```
com.campusfit/
  ├── auth/          # Authentication, sessions, users, roles
  ├── fitness/       # Assessments, goals, check-ins, milestones
  ├── study/         # Plans, completions, forgetting points, streaks
  ├── inbound/       # Receipts, lines, inspection, putaway, discrepancies
  ├── masterdata/    # Terms, schools, majors, classes, courses, import/export/merge
  ├── notification/  # Notifications, targets, delivery channels
  ├── export_/       # Account export/import, deletion, file retention
  ├── reporting/     # Dashboard, aggregation, slow-query logging
  └── shared/        # Security, encryption, exceptions, DTOs, config
```

### 6.3 Service Layer Responsibilities

| Service | Key Responsibilities |
|---------|---------------------|
| `AuthService` | Sign-up, sign-in (with lockout), sign-out, session validation, role assignment |
| `LoginAttemptService` | Failed attempt tracking, lockout enforcement/expiry |
| `PasswordService` | bcrypt hashing and verification |
| `AssessmentService` | Fitness assessment create/update, latest retrieval |
| `GoalService` | Goal CRUD, progress calculation, milestone tracking |
| `CheckInService` | Check-in recording, missed-check-in tracking, recalculation trigger |
| `GoalRecalculationPolicy` | Auto-adjusts goals after 2 missed check-ins (extend deadline +20%, reduce target -10%) |
| `StudyPlanService` | Plan CRUD with academic hierarchy referential validation |
| `DailyCompletionService` | Completion recording, streak updates |
| `ForgettingPointService` | SM-2 spaced repetition: create, review, recalculate intervals |
| `StudyExportImportService` | Export/import study data as JSON bundles |
| `InboundReceiptService` | Receipt CRUD, line management |
| `InboundStateMachine` | State transition validation and recording |
| `InboundLineService` | Line creation, receiving, inspection, auto-discrepancy detection |
| `DiscrepancyService` | Discrepancy creation (threshold-based), resolution |
| `PutawayService` | Idempotent task generation, task completion |
| `NotificationService` | Notification creation, paginated retrieval, read receipts, delivery status |
| `ExportService` | Export job lifecycle, encrypted file generation, decryption |
| `AccountImportService` | Import from raw JSON or encrypted file, goal ID remapping |
| `AccountDeletionService` | Hard-delete user row, cascade-delete personal data, preserve audit artifacts |
| `FileRetentionService` | Scheduled cleanup of expired export files |
| `DashboardService` | Role-based dashboard construction (user/ops/admin) |
| `AggregationService` | Nightly metric pre-aggregation into dashboard_summary |
| `SlowQueryLogService` | MySQL performance_schema analysis |
| `MasterDataImportService` | CSV/Excel parsing, row-by-row validation, error collection |
| `DuplicateMergeService` | Merge source into target, remap references, soft-delete source |
| `ChangeHistoryService` | Append-only change log for master data mutations |

### 6.4 Policy Layer

Domain policies encapsulate business rules that span multiple entities:

| Policy | Responsibility |
|--------|---------------|
| `GoalRecalculationPolicy` | Recalculates goal targets and deadlines after missed check-ins |
| `ReferentialIntegrityPolicy` | Blocks master data deletion when referenced by child entities or study plans |
| `EffectiveDatePolicy` | Validates `effectiveFrom` precedes `effectiveTo` |
| `DeliveryChannelPolicy` | Resolves active delivery channels based on configuration flags |

---

## 7. Authentication & Security Design

### 7.1 Authentication Model

- Local username/password authentication (no OAuth, no external IdP)
- Passwords hashed with bcrypt via Spring Security's `BCryptPasswordEncoder`
- Sessions stored in MySQL, identified by UUID tokens
- Token passed as `Bearer` in the `Authorization` header

### 7.2 Session Management

- Sliding window timeout: each authenticated request refreshes `lastAccessedAt`
- Default timeout: 30 minutes of inactivity
- `SessionAuthenticationFilter` validates token, checks timeout, loads roles, and sets Spring Security context on every request
- Sign-out deletes the session row

### 7.3 Account Lockout

| Parameter | Default |
|-----------|---------|
| Max failed attempts | 5 |
| Lockout duration | 15 minutes |
| Auto-reset | On lockout expiry, status returns to ACTIVE |

### 7.4 Route-Level Authorization

| Route Pattern | Requirement |
|---------------|-------------|
| `/api/auth/**` | Public (no auth required) |
| `/api/admin/**` | `ADMIN` role |
| `/api/inbound/**` | `OPERATIONS_STAFF` or `ADMIN` |
| `/api/**` | Authenticated |

### 7.5 Function-Level Authorization

Method-level `@PreAuthorize` annotations enforce:

- Notification creation: `ADMIN` only
- Notification delivery status: `ADMIN` only
- Role assignment: `ADMIN` only
- Performance metrics: `ADMIN` only
- Supervisor review: `ADMIN` only + separation of duty (reviewer ≠ receipt creator)

### 7.6 Object-Level Authorization

Service-layer ownership checks verify that users can only access their own:

- Study plans
- Fitness goals and check-ins
- Notifications
- Export jobs

### 7.7 RBAC Permission Matrix

| Domain | REGULAR_USER | OPERATIONS_STAFF | ADMIN |
|--------|:---:|:---:|:---:|
| Fitness (read/write) | Y | Y | Y |
| Study (read/write) | Y | Y | Y |
| Inbound (read/write) | - | Y | Y |
| Master Data (read/write) | - | Y | Y |
| Notifications (read) | Y | Y | Y |
| Notifications (write) | - | Y | Y |
| Export (read/write) | - | Y | Y |
| Reporting (read/write) | - | Y | Y |
| Supervisor review | - | - | Y |
| User management | - | - | Y |

---

## 8. Encryption Design

### 8.1 At-Rest Encryption (Database Fields)

| Property | Value |
|----------|-------|
| Algorithm | AES-256-GCM |
| IV | 96-bit (12 bytes), random per field |
| Auth tag | 128-bit |
| Key source | `APP_ENCRYPTION_KEY` environment variable (hex-encoded 256-bit key) |
| Storage format | Base64(IV + ciphertext) |

Implemented via JPA `@Converter` annotations (`AesEncryptor`, `BigDecimalAesEncryptor`) so encryption is transparent to service code.

**Encrypted fields:**

| Entity | Fields |
|--------|--------|
| Goal | `targetValue`, `startValue`, `currentValue`, `metricsEncrypted` |
| CheckIn | `value`, `valueEncrypted` |
| Assessment | Body measurement metrics |

### 8.2 Export File Encryption

| Property | Value |
|----------|-------|
| Algorithm | AES-256-CBC with PKCS5Padding |
| Key derivation | PBKDF2WithHmacSHA256, 65536 iterations, 256-bit output |
| Salt | 16 bytes random (prepended to file) |
| IV | 16 bytes random (prepended after salt) |
| File format | `[16B salt][16B IV][ciphertext]` → `.enc` extension |

Export files contain raw profile data (username, email, phone) — this is safe because the file itself is password-encrypted. This enables faithful round-trip import on another account.

### 8.3 Field Masking

`FieldMasker` redacts sensitive values in logs, API responses, **and export file payloads**:

- Generic: first 2 + last 2 characters visible (e.g., `jo***oe`)
- Email: first character + domain (e.g., `j***@example.com`)
- Numeric: replaced with `***`

Masking is applied even inside password-encrypted export files. Raw PII never appears in log output or export bundles.

---

## 9. Inbound Receiving Workflow Design

### 9.1 State Machine

```
DRAFT ──→ RECEIVING ──→ INSPECTION ──→ PUTAWAY ──→ COMPLETED ──→ POSTED ──→ UNPOSTED
  │           │              │            │
  └───────────┴──────────────┴────────────┴──→ REJECTED
```

### 9.2 Transition Guards

| Transition | Precondition | Required Role |
|------------|-------------|---------------|
| DRAFT → RECEIVING | Receipt must have ≥ 1 line | `OPERATIONS_STAFF` or `ADMIN` |
| RECEIVING → INSPECTION | All lines must have `receivedQty > 0` | `OPERATIONS_STAFF` or `ADMIN` |
| INSPECTION → PUTAWAY | All lines inspected (not PENDING). All supervisor-required discrepancies resolved. | `OPERATIONS_STAFF` or `ADMIN` |
| PUTAWAY → COMPLETED | All putaway tasks must be COMPLETED | `OPERATIONS_STAFF` or `ADMIN` |
| COMPLETED → POSTED | None. Records `postedBy` + `postedAt` on the receipt. | `OPERATIONS_STAFF` or `ADMIN` |
| POSTED → UNPOSTED | None. Records `unpostedBy` + `unpostedAt` on the receipt. | `ADMIN` only |
| Any → REJECTED | Always allowed | `OPERATIONS_STAFF` or `ADMIN` |

### 9.3 Discrepancy Detection

Triggered automatically when `receivedQty` is recorded:

| Condition | Action |
|-----------|--------|
| Variance > 2% **or** > 5 units | Create `Discrepancy` record |
| Either threshold exceeded | Flag `supervisorRequired = true` |
| Supervisor required | Set `receipt.supervisorApprovalRequired = true` |

### 9.4 Supervisor Review

- Restricted to `ADMIN` role via `@PreAuthorize`
- Separation of duty: the reviewer must not be the receipt creator
- Must provide a reason code from: `DAMAGED`, `SHORT_SHIP`, `OVER_SHIP`, `WRONG_ITEM`, `QUALITY_FAIL`, `OTHER`

### 9.5 Putaway

- Tasks are generated idempotently from inspected (PASS) lines
- Each task suggests a location (`ZONE-A-{itemCode}`)
- Completion records `actualLocation`, `completedBy`, and timestamp

### 9.6 Inventory Finalization (Post / Unpost)

After a receipt reaches `COMPLETED`, it can be posted to inventory:

- `POST /api/inbound/receipts/{id}/post` — transitions `COMPLETED → POSTED`. Records `postedBy` (user ID) and `postedAt` (timestamp). Requires `OPERATIONS_STAFF` or `ADMIN`.
- `POST /api/inbound/receipts/{id}/unpost` — transitions `POSTED → UNPOSTED`. Records `unpostedBy` and `unpostedAt`. Restricted to `ADMIN` only.

These transitions are enforced by `InboundStateMachine` and stored in `posted_by`, `posted_at`, `unposted_by`, `unposted_at` columns added by migration V20.

### 9.7 State History

Every transition is logged with `fromState`, `toState`, `changedBy`, `reason`, and timestamp.

---

## 10. Fitness Goal & Check-In Design

### 10.1 Goal Lifecycle

```
ACTIVE ──→ ACHIEVED     (when currentValue meets targetValue)
ACTIVE ──→ RECALCULATED (when 2 consecutive check-ins missed)
ACTIVE ──→ ABANDONED    (manual)
```

### 10.2 Recalculation Policy

Triggered when `missedCheckIns >= 2`:

| Action | Detail |
|--------|--------|
| Extend deadline | +20% of remaining duration (min 1 day) |
| Reduce target | Move 10% closer to current value |
| Reset counter | `missedCheckIns = 0` |
| Set status | `RECALCULATED` |
| Audit | Creates `GoalAdjustmentAudit` record with before/after values and explanation |

### 10.3 Milestones

Goals have phase milestones with sequence numbers. When a check-in causes `currentValue` to exceed a milestone's `targetValue`, the milestone is marked achieved.

### 10.4 Encrypted Metrics

Goal values (`targetValue`, `startValue`, `currentValue`) and check-in values are encrypted at rest using AES-256-GCM JPA converters. Decryption is transparent to business logic.

---

## 11. Study & Spaced Repetition Design

### 11.1 Academic Hierarchy

```
TERM
SCHOOL → MAJOR → CLASS → COURSE (scoped to TERM + CLASS)
```

Study plans reference the hierarchy via optional `termId`, `schoolId`, `majorId`, `classId`, `courseId`. The service layer validates each referenced ID exists before persisting.

### 11.2 Daily Completions

Users record daily study task completions against a plan. Completions update streak counters (`currentStreak`, `longestStreak`, `lastActiveDate`).

### 11.3 Forgetting Points (SM-2)

When a user tags a weak topic, the system initializes SM-2 parameters:

| Field | Initial Value |
|-------|--------------|
| `easeFactor` | 2.50 |
| `intervalDays` | 1 |
| `repetitions` | 0 |

On review, the user rates quality (0–5). The SM-2 algorithm recalculates `easeFactor`, `intervalDays`, `repetitions`, and `nextReviewDate`.

### 11.4 Export/Import

Study data (plans, completions, forgetting points) can be exported as JSON and imported into another account. Goal IDs are remapped during import to avoid conflicts.

---

## 12. Master Data Design

### 12.1 Entity Types

`TERM` | `SCHOOL` | `MAJOR` | `CLASS` | `COURSE`

### 12.2 Effective Dating

All master data records have `effectiveFrom` (required) and `effectiveTo` (optional). `EffectiveDatePolicy` enforces that `from < to`.

### 12.3 Referential Integrity

Deletion is blocked when entities are referenced:

| Entity | Blocked By |
|--------|-----------|
| School | Majors |
| Major | Classes |
| Class | Courses |
| Term | Courses, Study Plans |
| Course | Study Plans |

Deletion is soft (sets `active = false`), so historical references remain valid.

### 12.4 Import/Export

- **Import**: CSV (via OpenCSV) or Excel (via Apache POI). Row-by-row validation with error collection per row. Supports ISO and US date formats.
- **Export**: CSV or XLSX download with columns: Code, Name, Effective From, Effective To, Active.

### 12.5 Merge

- Duplicate candidates identified by Levenshtein distance (≥ 70% name similarity)
- Merge operation: source record is soft-deleted, all FK references repointed to target
- Full change history preserved

### 12.6 Change History

Every create, update, and merge is logged as an immutable `ChangeHistory` record with entity type, entity ID, field, old value, new value, changed-by user, and timestamp.

---

## 13. Notification Design

### 13.1 Delivery Channels

| Channel | Default State | Notes |
|---------|:------------:|-------|
| `IN_APP` | Enabled | Always active; the only channel in offline mode |
| `EMAIL` | Disabled | Configurable via `NOTIFICATION_EMAIL_ENABLED` |
| `SMS` | Disabled | Configurable via `NOTIFICATION_SMS_ENABLED` |
| `WECOM` | Disabled | Configurable via `NOTIFICATION_WECOM_ENABLED` |

### 13.2 Notification Types

`ANNOUNCEMENT` | `REMINDER` | `FOLLOW_UP`

### 13.3 Delivery Model

- Admin creates a notification with target user IDs
- A `NotificationTarget` record is created per recipient
- Users fetch their paginated notification list
- Read receipts recorded via `POST /notifications/{id}/read`
- Delivery status visible to admin per notification

---

## 14. Export & Account Deletion Design

### 14.1 Export Flow

1. User requests export with password → job created (`PENDING`)
2. System generates data bundle → encrypts with PBKDF2-derived key → writes `.enc` file
3. Job status: `COMPLETED`, file available for download
4. File expires after retention period (default 30 days)
5. `FileRetentionService` deletes expired files daily

### 14.2 Import Flow

1. User uploads `.enc` file + password
2. System decrypts file, parses JSON bundle
3. Imports: profile metadata, study data, fitness assessments, goals (with ID remapping), check-ins
4. Returns summary (study: N, assessments: N, goals: N, check-ins: N)

### 14.3 Account Deletion

1. User confirms with password
2. Backend verifies password
3. Hard-deletes: check-ins, goals, assessments, study plans, sessions
4. Hard-deletes user row (FK references to inbound receipts, notifications, deletion requests are SET NULL via migration V17)
5. `DeletionRequest` record preserved as de-identified audit artifact

---

## 15. Dashboard & Reporting Design

### 15.1 Role-Based Dashboards

| Role | Summary Keys | Charts |
|------|-------------|--------|
| Regular User | fitnessGoals, studyStreak, recentCheckIns, pendingNotifications | goalProgress |
| Operations Staff | activeReceipts, pendingDiscrepancies, putawayQueueSize, operationsProcessed | receiptsByStatus |
| Admin | totalUsers, activePlans, importJobs, totalMetrics | performance |

### 15.2 Pre-Aggregation

`AggregationService` runs nightly at 2:00 AM, computing:

- total_goals, active_goals, total_study_plans

Results stored in `t_dashboard_summary` and served via the cached dashboard endpoint (5-minute TTL).

### 15.3 Performance Monitoring

`SlowQueryLogService` queries MySQL's `performance_schema` for the top 25 slowest queries. Exposed via `GET /api/admin/performance` (admin-only).

---

## 16. Data Persistence Design

### 16.1 Database Tables (20 Flyway Migrations)

| Migration | Tables Created |
|-----------|---------------|
| V1 | `t_user`, `t_role`, `t_permission`, `t_role_permission`, `t_user_role`, `t_session` |
| V2 | Seed: 3 roles, 21 permissions, role-permission mappings |
| V3 | `t_assessment`, `t_goal`, `t_milestone`, `t_check_in`, `t_goal_adjustment_audit` (check_in partitioned by month) |
| V4 | `t_study_plan`, `t_study_plan_item`, `t_daily_completion`, `t_forgetting_point`, `t_streak` |
| V5 | `t_dashboard_summary` |
| V6 | `t_inbound_receipt`, `t_inbound_line`, `t_inbound_state_history`, `t_discrepancy`, `t_putaway_task` |
| V7 | `t_term`, `t_school`, `t_major`, `t_class`, `t_course`, `t_change_history`, `t_merge_operation`, `t_import_job`, `t_import_error` |
| V8 | `t_notification`, `t_notification_target` |
| V9 | `t_export_job`, `t_deletion_request` |
| V10 | Add `last_accessed_at` to `t_session` |
| V11–V14 | Encrypt fitness metrics columns (AES converters) |
| V15 | Add `school_id`, `major_id`, `class_id` to `t_study_plan` |
| V16 | Partition `t_daily_completion` and `t_forgetting_point` by month |
| V17 | Change non-cascading FKs to `ON DELETE SET NULL` for account deletion |
| V18 | Add term-based composite indexes (`idx_study_plan_term_user`, `idx_inbound_receipt_expected_date`, etc.) |
| V19 | Apply `RANGE` partitioning on `t_study_plan` by `term_id`; drop affected FKs (enforced at service layer) |
| V20 | Add `posted_by`, `posted_at`, `unposted_by`, `unposted_at` columns to `t_inbound_receipt` |

### 16.2 Partitioning Strategy

| Table | Partition Key | Strategy |
|-------|--------------|----------|
| `t_check_in` | `created_at` | Monthly range partitions |
| `t_daily_completion` | `completed_date` | Monthly range partitions |
| `t_forgetting_point` | `next_review_date` | Monthly range partitions |
| `t_study_plan` | `term_id` | Range partitions by term ID bucket (V19). FKs enforced at service layer. |

### 16.3 Indexing

Key indexes include:

- `idx_study_plan_user_id`, `idx_study_plan_user_status`
- `idx_daily_comp_plan_id`, `idx_daily_comp_date`
- `idx_forget_plan_id`, `idx_forget_review_date`
- `idx_streak_user_id`
- Unique constraints on `t_user.username`, `t_streak(user_id, plan_id)`

### 16.4 Caching

Caffeine cache with `maximumSize=1000, expireAfterWrite=300s`:

- Dashboard responses cached per user principal
- Master data lists cached per entity type

---

## 17. Error Handling Strategy

### 17.1 Backend Exception Hierarchy

| Exception | HTTP Status | Usage |
|-----------|:-----------:|-------|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `BusinessException` | 400 | Domain rule violation |
| `AccessDeniedException` | 403 | Insufficient role/permission |
| `MethodArgumentNotValidException` | 422 | Bean validation failure (field errors returned) |
| `Exception` (generic) | 500 | Unexpected errors (logged, generic message to client) |

### 17.2 Error Response Format

```json
{
  "timestamp": "2026-04-08T12:00:00",
  "status": 422,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/fitness/goals",
  "fieldErrors": {
    "targetValue": "Target value must be positive",
    "targetDate": "Target date must be in the future"
  }
}
```

### 17.3 Frontend Error Handling

- API errors normalized via `normalizeError()` utility
- Stores expose `error` ref for inline display
- Toast notifications for transient success/error feedback
- `ErrorState` component for full-page error states

---

## 18. Scheduled Tasks

| Task | Schedule | Service |
|------|----------|---------|
| Dashboard aggregation | `0 0 2 * * *` (daily 2 AM) | `AggregationService` |
| Export file cleanup | Every 24 hours | `FileRetentionService` |

---

## 19. Testing Strategy

### 19.1 Backend Unit Tests (JUnit 5 + Mockito)

| Area | Coverage |
|------|----------|
| Auth | Sign-up, sign-in, lockout, session validation |
| Fitness | Goal recalculation policy, check-in service |
| Inbound | State machine transitions (including POSTED/UNPOSTED), discrepancy thresholds |
| Master Data | Referential integrity, merge operations |
| Notification | Service delivery, read receipts |
| Export | Export generation, profile masking, import mapping, deletion semantics |

### 19.2 Backend Controller Tests (@WebMvcTest + Mockito)

| Area | Coverage |
|------|----------|
| Auth | Sign-up/sign-in flows, role assignment |
| Inbound | Receipt CRUD, transition, post/unpost endpoints, supervisor-review RBAC and separation-of-duty |
| Reporting | Dashboard payload, admin performance endpoint authorization |

### 19.3 Backend Integration Tests (@SpringBootTest + H2)

Full-stack tests exercising the real security filter chain, DB persistence, and state transitions without mocking:

| Test Class | Coverage |
|------------|----------|
| `AuthIntegrationTest` | Sign-up → sign-in → GET /me, session invalidation, lockout chain, 401/403 guards |
| `InboundWorkflowIntegrationTest` | DRAFT→RECEIVING→INSPECTION→PUTAWAY→COMPLETED full workflow; RBAC guard (regular user → 403); invalid transition → 400 |
| `ExportLifecycleIntegrationTest` | Export without password → 400; export with password → 201/COMPLETED/downloadReady; list exports; unauthenticated → 401 |

### 19.4 Frontend Tests (Vitest)

| Area | Coverage |
|------|----------|
| Dashboard | Role-specific rendering |
| Operations | Receipt detail, discrepancies, receiving list |
| Study | Plans page, review page, history page |
| Exports | Export form, import flow, deletion confirmation |
| Fitness | Check-ins, goals |
| Notifications | Notification page |

---

## 20. Implementation Constraints

- All data stored in a single MySQL instance — no distributed databases
- No external API calls — email/SMS/WeCom channels are configurable but disabled by default
- Authentication is session-based (not JWT) — sessions stored in MySQL
- Frontend mock mode enables development without a running backend
- All passwords bcrypt-hashed — no plaintext storage
- AES encryption key must be provided at deploy time via environment variable
- Flyway manages all schema changes — no manual DDL

---

## 21. Future Integration Readiness

Although the system is designed for standalone local deployment, the architecture supports future extension:

- **Backend API integration**: The `ApiAdapter` interface allows swapping `HttpAdapter` for adapters pointing at different backends
- **External notification channels**: Email/SMS/WeCom infrastructure is plumbed but disabled; enabling requires only configuration changes and channel-specific delivery implementations
- **Horizontal scaling**: Session-based auth can be migrated to token-based (JWT) or Redis-backed sessions
- **Cloud deployment**: The Docker Compose stack can be decomposed into separate services for container orchestration (Kubernetes, ECS)
- **Reporting**: The pre-aggregation pattern can be extended with additional metrics without changing the dashboard API contract
