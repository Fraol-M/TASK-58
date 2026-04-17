package com.campusfit.fitness.controller;

import com.campusfit.fitness.dto.AssessmentRequest;
import com.campusfit.fitness.dto.AssessmentResponse;
import com.campusfit.fitness.service.AssessmentService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssessmentService assessmentService;

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

    private AssessmentResponse sampleResponse(Long id, Long userId) {
        return AssessmentResponse.builder()
                .id(id)
                .userId(userId)
                .assessmentType("INITIAL")
                .heightFeet(5)
                .heightInches(10)
                .formattedHeight("5'10\"")
                .weightLbs(175.0)
                .bodyFatPercent(15.0)
                .waistInches(32.0)
                .chestInches(40.0)
                .armInches(14.0)
                .assessmentDate(LocalDate.now())
                .notes("Initial assessment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void getLatest_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/fitness/assessment"))
                .andExpect(status().isUnauthorized());
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void getLatest_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(assessmentService.getLatest(1L)).thenReturn(sampleResponse(1L, 1L));

        mockMvc.perform(get("/api/fitness/assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.heightFeet").value(5))
                .andExpect(jsonPath("$.data.weightLbs").value(175.0));

        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrUpdate_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        AssessmentRequest request = AssessmentRequest.builder()
                .heightFeet(5)
                .heightInches(10)
                .weightLbs(175.0)
                .bodyFatPercent(15.0)
                .waist(32.0)
                .chest(40.0)
                .arm(14.0)
                .notes("Updated assessment")
                .build();

        when(assessmentService.createOrUpdate(eq(1L), any(AssessmentRequest.class)))
                .thenReturn(sampleResponse(1L, 1L));

        mockMvc.perform(put("/api/fitness/assessment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.formattedHeight").value("5'10\""));

        SecurityContextHolder.clearContext();
    }
}
