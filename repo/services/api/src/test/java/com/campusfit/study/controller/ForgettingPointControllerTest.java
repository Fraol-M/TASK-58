package com.campusfit.study.controller;

import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import com.campusfit.study.dto.ForgettingPointRequest;
import com.campusfit.study.dto.ForgettingPointResponse;
import com.campusfit.study.dto.ReviewRequest;
import com.campusfit.study.service.ForgettingPointService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForgettingPointController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class ForgettingPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForgettingPointService forgettingPointService;

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

    private ForgettingPointResponse sampleResponse(Long id, Long planId) {
        return ForgettingPointResponse.builder()
                .id(id)
                .planId(planId)
                .topic("Binary Search")
                .description("Confusing off-by-one in loop bounds")
                .nextReviewDate(LocalDate.of(2026, 4, 17))
                .easeFactor(BigDecimal.valueOf(2.5))
                .intervalDays(1)
                .repetitions(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void create_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        ForgettingPointRequest request = ForgettingPointRequest.builder()
                .topic("Binary Search")
                .description("Off-by-one errors")
                .build();

        mockMvc.perform(post("/api/study/plans/1/forgetting-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void create_authenticated_returns201() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        ForgettingPointRequest request = ForgettingPointRequest.builder()
                .topic("Binary Search")
                .description("Confusing off-by-one in loop bounds")
                .build();

        when(forgettingPointService.create(eq(1L), eq(1L), any(ForgettingPointRequest.class)))
                .thenReturn(sampleResponse(1L, 1L));

        mockMvc.perform(post("/api/study/plans/1/forgetting-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.planId").value(1))
                .andExpect(jsonPath("$.data.topic").value("Binary Search"));
    }

    @Test
    void getByPlan_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(forgettingPointService.getByPlanId(eq(1L), eq(1L)))
                .thenReturn(List.of(sampleResponse(1L, 1L), sampleResponse(2L, 1L)));

        mockMvc.perform(get("/api/study/plans/1/forgetting-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].topic").value("Binary Search"));
    }

    @Test
    void review_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        ReviewRequest request = ReviewRequest.builder()
                .quality(4)
                .build();

        ForgettingPointResponse reviewed = sampleResponse(1L, 1L);
        reviewed.setRepetitions(1);
        reviewed.setIntervalDays(6);

        when(forgettingPointService.review(eq(1L), eq(1L), any(ReviewRequest.class)))
                .thenReturn(reviewed);

        mockMvc.perform(post("/api/study/forgetting-points/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.repetitions").value(1))
                .andExpect(jsonPath("$.data.intervalDays").value(6));
    }
}
