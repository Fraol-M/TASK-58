# CampusFit Static Delivery Acceptance & Architecture Audit (2026-04-09)

## 1. Verdict
- Overall conclusion: **Partial Pass**
- Rationale: The repository is substantial and includes broad module coverage, but there are material defects in a core inbound flow and multiple high-severity consistency/testability risks that block clean acceptance.

## 2. Scope and Static Verification Boundary
- Reviewed:
  - Top-level docs and run/test/config instructions ([repo/README.md:35](repo/README.md#L35), [repo/.env.example:15](repo/.env.example#L15), [repo/docker-compose.yml:11](repo/docker-compose.yml#L11), [docs/api_spec.md:212](docs/api_spec.md#L212), [docs/design.md:24](docs/design.md#L24)).
  - Backend entry points/security/auth/business modules/migrations/tests under [repo/services/api](repo/services/api).
  - Frontend routes/guards/adapters/stores/tests under [repo/apps/web](repo/apps/web).
- Not reviewed in depth:
  - Every single UI component style/state edge case.
  - Third-party library internals.
- Intentionally not executed:
  - No project startup, no Docker, no tests, no external services.
- Manual verification required for:
  - Runtime DB migration behavior on real MySQL.
  - Real session timeout/lockout timing in live requests.
  - End-to-end encryption/decryption interoperability in deployed environment.

## 3. Repository / Requirement Mapping Summary
- Prompt core goals mapped:
  - Offline auth + RBAC + lockout/session handling: implemented in security/auth modules ([repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:49](repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java#L49), [repo/services/api/src/main/java/com/campusfit/auth/service/AuthService.java:74](repo/services/api/src/main/java/com/campusfit/auth/service/AuthService.java#L74), [repo/services/api/src/main/java/com/campusfit/auth/service/LoginAttemptService.java:28](repo/services/api/src/main/java/com/campusfit/auth/service/LoginAttemptService.java#L28)).
  - Fitness/study/inbound/master-data/notifications/export/reporting modules exist with dedicated controllers/services/entities.
  - Encryption-at-rest and export password protection are implemented ([repo/services/api/src/main/java/com/campusfit/fitness/entity/Assessment.java:59](repo/services/api/src/main/java/com/campusfit/fitness/entity/Assessment.java#L59), [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:235](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L235)).
- Main gaps from prompt:
  - Inbound putaway recommendation/confirmation can be bypassed before completion.
  - Cross-account export/import handling masks critical profile identity fields and then skips restoring them.
  - Static testability has a concrete compile-level inconsistency in backend tests.

## 4. Section-by-section Review

### 1. Hard Gates

#### 1.1 Documentation and static verifiability
- Conclusion: **Partial Pass**
- Rationale: Startup/config/test instructions are present and mostly consistent; however, static verifiability is reduced by at least one test-signature mismatch and no integration-level test layer.
- Evidence:
  - [repo/README.md:35](repo/README.md#L35), [repo/README.md:64](repo/README.md#L64), [repo/.env.example:15](repo/.env.example#L15)
  - [repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java:156](repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java#L156)
  - [repo/services/api/src/main/java/com/campusfit/inbound/service/DiscrepancyService.java:84](repo/services/api/src/main/java/com/campusfit/inbound/service/DiscrepancyService.java#L84)
  - Web-layer/unit-layer tests only: [repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java:40](repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java#L40), [repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java:34](repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java#L34)
- Manual verification note: Runtime behavior cannot be asserted without execution.

#### 1.2 Material deviation from Prompt
- Conclusion: **Partial Pass**
- Rationale: Implementation is largely centered on prompt scope, but core inbound putaway semantics are weakened by bypass paths.
- Evidence:
  - Prompt-aligned modules exist: [repo/services/api/README.md:13](repo/services/api/README.md#L13)
  - Bypass risk: [repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java:113](repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java#L113), [repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:117](repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java#L117)

### 2. Delivery Completeness

#### 2.1 Core explicit requirements coverage
- Conclusion: **Partial Pass**
- Rationale: Most core flows are present, but there are high-impact mismatches in inbound completion logic and cross-account export/import identity handling.
- Evidence:
  - Fitness recalculation + audit: [repo/services/api/src/main/java/com/campusfit/fitness/policy/GoalRecalculationPolicy.java:30](repo/services/api/src/main/java/com/campusfit/fitness/policy/GoalRecalculationPolicy.java#L30)
  - Inbound state machine: [repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java:56](repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java#L56)
  - Export/import masking behavior: [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:191](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L191), [repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java:165](repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java#L165)

#### 2.2 End-to-end deliverable vs demo fragment
- Conclusion: **Pass**
- Rationale: Full multi-module structure with backend/frontend/docs/migrations/tests exists; this is not a single-file sample.
- Evidence:
  - Repository structure and module depth: [repo/README.md:1](repo/README.md#L1)
  - Multiple backend modules and migrations: [repo/services/api/README.md:13](repo/services/api/README.md#L13), [repo/services/api/src/main/resources/db/migration/V1__create_auth_tables.sql:1](repo/services/api/src/main/resources/db/migration/V1__create_auth_tables.sql#L1)

### 3. Engineering and Architecture Quality

#### 3.1 Structure and decomposition
- Conclusion: **Pass**
- Rationale: Domain modules are clearly decomposed with controller/service/repository/entity layers.
- Evidence:
  - Architecture decomposition: [docs/design.md:107](docs/design.md#L107)
  - Package organization reflected in code tree under [repo/services/api/src/main/java/com/campusfit](repo/services/api/src/main/java/com/campusfit)

#### 3.2 Maintainability and extensibility
- Conclusion: **Partial Pass**
- Rationale: Structure is extensible, but there are signs of drift in shared contracts (status enums/types) and an unused generic audit service.
- Evidence:
  - Front/backend status mismatch: [repo/apps/web/src/services/adapters/api-adapter.interface.ts:49](repo/apps/web/src/services/adapters/api-adapter.interface.ts#L49), [repo/services/api/src/main/java/com/campusfit/fitness/entity/Goal.java:96](repo/services/api/src/main/java/com/campusfit/fitness/entity/Goal.java#L96)
  - Study status mismatch: [repo/apps/web/src/services/adapters/api-adapter.interface.ts:101](repo/apps/web/src/services/adapters/api-adapter.interface.ts#L101), [repo/services/api/src/main/java/com/campusfit/study/entity/StudyPlan.java:65](repo/services/api/src/main/java/com/campusfit/study/entity/StudyPlan.java#L65)
  - Unused audit service: [repo/services/api/src/main/java/com/campusfit/shared/audit/AuditLogService.java:8](repo/services/api/src/main/java/com/campusfit/shared/audit/AuditLogService.java#L8)

### 4. Engineering Details and Professionalism

#### 4.1 Error handling/logging/validation/API design
- Conclusion: **Partial Pass**
- Rationale: Validation and global exception handling are present; logging exists and masks some values. However, test inconsistency and contract drift reduce professional reliability.
- Evidence:
  - Global error handling: [repo/services/api/src/main/java/com/campusfit/shared/exception/GlobalExceptionHandler.java:17](repo/services/api/src/main/java/com/campusfit/shared/exception/GlobalExceptionHandler.java#L17)
  - Logging levels: [repo/services/api/src/main/resources/application.yml:60](repo/services/api/src/main/resources/application.yml#L60)
  - Masking used in export logs: [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:99](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L99)

#### 4.2 Product-grade vs demo
- Conclusion: **Partial Pass**
- Rationale: Delivery is product-shaped, but core workflow bypass and high-impact data migration inconsistency are not demo-level minor issues.
- Evidence:
  - Product-level modules and routes: [repo/apps/web/README.md:20](repo/apps/web/README.md#L20), [repo/services/api/README.md:13](repo/services/api/README.md#L13)
  - Core workflow bypass evidence cited in Issues section.

### 5. Prompt Understanding and Requirement Fit

#### 5.1 Business-goal and constraint fit
- Conclusion: **Partial Pass**
- Rationale: The implementation generally understands the business scope; key constraints are partially weakened: putaway recommendation acceptance, cross-account migration semantics, and some contract consistency points.
- Evidence:
  - Prompt-fit modules: [docs/design.md:11](docs/design.md#L11)
  - Putaway bypass: [repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java:113](repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java#L113)
  - Cross-account export conflict with internal assumption: [docs/question.md:207](docs/question.md#L207), [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:191](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L191)

### 6. Aesthetics (frontend-only/full-stack)

#### 6.1 Visual/interaction quality
- Conclusion: **Cannot Confirm Statistically**
- Rationale: UI files and tests exist, but visual quality/interaction smoothness requires runtime rendering and manual interaction.
- Evidence:
  - UI route/page coverage: [repo/apps/web/src/app/router.ts:1](repo/apps/web/src/app/router.ts#L1)
  - UI test presence: [repo/apps/web/src/modules/dashboard/__tests__/DashboardPage.spec.ts](repo/apps/web/src/modules/dashboard/__tests__/DashboardPage.spec.ts)
- Manual verification note: Run manual browser QA for layout/spacing/feedback consistency.

## 5. Issues / Suggestions (Severity-Rated)

### Blocker

1. Severity: **Blocker**
- Title: Putaway completion can bypass putaway task generation/acceptance
- Conclusion: **Fail**
- Evidence:
  - Completion checks only pending tasks (empty list passes): [repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java:113](repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java#L113)
  - Task generation is optional API branch: [repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:117](repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java#L117), [repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:122](repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java#L122)
  - Frontend uses only task-fetch and task-complete paths; no generation call path before completion: [repo/apps/web/src/modules/operations/store.ts:117](repo/apps/web/src/modules/operations/store.ts#L117), [repo/apps/web/src/modules/operations/composables/useReceiptDetail.ts:14](repo/apps/web/src/modules/operations/composables/useReceiptDetail.ts#L14)
- Impact: A receipt can reach `COMPLETED` without confirmed putaway recommendations, violating a critical inbound business constraint.
- Minimum actionable fix:
  - Enforce `PUTAWAY -> COMPLETED` only if putaway tasks exist and all are completed.
  - Auto-generate putaway tasks on transition to `PUTAWAY` or require explicit generation step.
  - Add backend test to assert completion is blocked when no putaway tasks exist.

### High

2. Severity: **High**
- Title: Cross-account export/import masks and skips restoring profile identifiers
- Conclusion: **Fail**
- Evidence:
  - Export masks username/email/phone: [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:191](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L191)
  - Export rewrites userId values: [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:210](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L210)
  - Import skips masked profile fields: [repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java:165](repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java#L165), [repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java:175](repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java#L175)
  - Internal requirement note says raw profile in encrypted export is needed for migration: [docs/question.md:207](docs/question.md#L207)
- Impact: “Cross-account migration” can silently lose identity/profile metadata, causing incomplete migration outcomes.
- Minimum actionable fix:
  - Define a clear migration payload contract: keep sensitive profile fields unmasked inside password-encrypted export payload, while masking only logs/responses.
  - Add import validation/warnings for masked or incomplete payloads.

3. Severity: **High**
- Title: Backend test suite contains a static signature mismatch
- Conclusion: **Fail**
- Evidence:
  - Test calls 4-arg resolve: [repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java:156](repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java#L156)
  - Service requires 5 args: [repo/services/api/src/main/java/com/campusfit/inbound/service/DiscrepancyService.java:84](repo/services/api/src/main/java/com/campusfit/inbound/service/DiscrepancyService.java#L84)
- Impact: Static verifiability and confidence in discrepancy logic coverage are reduced; CI/test pass cannot be trusted until this mismatch is corrected.
- Minimum actionable fix:
  - Update test invocation to match service signature and include `receiptId` + `resolvedBy`.
  - Add a negative test for receipt/discrepancy mismatch path.

4. Severity: **High**
- Title: Frontend-backend status contract drift in core domain models
- Conclusion: **Fail**
- Evidence:
  - Frontend fitness status includes `PAUSED`: [repo/apps/web/src/services/adapters/api-adapter.interface.ts:49](repo/apps/web/src/services/adapters/api-adapter.interface.ts#L49)
  - Backend fitness status uses `RECALCULATED` instead: [repo/services/api/src/main/java/com/campusfit/fitness/entity/Goal.java:96](repo/services/api/src/main/java/com/campusfit/fitness/entity/Goal.java#L96)
  - Frontend study status includes `PAUSED`: [repo/apps/web/src/services/adapters/api-adapter.interface.ts:101](repo/apps/web/src/services/adapters/api-adapter.interface.ts#L101)
  - Backend study status uses `ARCHIVED`: [repo/services/api/src/main/java/com/campusfit/study/entity/StudyPlan.java:65](repo/services/api/src/main/java/com/campusfit/study/entity/StudyPlan.java#L65)
- Impact: UI logic/state rendering can break or silently mis-handle statuses; long-term maintainability degrades.
- Minimum actionable fix:
  - Establish one shared status contract and align FE/BE enums plus mapping tests.

### Medium

5. Severity: **Medium**
- Title: API auth contract inconsistency for sign-out endpoint
- Conclusion: **Partial Fail**
- Evidence:
  - Security allows all `/api/auth/**`: [repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:49](repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java#L49)
  - Sign-out proceeds even when token absent: [repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:44](repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java#L44), [repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:47](repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java#L47)
  - Spec states sign-out as authenticated flow context: [docs/api_spec.md:212](docs/api_spec.md#L212)
- Impact: Behavior/spec ambiguity complicates client integration and security expectations.
- Minimum actionable fix:
  - Decide and enforce one behavior (auth-required or idempotent public sign-out) and update code/spec/tests consistently.

6. Severity: **Medium**
- Title: No integration-layer backend tests for DB/migration/security-filter interactions
- Conclusion: **Partial Fail**
- Evidence:
  - Controller tests are `@WebMvcTest`: [repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java:40](repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java#L40)
  - Service tests are Mockito-only: [repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java:34](repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java#L34)
- Impact: Severe runtime defects (migration SQL incompatibility, encryption converter interactions, full security chain behavior) may remain undetected.
- Minimum actionable fix:
  - Add a minimal `@SpringBootTest` integration suite for auth/session, inbound state transitions, and export/import persistence paths.

7. Severity: **Medium**
- Title: Prompt performance constraint “partition by term and month” only partially evidenced
- Conclusion: **Partial Fail**
- Evidence:
  - Month-based partitions exist: [repo/services/api/src/main/resources/db/migration/V16__partition_study_tables.sql:8](repo/services/api/src/main/resources/db/migration/V16__partition_study_tables.sql#L8), [repo/services/api/src/main/resources/db/migration/V3__create_fitness_tables.sql:84](repo/services/api/src/main/resources/db/migration/V3__create_fitness_tables.sql#L84)
  - No term-based partition DDL found in reviewed migrations.
- Impact: Query scaling assumptions tied to term partitioning are not fully supported by static evidence.
- Minimum actionable fix:
  - Add explicit term-based partition/index strategy where required by prompt-level performance commitments.

### Low

8. Severity: **Low**
- Title: Generic audit service exists but is not integrated into domain flows
- Conclusion: **Partial Fail**
- Evidence:
  - Audit service defined: [repo/services/api/src/main/java/com/campusfit/shared/audit/AuditLogService.java:8](repo/services/api/src/main/java/com/campusfit/shared/audit/AuditLogService.java#L8)
  - No usage in other main Java files found during grep.
- Impact: Audit approach appears fragmented (domain-specific logs exist, but no shared audit baseline).
- Minimum actionable fix:
  - Either integrate `AuditLogService` in key mutation paths or remove it to avoid false architecture signals.

## 6. Security Review Summary

- Authentication entry points: **Pass**
  - Evidence: Session token filter + login service + bcrypt password service.
  - [repo/services/api/src/main/java/com/campusfit/shared/security/SessionAuthenticationFilter.java:48](repo/services/api/src/main/java/com/campusfit/shared/security/SessionAuthenticationFilter.java#L48), [repo/services/api/src/main/java/com/campusfit/auth/service/AuthService.java:74](repo/services/api/src/main/java/com/campusfit/auth/service/AuthService.java#L74), [repo/services/api/src/main/java/com/campusfit/auth/service/PasswordService.java:12](repo/services/api/src/main/java/com/campusfit/auth/service/PasswordService.java#L12)

- Route-level authorization: **Partial Pass**
  - Evidence: `/api/admin/**` and `/api/inbound/**` protected.
  - [repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:52](repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java#L52)
  - Note: `/api/auth/**` is fully permitAll including sign-out ([repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:49](repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java#L49)).

- Object-level authorization: **Partial Pass**
  - Evidence of ownership checks in study/fitness/notifications.
  - [repo/services/api/src/main/java/com/campusfit/study/service/StudyPlanService.java:85](repo/services/api/src/main/java/com/campusfit/study/service/StudyPlanService.java#L85), [repo/services/api/src/main/java/com/campusfit/fitness/service/GoalService.java:94](repo/services/api/src/main/java/com/campusfit/fitness/service/GoalService.java#L94), [repo/services/api/src/main/java/com/campusfit/notification/service/NotificationService.java:108](repo/services/api/src/main/java/com/campusfit/notification/service/NotificationService.java#L108)
  - Gap: inbound receipt access is role-gated but not owner/partition scoped (may be acceptable depending on business policy).

- Function-level authorization: **Pass**
  - Evidence: admin-only methods via `@PreAuthorize` for role assignment, notification creation/status, supervisor review, performance.
  - [repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:60](repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java#L60), [repo/services/api/src/main/java/com/campusfit/notification/controller/NotificationController.java:36](repo/services/api/src/main/java/com/campusfit/notification/controller/NotificationController.java#L36), [repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:127](repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java#L127), [repo/services/api/src/main/java/com/campusfit/reporting/controller/ReportingController.java:38](repo/services/api/src/main/java/com/campusfit/reporting/controller/ReportingController.java#L38)

- Tenant / user data isolation: **Partial Pass**
  - Evidence: per-user scoping for personal domains exists.
  - [repo/services/api/src/main/java/com/campusfit/study/service/ForgettingPointService.java:94](repo/services/api/src/main/java/com/campusfit/study/service/ForgettingPointService.java#L94)
  - Boundary: multi-tenant model is not present; treated as single-tenant local deployment.

- Admin / internal / debug protection: **Pass**
  - Evidence: admin route matcher and method-level checks.
  - [repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:52](repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java#L52)

## 7. Tests and Logging Review

- Unit tests: **Pass**
  - Evidence: substantial Mockito-based service tests and Vitest frontend tests.
  - [repo/services/api/src/test/java/com/campusfit/fitness/service/GoalServiceTest.java:27](repo/services/api/src/test/java/com/campusfit/fitness/service/GoalServiceTest.java#L27), [repo/apps/web/package.json:9](repo/apps/web/package.json#L9)

- API / integration tests: **Partial Pass**
  - Evidence: `@WebMvcTest` controller tests exist, but no integration-level DB/security chain tests in reviewed scope.
  - [repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java:38](repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java#L38)

- Logging categories / observability: **Partial Pass**
  - Evidence: app log levels configured; domain services emit operational logs.
  - [repo/services/api/src/main/resources/application.yml:60](repo/services/api/src/main/resources/application.yml#L60), [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:99](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L99)

- Sensitive-data leakage risk in logs / responses: **Partial Pass**
  - Evidence: masking utility used in export logs and payload masking; global exception handler uses generic 500 message.
  - [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:100](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L100), [repo/services/api/src/main/java/com/campusfit/shared/exception/GlobalExceptionHandler.java:87](repo/services/api/src/main/java/com/campusfit/shared/exception/GlobalExceptionHandler.java#L87)
  - Risk note: some business exceptions return raw messages by design.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview
- Unit tests exist:
  - Backend Mockito service tests and frontend Vitest tests.
  - Evidence: [repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java:34](repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java#L34), [repo/apps/web/vitest.config.ts:10](repo/apps/web/vitest.config.ts#L10), [repo/apps/web/src/modules/operations/__tests__/useWorkflow.spec.ts:1](repo/apps/web/src/modules/operations/__tests__/useWorkflow.spec.ts#L1)
- API/web-layer tests exist:
  - `@WebMvcTest` controller tests.
  - Evidence: [repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java:40](repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java#L40)
- Integration tests:
  - Cannot confirm meaningful integration coverage in reviewed scope.
- Test entry points documented:
  - Frontend `npm test`, backend `mvn test` documented.
  - Evidence: [repo/README.md:64](repo/README.md#L64), [repo/services/api/README.md:69](repo/services/api/README.md#L69)

### 8.2 Coverage Mapping Table

| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Auth lockout + failed attempts | [repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java:164](repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java#L164), [repo/services/api/src/test/java/com/campusfit/auth/service/LoginAttemptServiceTest.java](repo/services/api/src/test/java/com/campusfit/auth/service/LoginAttemptServiceTest.java) | wrong-password and locked-account paths mocked | basically covered | No real DB/session-chain integration | Add `@SpringBootTest` auth flow with real repositories and filter |
| Unauthenticated 401 on protected routes | [repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java:84](repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java#L84), [repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java:73](repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java#L73) | `SecurityContextHolder.clearContext()` then 401 assertions | sufficient | None significant at web layer | Keep regression tests |
| Unauthorized 403 on role-protected endpoints | [repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java:99](repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java#L99), [repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java:89](repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java#L89) | regular user forbidden on ops/admin endpoints | sufficient | Method-level + matcher interaction not integration-tested | Add integration authorization matrix test |
| Study object-level ownership checks | [repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java:122](repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java#L122) | service throws on foreign plan access | basically covered | Controller-level mocking does not prove repository scoping | Add service+repository integration test for foreign-user access |
| Inbound state machine transitions | [repo/services/api/src/test/java/com/campusfit/inbound/service/InboundStateMachineTest.java:61](repo/services/api/src/test/java/com/campusfit/inbound/service/InboundStateMachineTest.java#L61) | allowed/disallowed transitions asserted | basically covered | No test for mandatory putaway task existence before completion | Add failing test: `PUTAWAY -> COMPLETED` when no tasks should reject |
| Discrepancy threshold logic | [repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java:56](repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java#L56) | >2% and >5 unit scenarios | insufficient | One test currently has signature mismatch and may not compile | Fix signature and add receipt-id mismatch/duplicate resolve tests |
| Export password requirement/encryption path | [repo/services/api/src/test/java/com/campusfit/export_/service/ExportServiceTest.java:65](repo/services/api/src/test/java/com/campusfit/export_/service/ExportServiceTest.java#L65) | password required + encrypted file path | basically covered | No end-to-end import/export identity consistency test | Add integration test for export->import roundtrip with profile fields |
| Frontend workflow transition helper | [repo/apps/web/src/modules/operations/__tests__/useWorkflow.spec.ts:7](repo/apps/web/src/modules/operations/__tests__/useWorkflow.spec.ts#L7) | allowed transition matrix assertions | basically covered | Does not validate backend completion constraints | Add store-level test for putaway generation/complete path |
| 404 paths (not found) | [repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java:136](repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java#L136) | not-found assertion | basically covered | Not uniformly covered across all modules | Add representative 404 tests for inbound/export endpoints |

### 8.3 Security Coverage Audit
- Authentication: **Basically covered** (service + controller tests exist), but not fully integration-covered.
- Route authorization: **Basically covered** for 401/403 at web layer, no full integration matrix.
- Object-level authorization: **Insufficiently covered** for inbound domain; stronger in study/fitness.
- Tenant/data isolation: **Cannot Confirm Statistically** for multi-tenant scenarios (single-tenant assumptions).
- Admin/internal protection: **Basically covered** at web layer via `@WebMvcTest`, but no integration chain proof.

### 8.4 Final Coverage Judgment
- **Partial Pass**
- Boundary explanation:
  - Covered: many happy paths, core auth checks, state-machine unit paths, role-based 401/403 at controller level.
  - Not sufficiently covered: integration behavior (migrations/security filter chain/encryption persistence), inbound putaway enforcement edge case, and one static test-signature defect that can invalidate confidence in discrepancy coverage.

## 9. Final Notes
- Findings were limited to static, traceable evidence in the reviewed scope.
- Runtime success was not inferred.
- Highest priority for acceptance should be fixing putaway completion enforcement, export/import migration consistency, and test-signature correctness.
