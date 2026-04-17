package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.MergeRequest;
import com.campusfit.masterdata.entity.MergeOperation;
import com.campusfit.masterdata.service.*;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MasterDataMergeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class MasterDataMergeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DuplicateMergeService mergeService;

    @MockBean
    private TermService termService;

    @MockBean
    private SchoolService schoolService;

    @MockBean
    private MajorService majorService;

    @MockBean
    private ClassService classService;

    @MockBean
    private CourseService courseService;

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

    @Test
    void getCandidates_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/admin/master-data/merge")
                        .param("entityType", "SCHOOL"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCandidates_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(schoolService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/master-data/merge")
                        .param("entityType", "SCHOOL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void merge_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        MergeOperation operation = MergeOperation.builder()
                .id(1L)
                .entityType("SCHOOL")
                .sourceId(2L)
                .targetId(3L)
                .mergedBy(1L)
                .createdAt(LocalDateTime.now())
                .build();

        when(mergeService.merge(eq("SCHOOL"), eq(2L), eq(3L), eq(1L)))
                .thenReturn(operation);

        MergeRequest request = MergeRequest.builder()
                .entityType("SCHOOL")
                .sourceId(2L)
                .targetId(3L)
                .build();

        mockMvc.perform(post("/api/admin/master-data/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.entityType").value("SCHOOL"))
                .andExpect(jsonPath("$.data.sourceId").value(2))
                .andExpect(jsonPath("$.data.targetId").value(3));

        SecurityContextHolder.clearContext();
    }
}
