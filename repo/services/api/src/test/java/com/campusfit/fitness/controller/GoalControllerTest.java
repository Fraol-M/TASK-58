package com.campusfit.fitness.controller;

import com.campusfit.fitness.dto.GoalRequest;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.service.GoalService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoalService goalService;

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

    private GoalResponse sampleGoal(Long id, Long userId) {
        return GoalResponse.builder()
                .id(id)
                .userId(userId)
                .assessmentId(1L)
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .description("Lose 10 lbs")
                .targetValue(BigDecimal.valueOf(160))
                .startValue(BigDecimal.valueOf(170))
                .currentValue(BigDecimal.valueOf(165))
                .unit("lbs")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .status(Goal.GoalStatus.ACTIVE)
                .missedCheckIns(0)
                .progressPercentage(50.0)
                .milestones(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void create_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        GoalRequest request = GoalRequest.builder()
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .targetValue(BigDecimal.valueOf(160))
                .unit("lbs")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .build();

        mockMvc.perform(post("/api/fitness/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void create_authenticated_returns201() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        GoalRequest request = GoalRequest.builder()
                .goalType(Goal.GoalType.WEIGHT_LOSS)
                .description("Lose 10 lbs")
                .targetValue(BigDecimal.valueOf(160))
                .unit("lbs")
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(3))
                .build();

        when(goalService.create(eq(1L), any(GoalRequest.class)))
                .thenReturn(sampleGoal(1L, 1L));

        mockMvc.perform(post("/api/fitness/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.goalType").value("WEIGHT_LOSS"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(goalService.getAllForUser(eq(1L), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(
                        List.of(sampleGoal(1L, 1L), sampleGoal(2L, 1L)),
                        PageRequest.of(0, 25), 2));

        mockMvc.perform(get("/api/fitness/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].userId").value(1));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getById_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(goalService.getById(eq(1L), eq(1L))).thenReturn(sampleGoal(1L, 1L));

        mockMvc.perform(get("/api/fitness/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.goalType").value("WEIGHT_LOSS"))
                .andExpect(jsonPath("$.data.description").value("Lose 10 lbs"));

        SecurityContextHolder.clearContext();
    }
}
