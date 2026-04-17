package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.ChangeHistoryResponse;
import com.campusfit.masterdata.service.ChangeHistoryService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChangeHistoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class ChangeHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChangeHistoryService changeHistoryService;

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

    private ChangeHistoryResponse sampleHistoryResponse() {
        return ChangeHistoryResponse.builder()
                .id(1L)
                .entityType("SCHOOL")
                .entityId(1L)
                .field("name")
                .oldValue("Old Name")
                .newValue("New Name")
                .changedBy(1L)
                .changedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getHistory_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/admin/master-data/history")
                        .param("entityType", "SCHOOL"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHistory_admin_byType_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(changeHistoryService.getByEntityType("SCHOOL"))
                .thenReturn(List.of(sampleHistoryResponse()));

        mockMvc.perform(get("/api/admin/master-data/history")
                        .param("entityType", "SCHOOL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].entityType").value("SCHOOL"))
                .andExpect(jsonPath("$.data[0].field").value("name"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getHistory_admin_byEntity_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(changeHistoryService.getByEntity("SCHOOL", 1L))
                .thenReturn(List.of(sampleHistoryResponse()));

        mockMvc.perform(get("/api/admin/master-data/history")
                        .param("entityType", "SCHOOL")
                        .param("entityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].entityId").value(1));

        SecurityContextHolder.clearContext();
    }
}
