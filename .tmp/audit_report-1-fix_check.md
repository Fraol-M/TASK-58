# CampusFit Prior-Issue Recheck (Static) — 2026-04-09 (Round 2)

## Scope / Method
- Static-only recheck against previously reported issues.
- No project startup, no Docker, no test execution.
- Status values used: Fixed / Partially Fixed / Not Fixed.

## Summary
- Fixed: 8
- Partially Fixed: 0
- Not Fixed: 0

## Issue-by-Issue Verification

### 1) Putaway completion bypass (Blocker)
- Current status: **Fixed**
- Evidence:
  - Putaway completion is blocked when no tasks exist and when tasks remain incomplete: [repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java:141](repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java#L141)
  - Putaway tasks are auto-generated on transition to `PUTAWAY`: [repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java:62](repo/services/api/src/main/java/com/campusfit/inbound/service/InboundStateMachine.java#L62)

### 2) Cross-account export/import masking profile identifiers (High)
- Current status: **Fixed**
- Evidence:
  - Export includes raw profile identity values in encrypted payload: [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:200](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L200)
  - Import sets raw values and skips only masked legacy strings: [repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java:165](repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java#L165)

### 3) Backend test signature mismatch in discrepancy service test (High)
- Current status: **Fixed**
- Evidence:
  - Service signature remains 5-arg: [repo/services/api/src/main/java/com/campusfit/inbound/service/DiscrepancyService.java:84](repo/services/api/src/main/java/com/campusfit/inbound/service/DiscrepancyService.java#L84)
  - Test uses matching 5-arg call: [repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java:157](repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java#L157)

### 4) Frontend-backend status contract drift (High)
- Current status: **Fixed**
- Evidence:
  - FE fitness status includes `RECALCULATED` matching BE: [repo/apps/web/src/services/adapters/api-adapter.interface.ts:49](repo/apps/web/src/services/adapters/api-adapter.interface.ts#L49), [repo/services/api/src/main/java/com/campusfit/fitness/entity/Goal.java:96](repo/services/api/src/main/java/com/campusfit/fitness/entity/Goal.java#L96)
  - FE study status includes `ARCHIVED` matching BE: [repo/apps/web/src/services/adapters/api-adapter.interface.ts:101](repo/apps/web/src/services/adapters/api-adapter.interface.ts#L101), [repo/services/api/src/main/java/com/campusfit/study/entity/StudyPlan.java:65](repo/services/api/src/main/java/com/campusfit/study/entity/StudyPlan.java#L65)

### 5) Sign-out auth contract inconsistency (Medium)
- Current status: **Fixed**
- Evidence:
  - Runtime behavior is public/idempotent (permit-all and token optional): [repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:49](repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java#L49), [repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:44](repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java#L44)
  - API spec is now internally consistent with idempotent public sign-out semantics:
    - Auth section exempts sign-out from authentication: [docs/api_spec.md:212](docs/api_spec.md#L212)
    - Sign-out endpoint table is now `Auth | None (public, idempotent)`: [docs/api_spec.md:244](docs/api_spec.md#L244)

### 6) Missing backend integration-layer tests (Medium)
- Current status: **Fixed**
- Evidence:
  - Integration suite now present with `@SpringBootTest` + `@AutoConfigureMockMvc`: [repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java:31](repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java#L31), [repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java:32](repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java#L32)

### 7) “Partition by term and month” only partially evidenced (Medium)
- Current status: **Fixed**
- Evidence:
  - Month partitions still exist: [repo/services/api/src/main/resources/db/migration/V16__partition_study_tables.sql:8](repo/services/api/src/main/resources/db/migration/V16__partition_study_tables.sql#L8)
  - New term-based RANGE partition migration added: [repo/services/api/src/main/resources/db/migration/V19__partition_study_plan_by_term.sql:55](repo/services/api/src/main/resources/db/migration/V19__partition_study_plan_by_term.sql#L55)

### 8) Unused generic AuditLogService (Low)
- Current status: **Fixed**
- Evidence:
  - Audit service injected/used in export completion: [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:58](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L58), [repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:114](repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java#L114)
  - Audit service injected/used in deletion flow: [repo/services/api/src/main/java/com/campusfit/export_/service/AccountDeletionService.java:33](repo/services/api/src/main/java/com/campusfit/export_/service/AccountDeletionService.java#L33), [repo/services/api/src/main/java/com/campusfit/export_/service/AccountDeletionService.java:61](repo/services/api/src/main/java/com/campusfit/export_/service/AccountDeletionService.java#L61)

## Final Recheck Verdict
- **All previously listed items are now fixed.**
- 8 of 8 prior issues are fixed.
