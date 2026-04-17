# Test Coverage Audit

## Project Type Detection
- Declared in repo/README.md: Project Type: fullstack.
- Inference fallback not needed.

## Backend Endpoint Inventory
total endpoints: 79
- DELETE /api/account (requestDeletion) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:106
- GET /api/admin/classes (getAll) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/ClassController.java:32
- POST /api/admin/classes (create) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/ClassController.java:26
- DELETE /api/admin/classes/{id} (delete) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/ClassController.java:52
- GET /api/admin/classes/{id} (getById) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/ClassController.java:40
- PUT /api/admin/classes/{id} (update) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/ClassController.java:45
- GET /api/admin/courses (getAll) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/CourseController.java:32
- POST /api/admin/courses (create) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/CourseController.java:26
- DELETE /api/admin/courses/{id} (delete) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/CourseController.java:52
- GET /api/admin/courses/{id} (getById) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/CourseController.java:40
- PUT /api/admin/courses/{id} (update) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/CourseController.java:45
- GET /api/admin/majors (getAll) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MajorController.java:32
- POST /api/admin/majors (create) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MajorController.java:26
- DELETE /api/admin/majors/{id} (delete) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MajorController.java:52
- GET /api/admin/majors/{id} (getById) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MajorController.java:40
- PUT /api/admin/majors/{id} (update) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MajorController.java:45
- GET /api/admin/master-data/export (export) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MasterDataExportController.java:39
- GET /api/admin/master-data/history (getHistory) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/ChangeHistoryController.java:21
- POST /api/admin/master-data/imports (importFile) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MasterDataImportController.java:22
- GET /api/admin/master-data/imports/{id} (getJob) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MasterDataImportController.java:31
- GET /api/admin/master-data/merge (getCandidates) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MasterDataMergeController.java:42
- POST /api/admin/master-data/merge (merge) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/MasterDataMergeController.java:50
- GET /api/admin/performance (getPerformance) - repo/services/api/src/main/java/com/campusfit/reporting/controller/ReportingController.java:39
- GET /api/admin/schools (getAll) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/SchoolController.java:32
- POST /api/admin/schools (create) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/SchoolController.java:26
- DELETE /api/admin/schools/{id} (delete) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/SchoolController.java:52
- GET /api/admin/schools/{id} (getById) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/SchoolController.java:40
- PUT /api/admin/schools/{id} (update) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/SchoolController.java:45
- GET /api/admin/terms (getAll) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/TermController.java:33
- POST /api/admin/terms (create) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/TermController.java:26
- DELETE /api/admin/terms/{id} (delete) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/TermController.java:53
- GET /api/admin/terms/{id} (getById) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/TermController.java:41
- PUT /api/admin/terms/{id} (update) - repo/services/api/src/main/java/com/campusfit/masterdata/controller/TermController.java:46
- POST /api/admin/users/{userId}/roles (assignRole) - repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:67
- POST /api/auth/sign-in (signIn) - repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:34
- POST /api/auth/sign-out (signOut) - repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:50
- POST /api/auth/sign-up (signUp) - repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:28
- GET /api/dashboard (getDashboard) - repo/services/api/src/main/java/com/campusfit/reporting/controller/ReportingController.java:31
- GET /api/exports (listExports) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:56
- GET /api/exports/{id} (getExport) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:63
- GET /api/exports/{id}/download (downloadExport) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:70
- POST /api/exports/account (requestExport) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:49
- GET /api/fitness/assessment (getLatest) - repo/services/api/src/main/java/com/campusfit/fitness/controller/AssessmentController.java:22
- PUT /api/fitness/assessment (createOrUpdate) - repo/services/api/src/main/java/com/campusfit/fitness/controller/AssessmentController.java:29
- GET /api/fitness/goals (getAll) - repo/services/api/src/main/java/com/campusfit/fitness/controller/GoalController.java:33
- POST /api/fitness/goals (create) - repo/services/api/src/main/java/com/campusfit/fitness/controller/GoalController.java:26
- GET /api/fitness/goals/{goalId}/check-ins (getAll) - repo/services/api/src/main/java/com/campusfit/fitness/controller/CheckInController.java:34
- POST /api/fitness/goals/{goalId}/check-ins (create) - repo/services/api/src/main/java/com/campusfit/fitness/controller/CheckInController.java:25
- GET /api/fitness/goals/{id} (getById) - repo/services/api/src/main/java/com/campusfit/fitness/controller/GoalController.java:42
- POST /api/imports/account (importAccount) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:83
- POST /api/imports/account/file (importAccountFromFile) - repo/services/api/src/main/java/com/campusfit/export_/controller/ExportController.java:90
- GET /api/inbound/receipts (list) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:50
- POST /api/inbound/receipts (create) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:42
- GET /api/inbound/receipts/{id} (getById) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:59
- GET /api/inbound/receipts/{id}/discrepancies (getDiscrepancies) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:65
- POST /api/inbound/receipts/{id}/inspection (inspect) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:97
- POST /api/inbound/receipts/{id}/lines (addLine) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:81
- POST /api/inbound/receipts/{id}/post (post) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:128
- GET /api/inbound/receipts/{id}/putaway (getPutawayTasks) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:107
- POST /api/inbound/receipts/{id}/putaway (putaway) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:113
- POST /api/inbound/receipts/{id}/receive (receiveLine) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:89
- POST /api/inbound/receipts/{id}/supervisor-review (supervisorReview) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:144
- POST /api/inbound/receipts/{id}/transition (transition) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:72
- POST /api/inbound/receipts/{id}/unpost (unpost) - repo/services/api/src/main/java/com/campusfit/inbound/controller/InboundReceiptController.java:136
- GET /api/me (getCurrentUser) - repo/services/api/src/main/java/com/campusfit/auth/controller/AuthController.java:59
- GET /api/notifications (getNotifications) - repo/services/api/src/main/java/com/campusfit/notification/controller/NotificationController.java:27
- POST /api/notifications (createNotification) - repo/services/api/src/main/java/com/campusfit/notification/controller/NotificationController.java:37
- POST /api/notifications/{id}/read (markAsRead) - repo/services/api/src/main/java/com/campusfit/notification/controller/NotificationController.java:45
- GET /api/notifications/{id}/status (getStatus) - repo/services/api/src/main/java/com/campusfit/notification/controller/NotificationController.java:53
- GET /api/study/export (export) - repo/services/api/src/main/java/com/campusfit/study/controller/StudyExportController.java:20
- POST /api/study/forgetting-points/{id}/review (review) - repo/services/api/src/main/java/com/campusfit/study/controller/ForgettingPointController.java:42
- POST /api/study/import (importData) - repo/services/api/src/main/java/com/campusfit/study/controller/StudyExportController.java:27
- GET /api/study/plans (getAll) - repo/services/api/src/main/java/com/campusfit/study/controller/StudyPlanController.java:33
- POST /api/study/plans (create) - repo/services/api/src/main/java/com/campusfit/study/controller/StudyPlanController.java:26
- GET /api/study/plans/{id} (getById) - repo/services/api/src/main/java/com/campusfit/study/controller/StudyPlanController.java:42
- GET /api/study/plans/{planId}/completions (getAll) - repo/services/api/src/main/java/com/campusfit/study/controller/DailyCompletionController.java:34
- POST /api/study/plans/{planId}/completions (record) - repo/services/api/src/main/java/com/campusfit/study/controller/DailyCompletionController.java:25
- GET /api/study/plans/{planId}/forgetting-points (getByPlan) - repo/services/api/src/main/java/com/campusfit/study/controller/ForgettingPointController.java:35
- POST /api/study/plans/{planId}/forgetting-points (create) - repo/services/api/src/main/java/com/campusfit/study/controller/ForgettingPointController.java:26

## API Test Mapping Table
| Endpoint | Covered | Test Type | Test Files | Evidence |
|---|---|---|---|---|
| DELETE /api/account | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::deleteAccount_withCorrectPassword_returns200 |
| GET /api/admin/classes | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/ClassControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::class_getByIdUpdateDeleteById |
| POST /api/admin/classes | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/ClassControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::class_getByIdUpdateDeleteById |
| DELETE /api/admin/classes/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/ClassControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::class_getByIdUpdateDeleteById |
| GET /api/admin/classes/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/ClassControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::class_getByIdUpdateDeleteById |
| PUT /api/admin/classes/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/ClassControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::class_getByIdUpdateDeleteById |
| GET /api/admin/courses | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/CourseControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::course_getByIdUpdateDeleteById |
| POST /api/admin/courses | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/CourseControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::course_getByIdUpdateDeleteById |
| DELETE /api/admin/courses/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/CourseControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::course_getByIdUpdateDeleteById |
| GET /api/admin/courses/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/CourseControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::course_getByIdUpdateDeleteById |
| PUT /api/admin/courses/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/CourseControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::course_getByIdUpdateDeleteById |
| GET /api/admin/majors | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MajorControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::major_getByIdUpdateDeleteById |
| POST /api/admin/majors | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MajorControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::major_getByIdUpdateDeleteById |
| DELETE /api/admin/majors/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MajorControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::major_getByIdUpdateDeleteById |
| GET /api/admin/majors/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MajorControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::major_getByIdUpdateDeleteById |
| PUT /api/admin/majors/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MajorControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::major_getByIdUpdateDeleteById |
| GET /api/admin/master-data/export | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::masterDataExport_adminCanAccess |
| GET /api/admin/master-data/history | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/ChangeHistoryControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::changeHistory_adminCanAccess |
| POST /api/admin/master-data/imports | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataImportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::masterDataImport_uploadFile_adminCanAccess |
| GET /api/admin/master-data/imports/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataImportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::masterDataImport_uploadFile_adminCanAccess |
| GET /api/admin/master-data/merge | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataMergeControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::masterDataMerge_post_adminCanMergeEntities |
| POST /api/admin/master-data/merge | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataMergeControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::masterDataMerge_post_adminCanMergeEntities |
| GET /api/admin/performance | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ReportingIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/reporting/controller/ReportingControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ReportingIntegrationTest.java::performance_adminUser_returnsOk |
| GET /api/admin/schools | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/SchoolControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::school_createListGetUpdateDelete |
| POST /api/admin/schools | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/SchoolControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::school_createListGetUpdateDelete |
| DELETE /api/admin/schools/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/SchoolControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::school_createListGetUpdateDelete |
| GET /api/admin/schools/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/SchoolControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::school_createListGetUpdateDelete |
| PUT /api/admin/schools/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/SchoolControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::school_createListGetUpdateDelete |
| GET /api/admin/terms | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/TermControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::term_createListGetUpdateDelete |
| POST /api/admin/terms | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/TermControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::term_createListGetUpdateDelete |
| DELETE /api/admin/terms/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/TermControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::term_createListGetUpdateDelete |
| GET /api/admin/terms/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/TermControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::term_createListGetUpdateDelete |
| PUT /api/admin/terms/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/masterdata/controller/TermControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java::term_createListGetUpdateDelete |
| POST /api/admin/users/{userId}/roles | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java::assignRole_adminUser_canAssignRoleToTargetUser |
| POST /api/auth/sign-in | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java::signUp_signIn_getMe_fullFlow |
| POST /api/auth/sign-out | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java::signUp_signIn_getMe_fullFlow |
| POST /api/auth/sign-up | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java::signUp_signIn_getMe_fullFlow |
| GET /api/dashboard | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ReportingIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/reporting/controller/ReportingControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ReportingIntegrationTest.java::dashboard_regularUser_returnsOk |
| GET /api/exports | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::export_withPassword_completesAndIsListable |
| GET /api/exports/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::export_withPassword_completesAndIsListable |
| GET /api/exports/{id}/download | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::exportDownload_completedExport_returnsOctetStream |
| POST /api/exports/account | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::export_withPassword_completesAndIsListable |
| GET /api/fitness/assessment | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/AssessmentControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::assessment_upsertThenGet_persistsCorrectly |
| PUT /api/fitness/assessment | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/AssessmentControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::assessment_upsertThenGet_persistsCorrectly |
| GET /api/fitness/goals | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/GoalControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::goal_createListGetById |
| POST /api/fitness/goals | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/GoalControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::goal_createListGetById |
| GET /api/fitness/goals/{goalId}/check-ins | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/CheckInControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::checkIn_createAndList |
| POST /api/fitness/goals/{goalId}/check-ins | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/CheckInControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::checkIn_createAndList |
| GET /api/fitness/goals/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/fitness/controller/GoalControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java::goal_createListGetById |
| POST /api/imports/account | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::importAccount_json_endpointAccessible |
| POST /api/imports/account/file | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java::importAccountFromFile_invalidBytes_returns4xx |
| GET /api/inbound/receipts | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| GET /api/inbound/receipts/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| GET /api/inbound/receipts/{id}/discrepancies | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/inspection | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/lines | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/post | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| GET /api/inbound/receipts/{id}/putaway | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/putaway | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/receive | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/supervisor-review | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/transition | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| POST /api/inbound/receipts/{id}/unpost | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java::inbound_fullWorkflow_draftToCompleted |
| GET /api/me | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java::signUp_signIn_getMe_fullFlow |
| GET /api/notifications | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java::notification_adminCreates_userReceivesMarkReadAndStatusCheck |
| POST /api/notifications | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java::notification_adminCreates_userReceivesMarkReadAndStatusCheck |
| POST /api/notifications/{id}/read | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java::notification_adminCreates_userReceivesMarkReadAndStatusCheck |
| GET /api/notifications/{id}/status | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java::notification_adminCreates_userReceivesMarkReadAndStatusCheck |
| GET /api/study/export | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/StudyExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::studyExport_thenImport_roundTrip |
| POST /api/study/forgetting-points/{id}/review | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/ForgettingPointControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::forgettingPoint_createListAndReview_sm2Flow |
| POST /api/study/import | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/StudyExportControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::studyExport_thenImport_roundTrip |
| GET /api/study/plans | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::studyPlan_createListGetById_fullFlow |
| POST /api/study/plans | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::studyPlan_createListGetById_fullFlow |
| GET /api/study/plans/{id} | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::studyPlan_createListGetById_fullFlow |
| GET /api/study/plans/{planId}/completions | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/DailyCompletionControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::dailyCompletion_createAndList |
| POST /api/study/plans/{planId}/completions | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/DailyCompletionControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::dailyCompletion_createAndList |
| GET /api/study/plans/{planId}/forgetting-points | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/ForgettingPointControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::forgettingPoint_createListAndReview_sm2Flow |
| POST /api/study/plans/{planId}/forgetting-points | yes | true no-mock HTTP | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java; repo/services/api/src/test/java/com/campusfit/study/controller/ForgettingPointControllerTest.java | repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java::forgettingPoint_createListAndReview_sm2Flow |

## API Test Classification
1. True No-Mock HTTP (8 files)
- repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/ReportingIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java
2. HTTP with Mocking (21 files)
- repo/services/api/src/test/java/com/campusfit/auth/controller/AuthControllerTest.java
- repo/services/api/src/test/java/com/campusfit/export_/controller/ExportControllerTest.java
- repo/services/api/src/test/java/com/campusfit/fitness/controller/AssessmentControllerTest.java
- repo/services/api/src/test/java/com/campusfit/fitness/controller/CheckInControllerTest.java
- repo/services/api/src/test/java/com/campusfit/fitness/controller/GoalControllerTest.java
- repo/services/api/src/test/java/com/campusfit/inbound/controller/InboundReceiptControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/ChangeHistoryControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/ClassControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/CourseControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/MajorControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataExportControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataImportControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/MasterDataMergeControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/SchoolControllerTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/controller/TermControllerTest.java
- repo/services/api/src/test/java/com/campusfit/notification/controller/NotificationControllerTest.java
- repo/services/api/src/test/java/com/campusfit/reporting/controller/ReportingControllerTest.java
- repo/services/api/src/test/java/com/campusfit/study/controller/DailyCompletionControllerTest.java
- repo/services/api/src/test/java/com/campusfit/study/controller/ForgettingPointControllerTest.java
- repo/services/api/src/test/java/com/campusfit/study/controller/StudyExportControllerTest.java
- repo/services/api/src/test/java/com/campusfit/study/controller/StudyPlanControllerTest.java
3. Non-HTTP (26 files)
- repo/services/api/src/test/java/com/campusfit/auth/service/AuthServiceTest.java
- repo/services/api/src/test/java/com/campusfit/auth/service/LoginAttemptServiceTest.java
- repo/services/api/src/test/java/com/campusfit/export_/service/AccountDeletionServiceTest.java
- repo/services/api/src/test/java/com/campusfit/export_/service/AccountImportServiceTest.java
- repo/services/api/src/test/java/com/campusfit/export_/service/ExportServiceTest.java
- repo/services/api/src/test/java/com/campusfit/fitness/policy/GoalRecalculationPolicyTest.java
- repo/services/api/src/test/java/com/campusfit/fitness/service/CheckInServiceTest.java
- repo/services/api/src/test/java/com/campusfit/fitness/service/GoalServiceTest.java
- repo/services/api/src/test/java/com/campusfit/inbound/service/DiscrepancyServiceTest.java
- repo/services/api/src/test/java/com/campusfit/inbound/service/InboundReceiptServiceTest.java
- repo/services/api/src/test/java/com/campusfit/inbound/service/InboundStateMachineTest.java
- repo/services/api/src/test/java/com/campusfit/integration/AuthIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/ExportLifecycleIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/FitnessWorkflowIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/InboundWorkflowIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/MasterDataIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/NotificationIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/ReportingIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/integration/StudyWorkflowIntegrationTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/policy/ReferentialIntegrityPolicyTest.java
- repo/services/api/src/test/java/com/campusfit/masterdata/service/DuplicateMergeServiceTest.java
- repo/services/api/src/test/java/com/campusfit/notification/policy/DeliveryChannelPolicyTest.java
- repo/services/api/src/test/java/com/campusfit/notification/service/NotificationServiceTest.java
- repo/services/api/src/test/java/com/campusfit/study/service/DailyCompletionServiceTest.java
- repo/services/api/src/test/java/com/campusfit/study/service/ForgettingPointServiceTest.java
- repo/services/api/src/test/java/com/campusfit/study/service/StudyPlanServiceTest.java

## Mock Detection
- Backend HTTP mocking via @MockBean in all WebMvc controller tests.
- Backend unit mocking via @Mock and Mockito stubbing in service/policy tests.
- Frontend mocking via vi.mock in many spec files.

## Coverage Summary
- Total endpoints: 79
- Endpoints with HTTP tests: 79
- Endpoints with TRUE no-mock HTTP tests: 79
- HTTP coverage: 100.00%
- True API coverage: 100.00%

## Unit Test Summary
### Backend Unit Tests
- Unit tests exist for controllers, services, and domain policies.
- Important backend modules NOT directly unit-tested:
  - repo/services/api/src/main/java/com/campusfit/shared/security/SessionAuthenticationFilter.java
  - repo/services/api/src/main/java/com/campusfit/shared/config/SecurityConfig.java
  - repo/services/api/src/main/java/com/campusfit/shared/exception/GlobalExceptionHandler.java
  - repo/services/api/src/main/java/com/campusfit/shared/encryption/*

### Frontend Unit Tests (STRICT REQUIREMENT)
- Frontend test files:
- repo/apps/web/src/components/__tests__/SubmitButton.spec.ts
- repo/apps/web/src/components/__tests__/PermissionGuard.spec.ts
- repo/apps/web/src/app/__tests__/guards.spec.ts
- repo/apps/web/src/modules/study/__tests__/ReviewPage.spec.ts
- repo/apps/web/src/modules/study/__tests__/PlansPage.spec.ts
- repo/apps/web/src/modules/study/__tests__/HistoryPage.spec.ts
- repo/apps/web/src/modules/exports/__tests__/ExportsPage.spec.ts
- repo/apps/web/src/modules/auth/__tests__/store.spec.ts
- repo/apps/web/src/modules/auth/__tests__/SignUpPage.spec.ts
- repo/apps/web/src/modules/auth/__tests__/SignInPage.spec.ts
- repo/apps/web/src/modules/dashboard/__tests__/DashboardPage.spec.ts
- repo/apps/web/src/modules/admin/__tests__/PerformancePage.spec.ts
- repo/apps/web/src/modules/notifications/__tests__/NotificationsPage.spec.ts
- repo/apps/web/src/modules/profile/__tests__/ProfilePage.spec.ts
- repo/apps/web/src/modules/operations/__tests__/useWorkflow.spec.ts
- repo/apps/web/src/modules/operations/__tests__/ReceivingListPage.spec.ts
- repo/apps/web/src/modules/operations/__tests__/ReceivingDetailPage.spec.ts
- repo/apps/web/src/modules/operations/__tests__/DiscrepanciesPage.spec.ts
- repo/apps/web/src/modules/fitness/__tests__/GoalsPage.spec.ts
- repo/apps/web/src/modules/master-data/__tests__/MergePage.spec.ts
- repo/apps/web/src/modules/fitness/__tests__/CheckInsPage.spec.ts
- repo/apps/web/src/modules/fitness/__tests__/AssessmentPage.spec.ts
- repo/apps/web/src/modules/master-data/__tests__/MasterDataPage.spec.ts
- repo/apps/web/src/modules/master-data/__tests__/ImportPage.spec.ts
- repo/apps/web/src/modules/master-data/__tests__/EntityForm.spec.ts
- Frameworks/tools detected: vitest, @vue/test-utils, happy-dom.
- Components/modules covered: auth, dashboard, study, fitness, notifications, exports, operations, master-data, shared components/guards.
- Important frontend modules NOT tested directly:
  - repo/apps/web/src/services/adapters/http-adapter.ts
  - repo/apps/web/src/services/http-client.ts
  - repo/apps/web/src/modules/*/api.ts (mostly mocked)
  - repo/apps/web/src/app/router.ts
- Frontend unit tests: PRESENT

### Cross-Layer Observation
- Backend and frontend are both tested, but frontend tests heavily mock adapters/composables.

## API Observability Check
- Strong in integration tests: method/path/input/response assertions are usually explicit.
- Weak in some authorization-only tests where only status is asserted.

## Tests Check
- Success, failure, validation, and auth paths are broadly covered.
- run_tests.sh is Docker-based (passes Docker expectation), but still downloads dependencies inside containers.

## End-to-End Expectations
- Fullstack e2e tests exist in repo/apps/e2e/tests.
- Depth is moderate: mainly UI/navigation/auth flow assertions, fewer deep data round-trip assertions.

## Test Coverage Score (0-100)
- Score: 90

## Score Rationale
- Full endpoint coverage and broad true integration coverage are strong.
- Heavy mocking and moderate e2e depth reduce confidence for some boundary behaviors.

## Key Gaps
- Missing direct tests for key security/config/exception infrastructure classes.
- Frontend data-layer modules are mostly mocked instead of directly tested.

## Confidence & Assumptions
- Confidence: High (static evidence only).
- Assumptions: routes are defined by inspected controllers; no hidden runtime route registration.

## Final Test Coverage Verdict
- PASS (coverage strong; quality gaps remain).

---

# README Audit

## README Location
- repo/README.md is present.

## Hard Gate Evaluation
- Formatting/readability: PASS with minor encoding artifact in tree characters.
- Startup instructions for fullstack/backend: PASS (docker-compose up --build present).
- Access method: PASS (URL and ports are documented).
- Verification method: PASS (curl/backend + frontend HTTP check).
- Environment rules: PASS in README instructions (Docker-centered; no local npm/pip/apt/manual DB setup commands).
- Demo credentials with roles: PASS.

## Engineering Quality
- Tech stack and security/roles are clear.
- Architecture and testing explanation can be deeper.

## High Priority Issues
- None.

## Medium Priority Issues
- Markdown encoding artifacts in README tree block.
- README does not state containerized dependency download prerequisites for test execution.

## Low Priority Issues
- Verification section lacks explicit example response payloads.
- Architecture section could better describe FE-BE module boundaries.

## Hard Gate Failures
- None.

## README Verdict
- PASS

---

## Combined Final Verdicts
- Test Coverage Audit: PASS
- README Audit: PASS
