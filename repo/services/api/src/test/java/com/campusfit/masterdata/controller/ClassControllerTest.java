package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.service.ClassService;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class ClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClassService classService;

    @MockBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @BeforeEach
    void setUpFilter() throws Exception {
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(sessionAuthenticationFilter).doFilter(any(), any(), any());
    }

    private void authenticateAs(Long userId, String username, Set<String> roles) {
        UserPrincipal principal = new UserPrincipal(userId, username, roles);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private MasterDataResponse sampleResponse() {
        return MasterDataResponse.builder()
                .id(1L)
                .code("CLS-001")
                .name("Introduction to CS")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication / authorization ──────────────────────────────────────

    @Test
    void create_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        MasterDataRequest request = MasterDataRequest.builder()
                .code("CLS-001")
                .name("Introduction to CS")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/admin/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "regularuser", Set.of("REGULAR_USER"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("CLS-001")
                .name("Introduction to CS")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/admin/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/admin/classes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "regularuser", Set.of("REGULAR_USER"));

        mockMvc.perform(get("/api/admin/classes"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "ops", Set.of("OPERATIONS_STAFF"));

        mockMvc.perform(delete("/api/admin/classes/1"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void create_missingEffectiveFrom_returns422() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("CLS-001")
                .name("Introduction to CS")
                .build();

        mockMvc.perform(post("/api/admin/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.effectiveFrom").value("Effective from date is required"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void create_missingCode_returns422() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .name("Introduction to CS")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/admin/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.code").exists());

        SecurityContextHolder.clearContext();
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void create_admin_returns201() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("CLS-001")
                .name("Introduction to CS")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(classService.create(any(MasterDataRequest.class), eq(1L)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/admin/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("CLS-001"))
                .andExpect(jsonPath("$.data.name").value("Introduction to CS"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_admin_returns200WithPage() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(classService.getAll(any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 25), 1));

        mockMvc.perform(get("/api/admin/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].code").value("CLS-001"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(classService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/admin/classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(classService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Class not found: 99"));

        mockMvc.perform(get("/api/admin/classes/99"))
                .andExpect(status().isNotFound());

        SecurityContextHolder.clearContext();
    }

    @Test
    void update_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("CLS-001")
                .name("Updated Class")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        MasterDataResponse updated = sampleResponse();
        updated.setName("Updated Class");

        when(classService.update(eq(1L), any(MasterDataRequest.class), eq(1L)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/admin/classes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Class"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void update_duplicateCode_returns400() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("CLS-DUP")
                .name("Duplicate")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(classService.update(eq(1L), any(MasterDataRequest.class), eq(1L)))
                .thenThrow(new BusinessException("Code already exists: CLS-DUP"));

        mockMvc.perform(put("/api/admin/classes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Code already exists: CLS-DUP"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        doNothing().when(classService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/admin/classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_referentialIntegrityViolation_returns400() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        doThrow(new BusinessException("Cannot delete: class is in use"))
                .when(classService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/admin/classes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete: class is in use"));

        SecurityContextHolder.clearContext();
    }
}
