# CampusFit Issue Fix Verification

Date: 2026-04-10
Method: static re-inspection only (no runtime execution)

## Summary
- Total issues re-checked: 4
- Fixed: 4
- Partially fixed: 0
- Not fixed: 0

## 1) Master-data deletion integrity gap for school/major/class plan references
- Previous status: High
- Current status: Fixed
- Verification:
  - `StudyPlanRepository` now includes lookup methods for plan references by school/major/class:
    - `repo/services/api/src/main/java/com/campusfit/study/repository/StudyPlanRepository.java:24`
    - `repo/services/api/src/main/java/com/campusfit/study/repository/StudyPlanRepository.java:26`
    - `repo/services/api/src/main/java/com/campusfit/study/repository/StudyPlanRepository.java:28`
  - `ReferentialIntegrityPolicy` now blocks school/major/class deletion when referenced by study plans:
    - `repo/services/api/src/main/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicy.java:31`
    - `repo/services/api/src/main/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicy.java:40`
    - `repo/services/api/src/main/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicy.java:49`
  - New tests cover these paths:
    - `repo/services/api/src/test/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicyTest.java:76`
    - `repo/services/api/src/test/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicyTest.java:89`
    - `repo/services/api/src/test/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicyTest.java:102`

## 2) Export/import profile migration contradiction (masked export vs raw import expectation)
- Previous status: High
- Current status: Fixed
- Verification:
  - Export service now stores raw profile data inside encrypted payload with explicit rationale:
    - `repo/services/api/src/main/java/com/campusfit/export_/service/ExportService.java:197`
  - Import service now explicitly expects raw profile values in encrypted payload and keeps masked-guard fallback:
    - `repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java:161`
    - `repo/services/api/src/main/java/com/campusfit/export_/service/AccountImportService.java:182`
  - Export unit test has been aligned to require raw profile values in decrypted payload:
    - `repo/services/api/src/test/java/com/campusfit/export_/service/ExportServiceTest.java:174`

## 3) Notification external channels not implemented beyond placeholder records
- Previous status: Medium
- Current status: Fixed (via hard-disable mitigation)
- Verification:
  - Delivery policy now hard-disables unsupported external channels and only enables `IN_APP` until adapters exist:
    - `repo/services/api/src/main/java/com/campusfit/notification/service/DeliveryChannelPolicy.java:45`
    - `repo/services/api/src/main/java/com/campusfit/notification/service/DeliveryChannelPolicy.java:50`
    - `repo/services/api/src/main/java/com/campusfit/notification/service/DeliveryChannelPolicy.java:51`
  - Startup warnings are emitted when EMAIL/SMS/WECOM flags are enabled without adapters:
    - `repo/services/api/src/main/java/com/campusfit/notification/service/DeliveryChannelPolicy.java:29`

- Follow-up note (resolved):
  - `DeliveryChannelPolicyTest` now expects only IN_APP regardless of flag state, matching the hard-disable policy:
    - `repo/services/api/src/test/java/com/campusfit/notification/policy/DeliveryChannelPolicyTest.java:48`

## 4) Sign-out API contract inconsistency (OpenAPI vs implementation/security rules)
- Previous status: Medium
- Current status: Fixed
- Verification:
  - OpenAPI now documents idempotent sign-out and 200 behavior without 401 requirement:
    - `repo/contracts/openapi.yaml:93`
    - `repo/contracts/openapi.yaml:98`
    - `repo/contracts/openapi.yaml:105`
  - Backend implementation remains idempotent and consistent with contract:
    - `repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:44`
    - `repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:47`
  - Security config still permits `/api/auth/**`, which now matches the contract:
    - `repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java:49`

## Final conclusion
- The previously reported High-severity issues are now resolved based on static evidence.
- All previously reported issues in this verification set are now resolved based on static evidence.
- One separate follow-up remains: align `DeliveryChannelPolicyTest` expectations with the updated hard-disable channel policy.
