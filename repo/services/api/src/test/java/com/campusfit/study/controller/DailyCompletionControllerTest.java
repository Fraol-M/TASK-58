package com.campusfit.study.controller;

import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import com.campusfit.study.dto.DailyCompletionRequest;
import com.campusfit.study.dto.DailyCompletionResponse;
import com.campusfit.study.service.DailyCompletionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DailyCompletionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class DailyCompletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DailyCompletionService dailyCompletionService;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, String username, Set<String> roles) {
        UserPrincipal principal = new UserPrincipal(userId, username, roles);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private DailyCompletionResponse sampleResponse(Long id, Long planId) {
        return DailyCompletionResponse.builder()
                .id(id)
                .planId(planId)
                .itemId(10L)
                .completedDate(LocalDate.of(2026, 4, 16))
                .completed(true)
                .notes("Completed chapter 1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void record_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        DailyCompletionRequest request = DailyCompletionRequest.builder()
                .itemId(10L)
                .completedDate(LocalDate.of(2026, 4, 16))
                .completed(true)
                .notes("Done")
                .build();

        mockMvc.perform(post("/api/study/plans/1/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void record_authenticated_returns201() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        DailyCompletionRequest request = DailyCompletionRequest.builder()
                .itemId(10L)
                .completedDate(LocalDate.of(2026, 4, 16))
                .completed(true)
                .notes("Completed chapter 1")
                .build();

        when(dailyCompletionService.record(eq(1L), eq(1L), any(DailyCompletionRequest.class)))
                .thenReturn(sampleResponse(1L, 1L));

        mockMvc.perform(post("/api/study/plans/1/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.planId").value(1))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    void getByPlan_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(dailyCompletionService.getByPlanId(eq(1L), eq(1L)))
                .thenReturn(List.of(sampleResponse(1L, 1L), sampleResponse(2L, 1L)));

        mockMvc.perform(get("/api/study/plans/1/completions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].planId").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }
}
