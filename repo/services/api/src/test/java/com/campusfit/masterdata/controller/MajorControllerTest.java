package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.service.MajorService;
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

@WebMvcTest(MajorController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class MajorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MajorService majorService;

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
                .code("MAJ-001")
                .name("Computer Science")
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
                .code("MAJ-001")
                .name("Computer Science")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/admin/majors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "regularuser", Set.of("REGULAR_USER"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("MAJ-001")
                .name("Computer Science")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/admin/majors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/admin/majors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "regularuser", Set.of("REGULAR_USER"));

        mockMvc.perform(get("/api/admin/majors"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "ops", Set.of("OPERATIONS_STAFF"));

        mockMvc.perform(delete("/api/admin/majors/1"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void create_missingEffectiveFrom_returns422() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("MAJ-001")
                .name("Computer Science")
                .build();

        mockMvc.perform(post("/api/admin/majors")
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
                .name("Computer Science")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        mockMvc.perform(post("/api/admin/majors")
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
                .code("MAJ-001")
                .name("Computer Science")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(majorService.create(any(MasterDataRequest.class), eq(1L)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/admin/majors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("MAJ-001"))
                .andExpect(jsonPath("$.data.name").value("Computer Science"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_admin_returns200WithPage() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(majorService.getAll(any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 25), 1));

        mockMvc.perform(get("/api/admin/majors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].code").value("MAJ-001"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(majorService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/admin/majors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(majorService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Major not found: 99"));

        mockMvc.perform(get("/api/admin/majors/99"))
                .andExpect(status().isNotFound());

        SecurityContextHolder.clearContext();
    }

    @Test
    void update_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("MAJ-001")
                .name("Updated Major")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        MasterDataResponse updated = sampleResponse();
        updated.setName("Updated Major");

        when(majorService.update(eq(1L), any(MasterDataRequest.class), eq(1L)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/admin/majors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Major"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void update_duplicateCode_returns400() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MasterDataRequest request = MasterDataRequest.builder()
                .code("MAJ-DUP")
                .name("Duplicate")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        when(majorService.update(eq(1L), any(MasterDataRequest.class), eq(1L)))
                .thenThrow(new BusinessException("Code already exists: MAJ-DUP"));

        mockMvc.perform(put("/api/admin/majors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Code already exists: MAJ-DUP"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        doNothing().when(majorService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/admin/majors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_referentialIntegrityViolation_returns400() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        doThrow(new BusinessException("Cannot delete: major is in use"))
                .when(majorService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/admin/majors/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete: major is in use"));

        SecurityContextHolder.clearContext();
    }
}
