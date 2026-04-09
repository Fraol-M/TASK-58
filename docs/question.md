# Business Logic Questions Log — CampusFit Learning & Inventory Operations

## 1. Default Role on Sign-Up

**Question:**
The prompt defines three roles (Regular User, Operations Staff, Administrator) but does not specify which role a new user receives at sign-up.

**Assumption:**
All users register as `REGULAR_USER` by default. Only an Administrator can assign additional roles.

**Solution:**
Sign-up creates a `REGULAR_USER` role assignment automatically. Admin role assignment is a separate `POST /api/admin/users/{userId}/roles` endpoint restricted to `ADMIN`.

## 2. Password Minimum Requirements

**Question:**
The prompt specifies bcrypt hashing but does not define a minimum password length or complexity requirement.

**Assumption:**
A minimum of 8 characters is required; no uppercase/digit/symbol complexity rules are enforced.

**Solution:**
Validated `@Size(min = 8)` on the `password` field of `SignUpRequest`. No additional complexity rules applied.

## 3. Session Timeout Model

**Question:**
The prompt states "session timeout after 30 minutes of inactivity" but does not specify whether the timeout is a fixed window from login or a sliding window from the last activity.

**Assumption:**
The timeout is a sliding window — each authenticated request resets the inactivity clock.

**Solution:**
`SessionAuthenticationFilter` refreshes `lastAccessedAt` and `expiresAt` on every valid request. Timeout is checked against `lastAccessedAt + timeoutMinutes`.

## 4. Lockout Expiry Behavior

**Question:**
The prompt states "lockout after 5 failed attempts for 15 minutes" but does not define what happens when the lockout period expires.

**Assumption:**
The account automatically unlocks and the failed attempt counter resets.

**Solution:**
`LoginAttemptService.isLocked()` checks whether `lockoutUntil` has passed; if expired, it resets `failedAttempts` to 0 and status to `ACTIVE`.

## 5. Fitness Assessment Measurement Ranges

**Question:**
The prompt requires "height in ft/in, weight in lb, optional body measurements" but does not define valid ranges.

**Assumption:**
Height is 1–8 feet and 0–11 inches; weight must be positive. Body measurements (waist, chest, arm, body fat) are optional with no enforced range.

**Solution:**
`AssessmentRequest` applies `@Min`/`@Max` on `heightFeet` (1–8) and `heightInches` (0–11), `@Positive` on `weightLbs`. Optional fields are unconstrained.

## 6. Goal Recalculation Trigger Threshold

**Question:**
The prompt states "recalculating weekly targets if two consecutive check-ins are missed" but does not define what constitutes a missed check-in or how recalculation is computed.

**Assumption:**
A missed check-in is tracked by an incrementing counter on the goal. When the counter reaches 2, recalculation fires. Recalculation extends the deadline by 20% of remaining duration and reduces the target by 10% toward the current value.

**Solution:**
`GoalRecalculationPolicy.recalculate()` applies the formula, resets the counter, sets status to `RECALCULATED`, and creates an audit record.

## 7. Goal Start Value Initialization

**Question:**
The prompt implies goals track progress from a starting point but does not define where `startValue` comes from.

**Assumption:**
`startValue` is set from the user's current assessment metric at goal creation time (e.g., current weight for a weight-loss goal).

**Solution:**
Goal creation links the goal to the latest assessment and derives `startValue` from it.

## 8. Milestone Achievement Detection

**Question:**
The prompt mentions "phase milestones displayed on a dashboard" but does not define when a milestone is marked achieved.

**Assumption:**
A milestone is achieved when a check-in causes `currentValue` to cross the milestone's `targetValue`.

**Solution:**
Check-in processing compares `currentValue` against each milestone's threshold and sets `achievedDate` when crossed.

## 9. Discrepancy Threshold Logic

**Question:**
The prompt states ">2% or >5 units requires supervisor review" but does not specify whether the thresholds are AND or OR.

**Assumption:**
The thresholds are OR — exceeding either one triggers a discrepancy and requires supervisor review.

**Solution:**
`DiscrepancyService.detectAndCreate()` creates a discrepancy record and sets `supervisorRequired = true` if variance > 2% **or** > 5 units.

## 10. Supervisor Role Identity

**Question:**
The prompt refers to "supervisor review" for discrepancies but does not define a distinct supervisor role.

**Assumption:**
The `ADMIN` role serves as the supervisor authority. Ordinary `OPERATIONS_STAFF` cannot approve discrepancies they or their peers created.

**Solution:**
Supervisor review endpoint is restricted to `ADMIN` via `@PreAuthorize("hasRole('ADMIN')")`. A separation-of-duty check also prevents the receipt creator from reviewing their own discrepancies.

## 11. Inbound State Reversal

**Question:**
The prompt defines the state machine (Draft → Receiving → Inspection → Putaway → Completed) but does not specify whether receipts can move backward to a previous state.

**Assumption:**
States only progress forward. The only escape from any non-terminal state is `REJECTED`.

**Solution:**
`InboundStateMachine.validateTransition()` enforces strictly forward transitions and always allows transition to `REJECTED`.

## 12. Putaway Location Suggestion Logic

**Question:**
The prompt states "accept system putaway recommendations" but does not define how locations are determined.

**Assumption:**
A simple zone-based suggestion is sufficient for the MVP. Location is derived from the item code.

**Solution:**
`PutawayService.generateTasks()` assigns `ZONE-A-{itemCode}` as the suggested location. Operators can override with an actual location on completion.

## 13. Putaway Task Idempotency

**Question:**
The prompt does not specify behavior when putaway tasks are requested multiple times for the same receipt.

**Assumption:**
Task generation must be idempotent — requesting tasks for a receipt that already has them returns the existing tasks.

**Solution:**
`PutawayService.generateTasks()` checks for existing tasks first and returns them without creating duplicates.

## 14. Academic Hierarchy Field Optionality

**Question:**
The prompt requires study plans "tied to locally managed academic structures (term, school, major, class, course)" but does not specify whether all five levels are mandatory.

**Assumption:**
All hierarchy fields are optional on the study plan, allowing partial linkage (e.g., a plan linked to just a term and course).

**Solution:**
`StudyPlanRequest` defines `termId`, `schoolId`, `majorId`, `classId`, and `courseId` as optional. Server-side validation confirms that any non-null ID references an existing entity.

## 15. Master Data Deletion Semantics

**Question:**
The prompt states "blocks deletion when referenced by plans" but does not specify hard-delete vs soft-delete.

**Assumption:**
Deletion is a soft-delete (sets `active = false`) so historical references remain valid. Referential integrity blocks deletion only when active child entities exist.

**Solution:**
`ReferentialIntegrityPolicy` checks for child references before allowing deactivation. The entity remains in the database for historical queries.

## 16. Master Data Merge Behavior

**Question:**
The prompt requires "merge duplicates" but does not specify what happens to the source record or its references after merge.

**Assumption:**
The source record is soft-deleted, and all foreign key references are repointed to the target record. The merge operation is logged as an immutable audit entry.

**Solution:**
`DuplicateMergeService.merge()` repoints references, soft-deletes the source, and creates a `MergeOperation` record with source/target IDs and the merging user.

## 17. Duplicate Detection Algorithm

**Question:**
The prompt requires "merge duplicates" but does not define how duplicates are detected.

**Assumption:**
Name-based similarity is sufficient. A Levenshtein distance threshold of >= 70% identifies candidates.

**Solution:**
`MasterDataMergeController` computes pairwise Levenshtein distance between entity names and returns pairs exceeding 70% similarity.

## 18. Export Encryption Algorithm

**Question:**
The prompt requires a "password-protected file" but does not specify the encryption algorithm or key derivation.

**Assumption:**
AES-256-CBC with PBKDF2-SHA256 key derivation (65536 iterations) is appropriate for offline file protection.

**Solution:**
`ExportService` derives a 256-bit key from the user's password via PBKDF2, generates a random 16-byte salt and 16-byte IV, encrypts with AES-256-CBC, and prepends salt + IV to the file.

## 19. Profile Data in Exports — Masked or Raw

**Question:**
The prompt states "sensitive fields are masked in logs and exports by default" but also requires "one-click export to a file for cross-account migration."

**Current Implementation:**
`FieldMasker` is applied to profile identity fields (`username`, `email`, `phone`) inside the encrypted export bundle as well as in log output and API responses. This means cross-account import cannot restore these fields from an export file.

**Trade-off:**
Masking in the encrypted bundle provides defense-in-depth if the password is weak or shared. However, it breaks faithful round-trip import (profile fields are lost on import). The import service skips masked fields rather than writing `••••••••` into the target account.

**Note:** Restoring full cross-account migration (unmasked profile inside encrypted bundle) requires the export to include raw values. This is tracked as a pending improvement.

## 20. Account Deletion — Hard-Delete vs Anonymization

**Question:**
The prompt states "hard-deletes identifiers while retaining de-identified audit events for compliance." This creates tension between full deletion and audit retention.

**Assumption:**
The user row itself is deleted (hard-delete). Personal data records (goals, check-ins, assessments, study plans, sessions) are also deleted. Non-cascading references from operational records (inbound receipts, notifications) are set to NULL by the database, preserving those records as de-identified audit artifacts. The `DeletionRequest` record serves as the compliance audit trail.

**Solution:**
Migration V17 changes non-cascading FKs to `ON DELETE SET NULL`. `AccountDeletionService` explicitly deletes personal data, then deletes the user row. The deletion request record is preserved with `userId = null`.

## 21. Account Deletion Password Confirmation

**Question:**
The prompt does not explicitly state whether account deletion requires re-authentication.

**Assumption:**
Destructive account operations should require password re-verification to prevent accidental or unauthorized deletion.

**Solution:**
The `DELETE /api/account` endpoint requires a password in the request body. The backend verifies it against the user's stored hash before processing deletion.

## 22. Notification Delivery Channels in Offline Mode

**Question:**
The prompt states "delivery is in-app only in offline mode, while email/SMS/WeCom are configurable but disabled unless explicitly enabled."

**Assumption:**
External channels are disabled by default via environment variables. The system always creates in-app notification records regardless of channel configuration.

**Solution:**
`application.yml` defaults all external channels to `false`. The notification service always writes `NotificationTarget` records for in-app delivery. External channel delivery would be additive if enabled.

## 23. Notification Read Receipt Scope

**Question:**
The prompt requires "read receipts and delivery status visible to authorized users" but does not define who can see delivery status.

**Assumption:**
Any authenticated user can read their own notifications and mark them as read. Only `ADMIN` can view delivery status across all recipients.

**Solution:**
`POST /notifications/{id}/read` is open to the target user. `GET /notifications/{id}/status` is restricted to `ADMIN` via `@PreAuthorize`.

## 24. Spaced Repetition Algorithm Choice

**Question:**
The prompt mentions "forgetting points" and review reminders but does not specify the scheduling algorithm.

**Assumption:**
The SM-2 algorithm (used by Anki and SuperMemo) is appropriate for spaced repetition scheduling.

**Solution:**
Forgetting points are initialized with `easeFactor = 2.5`, `intervalDays = 1`, `repetitions = 0`. On review with a quality rating (0–5), the SM-2 formula recalculates `easeFactor`, `intervalDays`, `repetitions`, and `nextReviewDate`.

## 25. Streak Calculation Logic

**Question:**
The prompt mentions "streak indicators" for study but does not define how streaks are computed.

**Assumption:**
A streak increments for each consecutive day with at least one completion. It resets to zero on a missed day. The longest streak is tracked separately.

**Solution:**
`Streak` entity stores `currentStreak`, `longestStreak`, and `lastActiveDate` per user-plan pair. Completion recording updates the streak based on the gap between `lastActiveDate` and the new completion date.

## 26. CSV Import Date Format

**Question:**
The prompt requires "bulk Excel/CSV import" but does not specify the expected date format.

**Assumption:**
Both ISO format (`yyyy-MM-dd`) and US format (`MM/dd/yyyy`) should be accepted.

**Solution:**
`MasterDataImportService.parseDate()` attempts ISO format first, then falls back to US format. Invalid dates produce a row-level error without aborting the batch.

## 27. CSV Parsing Library

**Question:**
The prompt does not specify how CSV files should be parsed.

**Assumption:**
A standards-compliant CSV parser is needed to handle quoted fields, embedded commas, and escaped characters correctly.

**Solution:**
Used OpenCSV (`CSVReader` / `CSVReaderBuilder`) for CSV parsing and Apache POI (`XSSFWorkbook`) for Excel files. Both are declared as Maven dependencies.

## 28. Import Error Handling Strategy

**Question:**
The prompt requires "review validation errors inline" but does not specify whether a single bad row should abort the entire import.

**Assumption:**
Bad rows are collected as errors without aborting the batch. The import continues processing all rows and reports a per-row error summary.

**Solution:**
`MasterDataImportService` validates each row independently, collects errors with row number and field details, and returns a complete `ImportJobResponse` with `successCount`, `errorCount`, and `errors[]`.

## 29. Export File Retention Cleanup

**Question:**
The prompt states "configurable retention policy (default 30 days)" but does not define how expired files are cleaned up.

**Assumption:**
A scheduled background task deletes expired export files daily.

**Solution:**
`FileRetentionService.cleanupExpiredFiles()` runs every 24 hours, finds export jobs past their `expiresAt`, deletes the physical `.enc` file, and marks the job as `FAILED`.

## 30. Receipt Number Generation

**Question:**
The prompt does not specify how inbound receipt numbers are generated.

**Assumption:**
Receipt numbers are system-generated and unique.

**Solution:**
`InboundReceiptService.create()` generates a receipt number as `RCV-{8-char-UUID}` (e.g., `RCV-A1B2C3D4`), ensuring uniqueness via a database unique constraint.

## 31. Dashboard Caching Strategy

**Question:**
The prompt requires "result caching with a 5-minute TTL" but does not specify what is cached.

**Assumption:**
Dashboard responses are cached per user principal. Master data lists are cached per entity type.

**Solution:**
`DashboardService.getDashboard()` is annotated with `@Cacheable(value = "dashboard", key = "#principal.id")`. Caffeine cache is configured with `maximumSize=1000, expireAfterWrite=300s` (5 minutes).

## 32. Pre-Aggregated Dashboard Data

**Question:**
The prompt requires "pre-aggregated tables for dashboards" but does not specify which metrics or when aggregation runs.

**Assumption:**
A nightly batch job computes system-wide metrics (total goals, active goals, total study plans) and stores them in a summary table.

**Solution:**
`AggregationService.computeNightlyAggregation()` runs at 2:00 AM daily via `@Scheduled(cron)`, writes `DashboardSummary` records. The admin dashboard reads from this pre-aggregated table.

## 33. Database Partitioning Strategy

**Question:**
The prompt requires "MySQL partitioning by term and month" but does not specify which tables.

**Assumption:**
High-volume time-series tables benefit most: check-ins (by month), daily completions (by month), and forgetting points (by month).

**Solution:**
- V3 migration partitions `t_check_in` by `YEAR(created_at) * 100 + MONTH(created_at)`
- V16 migration partitions `t_daily_completion` by `completed_date` and `t_forgetting_point` by `next_review_date`
- Monthly partitions created for 2026–2027 plus a `p_future` catch-all

## 34. Pagination Default and Maximum

**Question:**
The prompt states "pagination defaults of 25 rows per page" but does not define a maximum page size.

**Assumption:**
A maximum of 100 rows per page prevents accidental full-table loads.

**Solution:**
`application.yml` sets `app.pagination.default-size: 25` and `app.pagination.max-size: 100`. All list endpoints accept `page` and `size` parameters with these defaults.

## 35. Pagination Implementation Location

**Question:**
The prompt does not specify whether pagination should happen at the database level or in application memory.

**Assumption:**
Pagination must happen at the database level to support large data volumes.

**Solution:**
All list endpoints use Spring Data's `Pageable` interface, which translates to SQL `LIMIT`/`OFFSET` queries. No in-memory list slicing.

## 36. Slow Query Logging Source

**Question:**
The prompt requires "built-in slow-query logging with an admin performance screen" but does not specify the data source.

**Assumption:**
MySQL's `performance_schema` provides the necessary slow-query data without custom instrumentation.

**Solution:**
`SlowQueryLogService.getPerformanceMetrics()` queries `performance_schema.events_statements_summary_by_digest` for the top 25 slowest queries, returning average/max duration and call count.

## 37. Frontend Development Without Backend

**Question:**
The prompt does not specify whether the frontend should be independently developable without a running backend.

**Assumption:**
Supporting standalone frontend development accelerates UI iteration.

**Solution:**
An `ApiAdapter` interface abstracts all backend communication. `AdapterFactory` instantiates `MockAdapter` (fake data) when `VITE_MOCK_MODE=true` or `HttpAdapter` (real API calls) otherwise. The mock adapter returns deterministic data for all 40+ endpoints.

## 38. Operations Page Access for Admin

**Question:**
The prompt restricts operations pages to "Warehouse/Operations Staff" but requires `ADMIN` to perform supervisor review, which happens on the discrepancies page.

**Assumption:**
Admin must be able to access all operations pages to perform supervisor review.

**Solution:**
Frontend route guards for `/operations/*` accept both `OPERATIONS_STAFF` and `ADMIN` roles. The backend guard (`roleGuard`) supports role arrays.

## 39. Effective Date Validation

**Question:**
The prompt stores "effective dates (MM/DD/YYYY)" but does not define validation rules beyond format.

**Assumption:**
`effectiveFrom` is required and `effectiveTo` is optional. If both are present, `effectiveFrom` must precede `effectiveTo`.

**Solution:**
`EffectiveDatePolicy.validate()` enforces the ordering constraint and rejects invalid date pairings.

## 40. Change History Granularity

**Question:**
The prompt requires "complete change audit" for master data but does not define what constitutes a logged change.

**Assumption:**
Every create, update (per changed field), and merge operation is logged with old/new values.

**Solution:**
`ChangeHistoryService` creates individual `ChangeHistory` records for each mutated field (e.g., if `name` changes, one record with `field=name`, `oldValue`, `newValue`). Create and delete operations are also logged.

## 41. Import Goal ID Remapping

**Question:**
The prompt requires "cross-account migration" via export/import but does not define how entity IDs are handled when importing into a different account.

**Assumption:**
Goal IDs from the source account will conflict with existing IDs in the target account. Check-ins reference goal IDs and need remapping.

**Solution:**
`AccountImportService.importAccountData()` creates new goals during import and builds an `oldId → newId` remap table. Check-ins are then imported with the remapped goal IDs.

## 42. Export Data Scope

**Question:**
The prompt lists three export types (account, study, fitness) but does not define what each includes.

**Assumption:**
- `ACCOUNT_DATA` — profile metadata + study data + fitness data + goals + check-ins (full account)
- `STUDY_DATA` — study plans, completions, forgetting points only
- `FITNESS_DATA` — assessments, goals, check-ins only

**Solution:**
`ExportService.generateExportData()` switches on export type and bundles the appropriate data subsets. `ACCOUNT_DATA` includes all other types plus profile metadata.

## 43. API Response Envelope

**Question:**
The prompt does not specify a standard response format.

**Assumption:**
A consistent envelope simplifies frontend error handling and response parsing.

**Solution:**
All endpoints return `ApiResponse<T>` with `{ success: boolean, data: T, error: string? }`. Error responses use a separate `ErrorResponse` with `timestamp`, `status`, `error`, `message`, `path`, and optional `fieldErrors`.

## 44. Error HTTP Status Code Mapping

**Question:**
The prompt does not define how business errors map to HTTP status codes.

**Assumption:**
Standard REST conventions apply: not-found → 404, business rule violation → 400, auth failure → 403, validation failure → 422, unexpected error → 500.

**Solution:**
`GlobalExceptionHandler` maps `ResourceNotFoundException` → 404, `BusinessException` → 400, `AccessDeniedException` → 403, `MethodArgumentNotValidException` → 422 (with field-level errors), and generic `Exception` → 500.

## 45. Inspection State Before Putaway Transition

**Question:**
The prompt defines inspection as part of the workflow but does not specify what "all lines inspected" means for the transition guard.

**Assumption:**
Every line must have an inspection result that is not `PENDING` (i.e., either `PASS` or `FAIL`). Additionally, all supervisor-required discrepancies must be resolved.

**Solution:**
`InboundStateMachine` validates `INSPECTION → PUTAWAY` by checking that no lines have `inspectionResult == PENDING` and no unresolved supervisor-required discrepancies exist.

## 46. Putaway Task Generation Scope

**Question:**
The prompt does not specify whether putaway tasks are generated for all lines or only for inspected-pass lines.

**Assumption:**
Only lines with `inspectionResult == PASS` receive putaway tasks. Failed lines do not need to be put away.

**Solution:**
`PutawayService.generateTasks()` filters lines by `InspectionResult.PASS` before creating tasks.

## 47. Notification Type Taxonomy

**Question:**
The prompt mentions "announcements, reminders, and follow-ups" but does not define whether these are structured types or free-form categories.

**Assumption:**
These are enumerated types enforced by the schema.

**Solution:**
`Notification.NotificationType` enum: `ANNOUNCEMENT`, `REMINDER`, `FOLLOW_UP`. The notification creation endpoint validates against this enum.

## 48. Study Plan Export/Import Format

**Question:**
The prompt requires study data export for "cross-account migration" but does not specify the format.

**Assumption:**
JSON is the natural format since the data is structured and the system already uses JSON for API communication.

**Solution:**
`StudyExportImportService` serializes plans, completions, and forgetting points to a JSON bundle (`StudyExportData` DTO). Import deserializes and remaps IDs.

## 49. Optimistic Concurrency on Inbound Receipts

**Question:**
The prompt does not specify how concurrent edits to the same receipt are handled.

**Assumption:**
Optimistic locking prevents lost updates when multiple users edit the same receipt.

**Solution:**
`InboundReceipt` uses a `@Version` field. JPA automatically throws `OptimisticLockException` if two concurrent updates collide.

## 50. AES Encryption Key Management

**Question:**
The prompt requires "AES-256 encryption at rest" but does not specify key provisioning.

**Assumption:**
The encryption key is provided at deploy time via an environment variable. There is no runtime key rotation or key management service.

**Solution:**
`APP_ENCRYPTION_KEY` is a 256-bit hex-encoded key injected via environment variable. `EncryptionService` uses it with AES-256-GCM. Key rotation requires re-encryption of all affected fields (not automated).

## 51. Inbound Receipt Posting and Unposting

**Question:**
The prompt defines a state machine ending at `COMPLETED` but does not specify how received goods are finalized into inventory, or whether that action can be reversed.

**Assumption:**
A `COMPLETED` receipt represents a verified and put-away delivery. A separate "post" action is needed to formally commit the receipt to inventory (e.g., trigger stock-level updates in a future inventory module). Unposting should be possible only by administrators, since it represents a reversal of a committed inventory entry.

**Solution:**
Two new states and two new endpoints are added after `COMPLETED`:

- `COMPLETED → POSTED` via `POST /api/inbound/receipts/{id}/post` — accessible to `OPERATIONS_STAFF` or `ADMIN`. Records `postedBy` and `postedAt` on the receipt.
- `POSTED → UNPOSTED` via `POST /api/inbound/receipts/{id}/unpost` — restricted to `ADMIN` only. Records `unpostedBy` and `unpostedAt`.

These states and audit fields are persisted via migration V20 (`posted_by`, `posted_at`, `unposted_by`, `unposted_at` columns on `t_inbound_receipt`).

## 52. Study Plan Partitioning — Foreign Key Enforcement

**Question:**
MySQL's partitioned InnoDB tables do not support foreign keys. V19 adds `RANGE` partitioning by `term_id` to `t_study_plan`, which requires dropping all FK constraints on that table. How is referential integrity maintained?

**Assumption:**
Service-layer validation is a viable substitute for FK enforcement in this context. The affected FKs (user → study plan, study plan → study plan items/completions/forgetting-points/streaks) are low-risk for orphan creation because inserts always flow through service methods.

**Solution:**
V19 drops FK constraints on `t_study_plan` and all child tables that reference it. The service layer (`StudyPlanService`, `DailyCompletionService`, etc.) validates existence before persisting. A `term_id = 0` sentinel lands rows with no assigned term in the `p_no_term` partition.
