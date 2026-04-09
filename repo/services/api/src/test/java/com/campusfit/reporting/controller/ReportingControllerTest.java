package com.campusfit.reporting.controller;

import com.campusfit.reporting.dto.DashboardResponse;
import com.campusfit.reporting.service.DashboardService;
import com.campusfit.reporting.service.SlowQueryLogService;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.GlobalExceptionHandler;
import com.campusfit.shared.security.AccessDeniedHandlerImpl;
import com.campusfit.shared.security.AuthenticationEntryPointImpl;
import com.campusfit.shared.security.SessionAuthenticationFilter;
import com.campusfit.shared.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.campusfit.reporting.controller.ReportingController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private SlowQueryLogService slowQueryLogService;

    @MockBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    private void authenticateAs(Long userId, String username, Set<String> roles) {
        UserPrincipal principal = new UserPrincipal(userId, username, roles);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getDashboard_authenticated_returns200() throws Exception {
        authenticateAs(1L, "testuser", Set.of("REGULAR_USER"));

        DashboardResponse response = DashboardResponse.builder()
                .userRole("REGULAR_USER")
                .metrics(List.of())
                .summary(Map.of("activeGoals", 3))
                .build();

        when(dashboardService.getDashboard(any(UserPrincipal.class))).thenReturn(response);

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userRole").value("REGULAR_USER"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getDashboard_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPerformance_adminOnly_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(slowQueryLogService.getPerformanceMetrics()).thenReturn(List.of(
                Map.of("query", "SELECT 1", "avgDurationMs", 10)
        ));

        mockMvc.perform(get("/api/admin/performance")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        SecurityContextHolder.clearContext();
    }

    @Test
    void getPerformance_nonAdmin_returns403() throws Exception {
        authenticateAs(2L, "regularuser", Set.of("REGULAR_USER"));

        mockMvc.perform(get("/api/admin/performance")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        SecurityContextHolder.clearContext();
    }
}
