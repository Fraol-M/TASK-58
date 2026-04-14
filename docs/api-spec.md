# CampusFit — API Specification

Offline-first Vue 3 SPA with Spring Boot REST backend, MySQL persistence, AES-256-GCM encryption at rest, bcrypt password hashing, and role-based access control.

## Table of Contents

- [Roles & Permissions](#roles--permissions)
- [Academic Hierarchy](#academic-hierarchy)
- [Enumerations](#enumerations)
- [Routes (Frontend)](#routes-frontend)
- [API Endpoints](#api-endpoints)
  - [AuthController](#1-authcontroller)
  - [AssessmentController](#2-assessmentcontroller)
  - [GoalController](#3-goalcontroller)
  - [CheckInController](#4-checkincontroller)
  - [StudyPlanController](#5-studyplancontroller)
  - [DailyCompletionController](#6-dailycompletioncontroller)
  - [ForgettingPointController](#7-forgettingpointcontroller)
  - [InboundReceiptController](#8-inboundreceiptcontroller)
  - [NotificationController](#9-notificationcontroller)
  - [ExportController](#10-exportcontroller)
  - [ReportingController](#11-reportingcontroller)
  - [Master Data CRUD Controllers](#12-master-data-crud-controllers)
  - [MasterDataImportController](#13-masterdataimportcontroller)
  - [MasterDataExportController](#14-masterdataexportcontroller)
  - [MasterDataMergeController](#15-masterdatamergecontroller)
  - [ChangeHistoryController](#16-changehistorycontroller)
- [Domain Policies](#domain-policies)
  - [Goal Recalculation](#goal-recalculation-policy)
  - [Inbound State Machine](#inbound-state-machine)
  - [Discrepancy Detection](#discrepancy-detection)
  - [Referential Integrity](#referential-integrity)
  - [Delivery Channel Policy](#delivery-channel-policy)
- [Encryption Model](#encryption-model)
- [Scheduled Tasks](#scheduled-tasks)
- [Validation Rules](#validation-rules)
- [Configuration Reference](#configuration-reference)

---

## Roles & Permissions

| Role | Description |
|------|-------------|
| `REGULAR_USER` | Standard user with access to fitness and study modules. |
| `OPERATIONS_STAFF` | Staff member with access to inbound receiving, master data, export, and reporting. Inherits all `REGULAR_USER` permissions. |
| `ADMIN` | Full system administrator with all permissions. Supervisor review authority. |

### Permission Matrix

| Permission | REGULAR_USER | OPERATIONS_STAFF | ADMIN |
|------------|:---:|:---:|:---:|
| `fitness:read` | Y | Y | Y |
| `fitness:write` | Y | Y | Y |
| `study:read` | Y | Y | Y |
| `study:write` | Y | Y | Y |
| `inbound:read` | - | Y | Y |
| `inbound:write` | - | Y | Y |
| `masterdata:read` | - | Y | Y |
| `masterdata:write` | - | Y | Y |
| `notification:read` | Y | Y | Y |
| `notification:write` | - | Y | Y |
| `export:read` | - | Y | Y |
| `export:write` | - | Y | Y |
| `reporting:read` | - | Y | Y |
| `reporting:write` | - | Y | Y |

Role hierarchy: `ADMIN` implicitly satisfies all role requirements.

---

## Academic Hierarchy

```
TERM
SCHOOL
  └─ MAJOR
       └─ CLASS
            └─ COURSE (scoped to TERM + CLASS)
```

| Parent Type | Allowed Child Type |
|-------------|-------------------|
| School | Major |
| Major | Class |
| Class | Course |

Study plans reference the hierarchy via optional `termId`, `schoolId`, `majorId`, `classId`, and `courseId` fields. Server-side referential validation confirms each referenced entity exists before persisting.

---

## Enumerations

### User Statuses
`ACTIVE` | `LOCKED` | `DISABLED` | `DELETED`

### Goal Types
`WEIGHT_LOSS` | `WEIGHT_GAIN` | `FLEXIBILITY` | `ENDURANCE` | `STRENGTH`

### Goal Statuses
`ACTIVE` | `ACHIEVED` | `RECALCULATED` | `ABANDONED`

### Study Plan Statuses
`ACTIVE` | `COMPLETED` | `ARCHIVED`

### Receipt Types
`PURCHASE` | `TRANSFER` | `RETURN`

### Receipt Statuses (State Machine)
`DRAFT` | `RECEIVING` | `INSPECTION` | `PUTAWAY` | `COMPLETED` | `REJECTED` | `POSTED` | `UNPOSTED`

Transitions:
```
DRAFT       -> RECEIVING, REJECTED
RECEIVING   -> INSPECTION, REJECTED
INSPECTION  -> PUTAWAY, REJECTED
PUTAWAY     -> COMPLETED, REJECTED
COMPLETED   -> POSTED
POSTED      -> UNPOSTED
REJECTED    -> (terminal)
UNPOSTED    -> (terminal)
```

### Inspection Results
`PENDING` | `PASS` | `FAIL`

### Putaway Task Statuses
`PENDING` | `COMPLETED`

### Discrepancy Types
`QUANTITY` | `QUALITY` | `WRONG_ITEM`

### Discrepancy Reason Codes
`DAMAGED` | `SHORT_SHIP` | `OVER_SHIP` | `WRONG_ITEM` | `QUALITY_FAIL` | `OTHER`

### Notification Types
`ANNOUNCEMENT` | `REMINDER` | `FOLLOW_UP`

### Export Types
`ACCOUNT_DATA` | `STUDY_DATA` | `FITNESS_DATA`

### Export Job Statuses
`PENDING` | `PROCESSING` | `COMPLETED` | `FAILED`

### Import Job Statuses
`PENDING` | `PROCESSING` | `COMPLETED` | `FAILED`

### Master Data Entity Types
`TERM` | `SCHOOL` | `MAJOR` | `CLASS` | `COURSE`

---

## Routes (Frontend)

| Path | Access |
|------|--------|
| `/sign-in` | Public |
| `/sign-up` | Public |
| `/dashboard` | Authenticated |
| `/notifications` | Authenticated |
| `/profile` | Authenticated |
| `/exports` | Authenticated |
| `/fitness/assessment` | `REGULAR_USER` |
| `/fitness/goals` | `REGULAR_USER` |
| `/fitness/check-ins` | `REGULAR_USER` |
| `/study/plans` | `REGULAR_USER` |
| `/study/review` | `REGULAR_USER` |
| `/study/history` | `REGULAR_USER` |
| `/operations/receiving` | `OPERATIONS_STAFF`, `ADMIN` |
| `/operations/receiving/:receiptId` | `OPERATIONS_STAFF`, `ADMIN` |
| `/operations/discrepancies` | `OPERATIONS_STAFF`, `ADMIN` |
| `/operations/putaway` | `OPERATIONS_STAFF`, `ADMIN` |
| `/admin/master-data` | `ADMIN` |
| `/admin/master-data/import` | `ADMIN` |
| `/admin/master-data/merge` | `ADMIN` |
| `/admin/master-data/history` | `ADMIN` |
| `/admin/performance` | `ADMIN` |

---

## API Endpoints

All endpoints are prefixed with `/api`. Unless noted otherwise, requests and responses use `application/json`.

### Response Envelope

Every JSON response is wrapped in:

```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

Paginated responses wrap `data` in a Spring `Page` object:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 4,
    "size": 25,
    "number": 0
  }
}
```

### Authentication

All endpoints except `/api/auth/sign-up`, `/api/auth/sign-in`, and `/api/auth/sign-out` require a `Bearer` token in the `Authorization` header. Tokens are UUID-based session identifiers obtained from sign-in.

---

### 1. AuthController

Authentication, session management, and user administration.

#### `POST /api/auth/sign-up`

| Field | Value |
|-------|-------|
| Auth | None (public) |
| Body | `{ username: string, password: string, email?: string, phone?: string }` |
| Returns | `201` — `UserDto` |
| Validation | `username`: required. `password`: required, min 8 characters. |
| Errors | Duplicate username. |

#### `POST /api/auth/sign-in`

| Field | Value |
|-------|-------|
| Auth | None (public) |
| Body | `{ username: string, password: string }` |
| Returns | `200` — `{ token: string, user: UserDto, expiresAt: datetime }` |
| Errors | Invalid credentials (generic for wrong password, unknown user, locked, deactivated). |
| Lockout | 5 failed attempts triggers 15-minute lock. |

#### `POST /api/auth/sign-out`

| Field | Value |
|-------|-------|
| Auth | None (public, idempotent) |
| Returns | `200` — void |
| Behavior | Idempotent. If a valid `Bearer` token is present it is invalidated; if absent or already invalid the call still returns `200`. This prevents client-side errors on double-logout or expired-session scenarios. |

#### `GET /api/me`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Returns | `200` — `UserDto { id, username, email, roles[], status }` |

#### `POST /api/admin/users/{userId}/roles`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Params | `roleCode`: query string — one of `REGULAR_USER`, `OPERATIONS_STAFF`, `ADMIN` |
| Returns | `200` — `UserDto` |

---

### 2. AssessmentController

Fitness assessment (body measurements) management.

#### `GET /api/fitness/assessment`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Returns | `200` — `AssessmentResponse` |
| Behavior | Returns the latest assessment for the authenticated user. |

#### `PUT /api/fitness/assessment`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | `AssessmentRequest` (see below) |
| Returns | `200` — `AssessmentResponse` |
| Behavior | Creates a new assessment or updates the existing one. |

**AssessmentRequest:**

| Field | Type | Required | Validation |
|-------|------|:--------:|------------|
| `heightFeet` | integer | Y | 1–8 |
| `heightInches` | integer | Y | 0–11 |
| `weightLbs` | double | Y | > 0 |
| `bodyFatPercent` | double | N | |
| `waist` | double | N | |
| `chest` | double | N | |
| `arm` | double | N | |
| `notes` | string | N | |

---

### 3. GoalController

Fitness goal lifecycle management. Goal values are AES-256-GCM encrypted at rest.

#### `POST /api/fitness/goals`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | `GoalRequest` (see below) |
| Returns | `201` — `GoalResponse` |
| Behavior | Creates a goal linked to the user's latest assessment. Sets `startValue` from current assessment metrics. |

**GoalRequest:**

| Field | Type | Required | Validation |
|-------|------|:--------:|------------|
| `goalType` | enum | Y | One of `GoalType` values |
| `description` | string | N | max 500 chars |
| `targetValue` | decimal | Y | > 0 |
| `unit` | string | Y | |
| `startDate` | date | Y | |
| `targetDate` | date | Y | Must be in the future |

#### `GET /api/fitness/goals`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Params | `page` (default 0), `size` (default 25) |
| Returns | `200` — `Page<GoalResponse>` |

#### `GET /api/fitness/goals/{id}`

| Field | Value |
|-------|-------|
| Auth | Authenticated (owner only) |
| Returns | `200` — `GoalResponse` |
| Errors | `403` if not owner, `404` if not found |

**GoalResponse includes:** `id`, `userId`, `assessmentId`, `goalType`, `description`, `targetValue`, `startValue`, `currentValue`, `unit`, `startDate`, `targetDate`, `status`, `missedCheckIns`, `progressPercentage`, `milestones[]`, `createdAt`, `updatedAt`.

---

### 4. CheckInController

Weekly progress check-ins against fitness goals. Triggers recalculation policy when 2 consecutive check-ins are missed.

#### `POST /api/fitness/goals/{goalId}/check-ins`

| Field | Value |
|-------|-------|
| Auth | Authenticated (goal owner) |
| Body | `{ value: decimal (required, > 0), notes?: string }` |
| Returns | `201` — `CheckInResponse` |
| Side Effects | Updates goal `currentValue`. Resets `missedCheckIns` counter. May trigger milestone achievement. |

#### `GET /api/fitness/goals/{goalId}/check-ins`

| Field | Value |
|-------|-------|
| Auth | Authenticated (goal owner) |
| Returns | `200` — `CheckInResponse[]` |

---

### 5. StudyPlanController

Study plan management with full academic hierarchy linkage.

#### `POST /api/study/plans`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | `StudyPlanRequest` (see below) |
| Returns | `201` — `StudyPlanResponse` |
| Validation | Referential check: each non-null hierarchy ID must reference an existing master data entity. |

**StudyPlanRequest:**

| Field | Type | Required | Notes |
|-------|------|:--------:|-------|
| `title` | string | Y | Non-blank |
| `description` | string | N | |
| `termId` | long | N | Must reference existing term |
| `schoolId` | long | N | Must reference existing school |
| `majorId` | long | N | Must reference existing major |
| `classId` | long | N | Must reference existing class |
| `courseId` | long | N | Must reference existing course |

#### `GET /api/study/plans`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Params | `page` (default 0), `size` (default 25) |
| Returns | `200` — `Page<StudyPlanResponse>` |
| Behavior | Database-level pagination. Returns only the authenticated user's plans. |

#### `GET /api/study/plans/{id}`

| Field | Value |
|-------|-------|
| Auth | Authenticated (owner only) |
| Returns | `200` — `StudyPlanResponse` |

---

### 6. DailyCompletionController

Track daily study task completions within a plan.

#### `POST /api/study/plans/{planId}/completions`

| Field | Value |
|-------|-------|
| Auth | Authenticated (plan owner) |
| Body | `{ completedDate: date, completed: boolean, itemId?: long, notes?: string }` |
| Returns | `201` — `DailyCompletionResponse` |
| Side Effects | Updates streak counters. |

#### `GET /api/study/plans/{planId}/completions`

| Field | Value |
|-------|-------|
| Auth | Authenticated (plan owner) |
| Returns | `200` — `DailyCompletionResponse[]` |

---

### 7. ForgettingPointController

Spaced repetition system for tagging and reviewing weak knowledge areas.

#### `POST /api/study/plans/{planId}/forgetting-points`

| Field | Value |
|-------|-------|
| Auth | Authenticated (plan owner) |
| Body | `{ topic: string (required, non-blank), description?: string }` |
| Returns | `201` — `ForgettingPointResponse` |
| Behavior | Initializes SM-2 algorithm fields: `easeFactor=2.5`, `intervalDays=1`, `repetitions=0`. |

#### `GET /api/study/plans/{planId}/forgetting-points`

| Field | Value |
|-------|-------|
| Auth | Authenticated (plan owner) |
| Returns | `200` — `ForgettingPointResponse[]` |

#### `POST /api/study/forgetting-points/{id}/review`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | `{ quality: integer }` — 0 to 5 (SM-2 quality rating) |
| Returns | `200` — `ForgettingPointResponse` |
| Behavior | Recalculates `easeFactor`, `intervalDays`, `repetitions`, and `nextReviewDate` per SM-2 algorithm. |

---

### 8. InboundReceiptController

Warehouse inbound receiving workflow. State-machine-driven with discrepancy tracking and putaway management.

#### `POST /api/inbound/receipts`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Body | `{ receiptType: enum, referenceNumber?: string, supplierName?: string, expectedDate?: date }` |
| Returns | `201` — `InboundReceiptResponse` (status: `DRAFT`) |

#### `GET /api/inbound/receipts`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Params | `status` (optional filter), `page` (default 0), `size` (default 25) |
| Returns | `200` — `Page<InboundReceiptResponse>` |
| Behavior | Database-level pagination with optional status filter. |

#### `GET /api/inbound/receipts/{id}`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Returns | `200` — `InboundReceiptResponse` (includes `lines[]`) |

#### `POST /api/inbound/receipts/{id}/lines`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Body | `{ itemCode: string, itemName: string, expectedQty: decimal (>0), unitCost?: decimal }` |
| Returns | `201` — `InboundLineResponse` |

#### `POST /api/inbound/receipts/{id}/receive`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Body | `{ lineId: long, receivedQty: decimal }` |
| Returns | `200` — `InboundLineResponse` |
| Side Effects | Auto-detects quantity discrepancies. Creates `Discrepancy` record if variance > 2% or > 5 units. |

#### `POST /api/inbound/receipts/{id}/transition`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Body | `{ targetState: enum, reason?: string }` |
| Returns | `200` — void |
| Validation | See [Inbound State Machine](#inbound-state-machine) for transition guards. |

#### `POST /api/inbound/receipts/{id}/inspection`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Body | `{ lineId: long, inspectedQty: decimal, result: enum (PASS/FAIL), notes?: string }` |
| Returns | `200` — `InboundLineResponse` |

#### `GET /api/inbound/receipts/{id}/discrepancies`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Returns | `200` — `Discrepancy[]` |

#### `GET /api/inbound/receipts/{id}/putaway`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Returns | `200` — `PutawayTask[]` |

#### `POST /api/inbound/receipts/{id}/putaway`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Body | `{ taskId?: long, actualLocation?: string }` |
| Returns | `200` — `PutawayTask[]` |
| Behavior | If `taskId` is provided, completes that task. If omitted, generates tasks for all PASS-inspected lines (idempotent — returns existing tasks if already generated). |

#### `POST /api/inbound/receipts/{id}/post`

| Field | Value |
|-------|-------|
| Auth | `OPERATIONS_STAFF` or `ADMIN` |
| Returns | `200` — void |
| Behavior | Transitions a `COMPLETED` receipt to `POSTED`, recording `postedBy` and `postedAt`. Represents finalizing the receipt into inventory. |
| Errors | `400` if receipt is not in `COMPLETED` state. |

#### `POST /api/inbound/receipts/{id}/unpost`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` only |
| Returns | `200` — void |
| Behavior | Transitions a `POSTED` receipt to `UNPOSTED`, recording `unpostedBy` and `unpostedAt`. Only an ADMIN can reverse a posted receipt. |
| Errors | `400` if receipt is not in `POSTED` state. |

#### `POST /api/inbound/receipts/{id}/supervisor-review`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` only |
| Body | `{ discrepancyId: long, reasonCode: enum, notes?: string }` |
| Returns | `200` — void |
| Validation | Reviewer must not be the receipt creator (separation of duty). |
| Errors | `403` if not ADMIN. Business error if reviewer equals creator. |

---

### 9. NotificationController

In-app notification management with delivery tracking.

#### `GET /api/notifications`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Params | `page` (default 0), `size` (default 25) |
| Returns | `200` — `Page<NotificationResponse>` |
| Behavior | Returns notifications targeted at the authenticated user. |

#### `POST /api/notifications`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Body | `{ type: enum, title: string, body?: string, targetUserIds: long[] }` |
| Returns | `201` — void |

#### `POST /api/notifications/{id}/read`

| Field | Value |
|-------|-------|
| Auth | Authenticated (target user only) |
| Returns | `200` — void |
| Behavior | Records read timestamp. |

#### `GET /api/notifications/{id}/status`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Returns | `200` — `DeliveryStatusResponse[]` |
| Response Fields | `targetId`, `userId`, `read`, `readAt`, `deliveredAt`, `channel` |

---

### 10. ExportController

Data export/import and account deletion with password-encrypted file support.

#### `POST /api/exports/account`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | `{ exportType: enum, passwordProtected: boolean, exportPassword: string }` |
| Returns | `201` — `ExportResponse` |
| Validation | `exportPassword` is required. |
| Behavior | Generates export data, encrypts with AES-CBC (PBKDF2-derived key from password), writes `.enc` file. |

#### `GET /api/exports`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Returns | `200` — `ExportResponse[]` |
| Behavior | Returns only the authenticated user's exports. |

#### `GET /api/exports/{id}`

| Field | Value |
|-------|-------|
| Auth | Authenticated (owner only) |
| Returns | `200` — `ExportResponse` |

#### `GET /api/exports/{id}/download`

| Field | Value |
|-------|-------|
| Auth | Authenticated (owner only) |
| Returns | `200` — binary (`.enc` file) |
| Errors | Export not ready, expired, or access denied. |

#### `POST /api/imports/account`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | Raw JSON map (decrypted export data) |
| Returns | `200` — import summary string |

#### `POST /api/imports/account/file`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Content-Type | `multipart/form-data` |
| Params | `file`: `.enc` file, `password`: string |
| Returns | `200` — import summary string |
| Behavior | Decrypts file with password, then imports study data, fitness assessments, goals, check-ins, and profile metadata. Goal IDs are remapped during import. |

#### `DELETE /api/account`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Body | `{ password: string }` |
| Returns | `200` — void |
| Validation | Password must match current user's password. |
| Behavior | Hard-deletes all personal data (check-ins, goals, assessments, study plans, sessions) and the user row itself. Non-cascading FK references (inbound receipts, notifications, deletion requests) are set to NULL, preserving de-identified audit artifacts. |

**ExportResponse includes:** `id`, `userId`, `exportType`, `status`, `downloadReady`, `passwordProtected`, `expiresAt`, `createdAt`, `completedAt`.

> **Profile masking in exports:** Even inside the password-encrypted `.enc` file, identity fields (`username`, `email`, `phone`) are masked via `FieldMasker`. This protects privacy but means cross-account import cannot restore these fields from an export.

---

### 11. ReportingController

Role-based dashboards and performance monitoring.

#### `GET /api/dashboard`

| Field | Value |
|-------|-------|
| Auth | Authenticated |
| Returns | `200` — `DashboardResponse` |
| Behavior | Returns role-specific dashboard: user dashboard (fitness + study stats), ops dashboard (receipt status overview), or admin dashboard (system-wide metrics). Cached per user (5-minute TTL). |

**DashboardResponse:**

| Field | Type | Description |
|-------|------|-------------|
| `userRole` | string | The active role used to build this dashboard |
| `metrics` | `MetricEntry[]` | Pre-aggregated metric entries |
| `summary` | `Map<string, any>` | Role-specific key-value summary data |
| `recentActivity` | `ActivityEntry[]` | Activity feed items |
| `charts` | `Map<string, number[]>` | Chart data series |

#### `GET /api/admin/performance`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Returns | `200` — `List<{ query, avgDurationMs, maxDurationMs, callCount, status }>` |
| Behavior | Slow-query log analysis. |

---

### 12. Master Data CRUD Controllers

Five parallel controllers for academic master data entities: `TermController`, `SchoolController`, `MajorController`, `ClassController`, `CourseController`.

All follow the same pattern under `/api/admin/{entity-plural}`:

| Method | Path | Auth | Returns | Notes |
|--------|------|------|---------|-------|
| `POST` | `/api/admin/terms` | `ADMIN` | `201` — `MasterDataResponse` | Validates effective dates, duplicate codes |
| `GET` | `/api/admin/terms` | `ADMIN` | `200` — `Page<MasterDataResponse>` | Database-level pagination (default 25) |
| `GET` | `/api/admin/terms/{id}` | `ADMIN` | `200` — `MasterDataResponse` | |
| `PUT` | `/api/admin/terms/{id}` | `ADMIN` | `200` — `MasterDataResponse` | Logs field changes to change history |
| `DELETE` | `/api/admin/terms/{id}` | `ADMIN` | `200` — void | Soft-delete (sets `active=false`). Blocked if referenced. |

Replace `terms` with `schools`, `majors`, `classes`, `courses` for other entities.

**MasterDataRequest:**

| Field | Type | Required | Notes |
|-------|------|:--------:|-------|
| `code` | string | Y | Unique per entity type |
| `name` | string | Y | |
| `effectiveFrom` | date | Y | Must be before `effectiveTo` |
| `effectiveTo` | date | N | |
| `startDate` | date | N | Terms only |
| `endDate` | date | N | Terms only |
| `schoolId` | long | N | Majors only |
| `majorId` | long | N | Classes only |
| `classId` | long | N | Courses only |
| `termId` | long | N | Courses only |

---

### 13. MasterDataImportController

Bulk import from CSV or Excel files.

#### `POST /api/admin/master-data/imports`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Content-Type | `multipart/form-data` |
| Params | `file`: CSV/XLSX, `entityType`: string |
| Returns | `201` — `ImportJobResponse` |
| Behavior | Parses CSV via OpenCSV or XLSX via Apache POI. Skips header row. Validates each row individually (code, name required). Collects errors per row without aborting the batch. |

#### `GET /api/admin/master-data/imports/{id}`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Returns | `200` — `ImportJobResponse { id, fileName, entityType, totalRows, successCount, errorCount, status, errors[] }` |

---

### 14. MasterDataExportController

Export master data to CSV or Excel.

#### `GET /api/admin/master-data/export`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Params | `entityType`: string, `format`: `csv` or `xlsx` (default `xlsx`) |
| Returns | `200` — binary file download |
| Columns | Code, Name, Effective From, Effective To, Active |

---

### 15. MasterDataMergeController

Duplicate detection and merge operations.

#### `GET /api/admin/master-data/merge`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Params | `entityType`: string |
| Returns | `200` — `MergeCandidate[]` |
| Behavior | Finds pairs with >= 70% name similarity (Levenshtein distance). |

#### `POST /api/admin/master-data/merge`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Body | `{ entityType: string, sourceId: long, targetId: long }` |
| Returns | `200` — `MergeOperation` |
| Behavior | Source record is soft-deleted. All references are remapped to the target. Full audit trail preserved. |

---

### 16. ChangeHistoryController

Audit trail for master data mutations.

#### `GET /api/admin/master-data/history`

| Field | Value |
|-------|-------|
| Auth | `ADMIN` |
| Params | `entityType`: string (required), `entityId`: long (optional) |
| Returns | `200` — `ChangeHistoryResponse[]` |
| Fields | `id`, `entityType`, `entityId`, `field`, `oldValue`, `newValue`, `changedBy`, `changedAt` |

---

## Domain Policies

### Goal Recalculation Policy

Triggered when a goal accumulates 2 consecutive missed check-ins:

| Action | Detail |
|--------|--------|
| Extend target date | +20% of remaining duration (minimum 1 day) |
| Reduce target value | Move 10% closer to current value |
| Reset missed counter | Set `missedCheckIns = 0` |
| Update status | Set to `RECALCULATED` |
| Audit | Creates `GoalAdjustmentAudit` record with previous/new values and reason |

### Inbound State Machine

Each transition enforces guards:

| Transition | Guard | Required Role |
|------------|-------|---------------|
| DRAFT → RECEIVING | Receipt must have at least one line | `OPERATIONS_STAFF` or `ADMIN` |
| RECEIVING → INSPECTION | All lines must have `receivedQty > 0` | `OPERATIONS_STAFF` or `ADMIN` |
| INSPECTION → PUTAWAY | All lines must be inspected (not PENDING). All supervisor-required discrepancies must be resolved. | `OPERATIONS_STAFF` or `ADMIN` |
| PUTAWAY → COMPLETED | All putaway tasks must be COMPLETED | `OPERATIONS_STAFF` or `ADMIN` |
| COMPLETED → POSTED | None (records `postedBy` + `postedAt`) | `OPERATIONS_STAFF` or `ADMIN` |
| POSTED → UNPOSTED | None (records `unpostedBy` + `unpostedAt`) | `ADMIN` only |
| Any → REJECTED | Always allowed | `OPERATIONS_STAFF` or `ADMIN` |

State history is recorded for every transition with `fromState`, `toState`, `changedBy`, and `reason`.

**InboundReceiptResponse includes posting fields:** `postedBy`, `postedAt`, `unpostedBy`, `unpostedAt` (all nullable).

### Discrepancy Detection

Auto-triggered when `receivedQty` is recorded for a line:

| Condition | Action |
|-----------|--------|
| Variance > 2% **or** > 5 units | Create `Discrepancy` record |
| Either threshold exceeded | Set `supervisorRequired = true` on discrepancy |
| Supervisor required | Flag receipt `supervisorApprovalRequired = true` |

Discrepancies must be resolved by an `ADMIN` via the supervisor-review endpoint before the receipt can transition from INSPECTION to PUTAWAY.

### Referential Integrity

Master data deletion is blocked when referenced:

| Entity | Blocked If Referenced By |
|--------|--------------------------|
| School | Majors |
| Major | Classes |
| Class | Courses |
| Term | Courses or Study Plans |
| Course | Study Plans |

### Delivery Channel Policy

| Channel | Default | Configuration |
|---------|---------|---------------|
| `IN_APP` | Enabled (always) | Not configurable |
| `EMAIL` | Disabled | `NOTIFICATION_EMAIL_ENABLED` |
| `SMS` | Disabled | `NOTIFICATION_SMS_ENABLED` |
| `WECOM` | Disabled | `NOTIFICATION_WECOM_ENABLED` |

In offline mode, only `IN_APP` delivery is active.

---

## Encryption Model

### At-Rest Encryption (Database Fields)

| Property | Value |
|----------|-------|
| Algorithm | AES-256-GCM |
| IV Length | 96-bit (12 bytes), random per field |
| Tag Length | 128-bit |
| Key Source | `APP_ENCRYPTION_KEY` environment variable (hex-encoded) |
| Storage | Base64-encoded (IV prepended to ciphertext) |

**Encrypted Fields:**

| Entity | Fields |
|--------|--------|
| Goal | `targetValue`, `startValue`, `currentValue`, `metricsEncrypted` |
| CheckIn | `value` |
| Assessment | Body measurement metrics |

### Export File Encryption

| Property | Value |
|----------|-------|
| Algorithm | AES-256-CBC with PKCS5Padding |
| Key Derivation | PBKDF2WithHmacSHA256, 65536 iterations, 256-bit key |
| Salt | 16 bytes random (prepended to file) |
| IV | 16 bytes random (prepended after salt) |
| File Format | `[16-byte salt][16-byte IV][ciphertext]` → `.enc` extension |

### Field Masking

Sensitive fields (userId, username, email, phone) are masked in logs and API responses using `FieldMasker`. This masking is also applied inside encrypted export files — identity fields are redacted even in the password-protected `.enc` bundle. Logging uses the masked value; raw PII never appears in log output.

---

## Scheduled Tasks

| Task | Interval | Description |
|------|----------|-------------|
| Export file cleanup | 24 hours | Deletes expired `.enc` files past retention period |
| Dashboard aggregation | Nightly | Pre-aggregates metrics into `t_dashboard_summary` |

---

## Validation Rules

| Rule | Value |
|------|-------|
| Password minimum length | 8 characters |
| Height feet range | 1–8 |
| Height inches range | 0–11 |
| Weight | Must be positive |
| Goal target value | Must be positive |
| Goal target date | Must be in the future |
| Check-in value | Must be positive |
| SM-2 quality rating | 0–5 (integer) |
| Study plan title | Non-blank |
| Master data code | Non-blank, unique per type |
| Master data effective dates | `effectiveFrom` must precede `effectiveTo` |
| Login lockout threshold | 5 failed attempts |
| Lockout duration | 15 minutes |
| Session timeout | 30 minutes of inactivity |
| Discrepancy threshold (percent) | > 2% |
| Discrepancy threshold (units) | > 5 units |
| Export password | Required for all exports |
| Export retention | 30 days (configurable) |
| Pagination default | 25 rows per page |
| Pagination maximum | 100 rows per page |
| Merge similarity threshold | >= 70% (Levenshtein) |

---

## Configuration Reference

All configuration is in `application.yml` with environment variable overrides:

| Property | Env Variable | Default |
|----------|-------------|---------|
| Server port | `APP_PORT` | `8080` |
| Database URL | `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/campusfit` |
| Database username | `SPRING_DATASOURCE_USERNAME` | `campusfit` |
| Database password | `SPRING_DATASOURCE_PASSWORD` | — |
| Encryption key | `APP_ENCRYPTION_KEY` | — (required) |
| Session timeout | `APP_SESSION_TIMEOUT_MINUTES` | `30` |
| Lockout attempts | `APP_LOCKOUT_ATTEMPTS` | `5` |
| Lockout duration | `APP_LOCKOUT_DURATION_MINUTES` | `15` |
| Export retention | `EXPORT_RETENTION_DAYS` | `30` |
| Email notifications | `NOTIFICATION_EMAIL_ENABLED` | `false` |
| SMS notifications | `NOTIFICATION_SMS_ENABLED` | `false` |
| WeCom notifications | `NOTIFICATION_WECOM_ENABLED` | `false` |
| Cache spec | — | `maximumSize=1000,expireAfterWrite=300s` |
| Connection pool max | — | `20` |
| Connection pool min idle | — | `5` |
