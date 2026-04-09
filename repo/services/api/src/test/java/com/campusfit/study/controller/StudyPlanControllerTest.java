package com.campusfit.study.controller;

import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.exception.ResourceNotFoundException;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import com.campusfit.study.dto.StudyPlanRequest;
import com.campusfit.study.dto.StudyPlanResponse;
import com.campusfit.study.entity.StudyPlan;
import com.campusfit.study.service.StudyPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyPlanController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class StudyPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyPlanService studyPlanService;

    @MockBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    private void authenticateAs(Long userId, String username, Set<String> roles) {
        UserPrincipal principal = new UserPrincipal(userId, username, roles);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private StudyPlanResponse samplePlan(Long id, Long userId) {
        return StudyPlanResponse.builder()
                .id(id)
                .userId(userId)
                .title("CS101 Study Plan")
                .description("Weekly study schedule")
                .status(StudyPlan.PlanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void create_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        StudyPlanRequest request = StudyPlanRequest.builder()
                .title("Study Plan")
                .build();

        mockMvc.perform(post("/api/study/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/study/plans"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/study/plans/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void create_missingTitle_returns422() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        // title blank — @NotBlank should reject
        StudyPlanRequest request = StudyPlanRequest.builder()
                .description("No title plan")
                .build();

        mockMvc.perform(post("/api/study/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));

        SecurityContextHolder.clearContext();
    }

    // ── Ownership enforcement ────────────────────────────────────────────────

    @Test
    void getById_otherUsersplan_returns400() throws Exception {
        // The service enforces ownership and throws BusinessException
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(studyPlanService.getById(eq(99L), eq(1L)))
                .thenThrow(new BusinessException("Access denied: plan belongs to another user"));

        mockMvc.perform(get("/api/study/plans/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access denied: plan belongs to another user"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_planNotFound_returns404() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(studyPlanService.getById(eq(999L), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Study plan not found: 999"));

        mockMvc.perform(get("/api/study/plans/999"))
                .andExpect(status().isNotFound());

        SecurityContextHolder.clearContext();
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void create_authenticated_returns201() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        StudyPlanRequest request = StudyPlanRequest.builder()
                .title("CS101 Study Plan")
                .description("My weekly plan")
                .termId(5L)
                .build();

        when(studyPlanService.create(eq(1L), any(StudyPlanRequest.class)))
                .thenReturn(samplePlan(1L, 1L));

        mockMvc.perform(post("/api/study/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("CS101 Study Plan"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_authenticated_returnsOwnPlans() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(studyPlanService.getAllForUser(eq(1L), any()))
                .thenReturn(new PageImpl<>(
                        List.of(samplePlan(1L, 1L), samplePlan(2L, 1L)),
                        PageRequest.of(0, 25), 2));

        mockMvc.perform(get("/api/study/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].userId").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_ownPlan_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(studyPlanService.getById(1L, 1L)).thenReturn(samplePlan(1L, 1L));

        mockMvc.perform(get("/api/study/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("CS101 Study Plan"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void create_adminCanAlsoCreate_returns201() throws Exception {
        // Admin users are also able to create study plans (ADMIN has the /api/** access)
        authenticateAs(99L, "admin", Set.of("ADMIN"));

        StudyPlanRequest request = StudyPlanRequest.builder()
                .title("Admin Test Plan")
                .build();

        when(studyPlanService.create(eq(99L), any(StudyPlanRequest.class)))
                .thenReturn(samplePlan(10L, 99L));

        mockMvc.perform(post("/api/study/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(99));

        SecurityContextHolder.clearContext();
    }
}
