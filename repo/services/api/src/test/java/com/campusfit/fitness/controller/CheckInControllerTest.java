package com.campusfit.fitness.controller;

import com.campusfit.fitness.dto.CheckInRequest;
import com.campusfit.fitness.dto.CheckInResponse;
import com.campusfit.fitness.service.CheckInService;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.GlobalExceptionHandler;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckInController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckInService checkInService;

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

    private CheckInResponse sampleCheckIn(Long id, Long goalId, Long userId) {
        return CheckInResponse.builder()
                .id(id)
                .goalId(goalId)
                .userId(userId)
                .weekNumber(1)
                .value(BigDecimal.valueOf(165))
                .notes("Feeling good")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void create_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        CheckInRequest request = CheckInRequest.builder()
                .value(BigDecimal.valueOf(165))
                .notes("Weekly check-in")
                .build();

        mockMvc.perform(post("/api/fitness/goals/1/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void create_authenticated_returns201() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        CheckInRequest request = CheckInRequest.builder()
                .value(BigDecimal.valueOf(165))
                .notes("Weekly check-in")
                .build();

        when(checkInService.create(eq(1L), eq(1L), any(CheckInRequest.class)))
                .thenReturn(sampleCheckIn(1L, 1L, 1L));

        mockMvc.perform(post("/api/fitness/goals/1/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.goalId").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.weekNumber").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getByGoal_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(checkInService.getByGoalId(eq(1L), eq(1L)))
                .thenReturn(List.of(
                        sampleCheckIn(1L, 1L, 1L),
                        sampleCheckIn(2L, 1L, 1L)));

        mockMvc.perform(get("/api/fitness/goals/1/check-ins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].goalId").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1));

        SecurityContextHolder.clearContext();
    }
}
