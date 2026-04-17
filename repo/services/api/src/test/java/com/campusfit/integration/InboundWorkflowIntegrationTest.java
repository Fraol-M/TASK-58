package com.campusfit.integration;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.entity.Role;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.entity.UserRole;
import com.campusfit.auth.repository.RoleRepository;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test covering the inbound receipt workflow end-to-end against a real H2 database
 * and the full Spring Security filter chain.
 *
 * Exercises: create receipt → add line → DRAFT→RECEIVING→INSPECTION→PUTAWAY→COMPLETED,
 * role-based access control (regular user blocked, ops staff allowed), and the /post endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InboundWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;

    @BeforeEach
    void seedRoles() {
        seedRole("REGULAR_USER", "Regular User");
        seedRole("OPERATIONS_STAFF", "Operations Staff");
        seedRole("ADMIN", "Administrator");
    }

    // ---- RBAC guard ----

    @Test
    void inbound_regularUser_returns403() throws Exception {
        String token = signUpAndGetToken("iw_regular_u", "password123", "REGULAR_USER");

        mockMvc.perform(get("/api/inbound/receipts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---- Full happy-path workflow ----

    @Test
    void inbound_fullWorkflow_draftToCompleted() throws Exception {
        String token = signUpAndGetToken("iw_ops_u", "password123", "OPERATIONS_STAFF");

        // 1. Create receipt (DRAFT)
        MvcResult createResult = mockMvc.perform(post("/api/inbound/receipts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiptType", "PURCHASE",
                                "supplierName", "Integration Supplier"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        long receiptId = extractId(createResult, "/data/id");

        // 2. Add line item
        MvcResult addLineResult = mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/lines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemCode", "ITG-001",
                                "itemName", "Integration Item",
                                "expectedQty", 10))))
                .andExpect(status().isCreated())
                .andReturn();

        long lineId = extractId(addLineResult, "/data/id");

        // 3. DRAFT → RECEIVING
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "RECEIVING"))))
                .andExpect(status().isOk());

        // 4. Record received quantity
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/receive")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lineId", lineId,
                                "receivedQty", 10))))
                .andExpect(status().isOk());

        // 5. RECEIVING → INSPECTION
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "INSPECTION"))))
                .andExpect(status().isOk());

        // 6. Inspect line (PASS — no discrepancy)
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/inspection")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lineId", lineId,
                                "inspectedQty", 10,
                                "result", "PASS"))))
                .andExpect(status().isOk());

        // 7. INSPECTION → PUTAWAY (state machine auto-generates putaway tasks)
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "PUTAWAY"))))
                .andExpect(status().isOk());

        // 8. Retrieve and complete the auto-generated putaway task
        MvcResult tasksResult = mockMvc.perform(get("/api/inbound/receipts/" + receiptId + "/putaway")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        long taskId = extractId(tasksResult, "/data/0/id");

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/putaway")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "taskId", taskId,
                                "actualLocation", "ZONE-B-01"))))
                .andExpect(status().isOk());

        // 9. PUTAWAY → COMPLETED
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "COMPLETED"))))
                .andExpect(status().isOk());

        // 10. Verify final state persisted correctly
        mockMvc.perform(get("/api/inbound/receipts/" + receiptId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.lines[0].inspectionResult").value("PASS"));
    }

    @Test
    void inbound_postEndpoint_opsStaffHasAccess_notForbidden() throws Exception {
        String token = signUpAndGetToken("iw_ops_post_u", "password123", "OPERATIONS_STAFF");

        // Posting a non-existent receipt should give 4xx error (400/404),
        // but NOT 403 — confirming OPERATIONS_STAFF is authorized for /post.
        mockMvc.perform(post("/api/inbound/receipts/99999/post")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false));

        // A regular user hitting the same endpoint gets 403
        String regularToken = signUpAndGetToken("iw_regular_post_u", "password123", "REGULAR_USER");
        mockMvc.perform(post("/api/inbound/receipts/99999/post")
                        .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void inbound_invalidTransition_returns400() throws Exception {
        String token = signUpAndGetToken("iw_ops_invalid_u", "password123", "OPERATIONS_STAFF");

        MvcResult createResult = mockMvc.perform(post("/api/inbound/receipts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiptType", "PURCHASE",
                                "supplierName", "Test"))))
                .andExpect(status().isCreated())
                .andReturn();

        long receiptId = extractId(createResult, "/data/id");

        // DRAFT → INSPECTION is not a valid transition (must go DRAFT→RECEIVING first)
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "INSPECTION"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // ---- Discrepancies endpoint ----

    @Test
    void inbound_getDiscrepancies_afterFailInspection_returnsListOk() throws Exception {
        String token = signUpAndGetToken("iw_disc_admin", "password123", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/inbound/receipts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiptType", "PURCHASE", "supplierName", "Disc Supplier"))))
                .andExpect(status().isCreated())
                .andReturn();
        long receiptId = extractId(createResult, "/data/id");

        MvcResult lineResult = mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/lines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemCode", "DISC-001", "itemName", "Disc Item", "expectedQty", 5))))
                .andExpect(status().isCreated())
                .andReturn();
        long lineId = extractId(lineResult, "/data/id");

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "RECEIVING"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/receive")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("lineId", lineId, "receivedQty", 5))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "INSPECTION"))))
                .andExpect(status().isOk());

        // Inspect with FAIL to generate a discrepancy
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/inspection")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lineId", lineId, "inspectedQty", 3, "result", "FAIL",
                                "notes", "Damaged packaging"))))
                .andExpect(status().isOk());

        // GET discrepancies — list endpoint returns 200 with array
        mockMvc.perform(get("/api/inbound/receipts/" + receiptId + "/discrepancies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ---- Supervisor-review: separation-of-duty enforcement ----

    @Test
    void inbound_supervisorReview_sameUserAsCreator_returns400() throws Exception {
        String token = signUpAndGetToken("iw_sv_same_admin", "password123", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/inbound/receipts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiptType", "PURCHASE", "supplierName", "SV Same Supplier"))))
                .andExpect(status().isCreated())
                .andReturn();
        long receiptId = extractId(createResult, "/data/id");

        // Same admin tries to supervisor-review their own receipt → separation-of-duty violation
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/supervisor-review")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "discrepancyId", 99999,
                                "reasonCode", "COUNTED_WRONG",
                                "notes", "Test note"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Supervisor review requires a different user than the receipt creator"));
    }

    // ---- Unpost: admin can unpost a posted receipt ----

    @Test
    void inbound_unpost_adminCanUnpostPostedReceipt() throws Exception {
        String token = signUpAndGetToken("iw_unpost_admin", "password123", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/inbound/receipts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiptType", "PURCHASE", "supplierName", "Unpost Supplier"))))
                .andExpect(status().isCreated())
                .andReturn();
        long receiptId = extractId(createResult, "/data/id");

        MvcResult lineResult = mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/lines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "itemCode", "UP-001", "itemName", "Unpost Item", "expectedQty", 2))))
                .andExpect(status().isCreated())
                .andReturn();
        long lineId = extractId(lineResult, "/data/id");

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "RECEIVING"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/receive")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("lineId", lineId, "receivedQty", 2))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "INSPECTION"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/inspection")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "lineId", lineId, "inspectedQty", 2, "result", "PASS"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "PUTAWAY"))))
                .andExpect(status().isOk());

        MvcResult tasksResult = mockMvc.perform(get("/api/inbound/receipts/" + receiptId + "/putaway")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long taskId = extractId(tasksResult, "/data/0/id");

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/putaway")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "taskId", taskId, "actualLocation", "ZONE-A-01"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetState", "COMPLETED"))))
                .andExpect(status().isOk());

        // POST → POSTED
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/post")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // UNPOST → UNPOSTED (ADMIN only)
        mockMvc.perform(post("/api/inbound/receipts/" + receiptId + "/unpost")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/inbound/receipts/" + receiptId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNPOSTED"));
    }

    // ---- Helpers ----

    private void seedRole(String code, String name) {
        if (roleRepository.findByCode(code).isEmpty()) {
            roleRepository.save(Role.builder().code(code).name(name).description(name).build());
        }
    }

    private String signUpAndGetToken(String username, String password, String roleCode) throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        SignUpRequest.builder().username(username).password(password).build())));

        if (!"REGULAR_USER".equals(roleCode)) {
            User user = userRepository.findByUsername(username).orElseThrow();
            Role role = roleRepository.findByCode(roleCode).orElseThrow();
            userRoleRepository.save(UserRole.builder()
                    .userId(user.getId())
                    .roleId(role.getId())
                    .build());
        }

        MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequest.builder().username(username).password(password).build())))
                .andReturn();
        return extractToken(loginResult);
    }

    private String extractToken(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at("/data/token").asText();
    }

    private long extractId(MvcResult result, String jsonPath) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at(jsonPath).asLong();
    }
}
