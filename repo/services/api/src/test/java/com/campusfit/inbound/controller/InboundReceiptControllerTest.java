package com.campusfit.inbound.controller;

import com.campusfit.inbound.dto.*;
import com.campusfit.inbound.entity.Discrepancy;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.service.*;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InboundReceiptController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class InboundReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InboundReceiptService receiptService;

    @MockBean
    private InboundStateMachine stateMachine;

    @MockBean
    private InboundLineService lineService;

    @MockBean
    private PutawayService putawayService;

    @MockBean
    private DiscrepancyService discrepancyService;

    @MockBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    private void authenticateAs(Long userId, String username, Set<String> roles) {
        UserPrincipal principal = new UserPrincipal(userId, username, roles);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private InboundReceiptResponse sampleReceipt(Long id, Long createdBy) {
        return InboundReceiptResponse.builder()
                .id(id)
                .receiptNumber("REC-00" + id)
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .status(InboundReceipt.ReceiptStatus.DRAFT)
                .supplierName("ACME Corp")
                .createdBy(createdBy)
                .lines(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication / authorization ──────────────────────────────────────

    @Test
    void create_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        InboundReceiptRequest request = InboundReceiptRequest.builder()
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .supplierName("ACME Corp")
                .build();

        mockMvc.perform(post("/api/inbound/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_regularUser_returns403() throws Exception {
        authenticateAs(2L, "user", Set.of("REGULAR_USER"));

        InboundReceiptRequest request = InboundReceiptRequest.builder()
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .build();

        mockMvc.perform(post("/api/inbound/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void list_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/inbound/receipts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_regularUser_returns403() throws Exception {
        authenticateAs(2L, "user", Set.of("REGULAR_USER"));

        mockMvc.perform(get("/api/inbound/receipts"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void create_missingReceiptType_returns422() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        // receiptType is null — @NotNull should reject
        InboundReceiptRequest request = InboundReceiptRequest.builder()
                .supplierName("ACME Corp")
                .build();

        mockMvc.perform(post("/api/inbound/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.receiptType").value("Receipt type is required"));

        SecurityContextHolder.clearContext();
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void create_opsStaff_returns201() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        InboundReceiptRequest request = InboundReceiptRequest.builder()
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .supplierName("ACME Corp")
                .build();

        when(receiptService.create(eq(1L), any(InboundReceiptRequest.class)))
                .thenReturn(sampleReceipt(1L, 1L));

        mockMvc.perform(post("/api/inbound/receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receiptNumber").value("REC-001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void list_opsStaff_returns200() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        when(receiptService.list(isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleReceipt(1L, 1L)), PageRequest.of(0, 25), 1));

        mockMvc.perform(get("/api/inbound/receipts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].receiptNumber").value("REC-001"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_opsStaff_returns200() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        when(receiptService.getById(1L)).thenReturn(sampleReceipt(1L, 1L));

        mockMvc.perform(get("/api/inbound/receipts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    void transition_opsStaff_returns200() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        TransitionRequest request = TransitionRequest.builder()
                .targetState(InboundReceipt.ReceiptStatus.RECEIVING)
                .reason("Starting receiving")
                .build();

        when(stateMachine.transition(eq(1L), eq(InboundReceipt.ReceiptStatus.RECEIVING), eq(1L), anyString()))
                .thenReturn(null);

        mockMvc.perform(post("/api/inbound/receipts/1/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    // ── Supervisor review (ADMIN only, separation of duty) ───────────────────

    @Test
    void supervisorReview_nonAdmin_returns403() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        SupervisorReviewRequest request = SupervisorReviewRequest.builder()
                .discrepancyId(10L)
                .reasonCode(Discrepancy.ReasonCode.OTHER)
                .notes("Approved")
                .build();

        mockMvc.perform(post("/api/inbound/receipts/1/supervisor-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void supervisorReview_admin_differentUser_returns200() throws Exception {
        // Admin reviewing a receipt created by user 2
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        SupervisorReviewRequest request = SupervisorReviewRequest.builder()
                .discrepancyId(10L)
                .reasonCode(Discrepancy.ReasonCode.OTHER)
                .notes("Approved after review")
                .build();

        // Receipt was created by user 2 — different from the reviewing admin (1)
        when(receiptService.getById(5L)).thenReturn(sampleReceipt(5L, 2L));
        when(discrepancyService.resolve(eq(5L), eq(10L), eq(1L),
                eq(Discrepancy.ReasonCode.OTHER), anyString())).thenReturn(null);

        mockMvc.perform(post("/api/inbound/receipts/5/supervisor-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void supervisorReview_admin_sameUserAsCreator_returns400() throws Exception {
        // Admin is also the receipt creator — separation of duty violation
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        SupervisorReviewRequest request = SupervisorReviewRequest.builder()
                .discrepancyId(10L)
                .reasonCode(Discrepancy.ReasonCode.OTHER)
                .notes("Self-review attempt")
                .build();

        // Receipt was created by the same admin (1) trying to review it
        when(receiptService.getById(5L)).thenReturn(sampleReceipt(5L, 1L));

        mockMvc.perform(post("/api/inbound/receipts/5/supervisor-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Supervisor review requires a different user than the receipt creator"));

        SecurityContextHolder.clearContext();
    }

    // ── Post / Unpost (inventory finalization) ────────────────────────────────

    @Test
    void post_operationsStaff_returns200() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));
        // stateMachine.transition() return value is not used by the /post endpoint

        mockMvc.perform(post("/api/inbound/receipts/5/post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void unpost_nonAdmin_returns403() throws Exception {
        authenticateAs(1L, "ops", Set.of("OPERATIONS_STAFF"));

        mockMvc.perform(post("/api/inbound/receipts/5/unpost"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void unpost_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));
        // stateMachine.transition() return value is not used by the /unpost endpoint

        mockMvc.perform(post("/api/inbound/receipts/5/unpost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }
}
