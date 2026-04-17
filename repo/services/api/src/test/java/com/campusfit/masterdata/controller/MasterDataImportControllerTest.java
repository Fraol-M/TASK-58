package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.ImportJobResponse;
import com.campusfit.masterdata.entity.ImportJob;
import com.campusfit.masterdata.service.MasterDataImportService;
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
import org.springframework.mock.web.MockMultipartFile;
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

@WebMvcTest(MasterDataImportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class MasterDataImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MasterDataImportService importService;

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

    private ImportJobResponse sampleJobResponse() {
        return ImportJobResponse.builder()
                .id(1L)
                .fileName("test.csv")
                .entityType("SCHOOL")
                .totalRows(1)
                .successCount(1)
                .errorCount(0)
                .status(ImportJob.JobStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .errors(Collections.emptyList())
                .build();
    }

    @Test
    void importFile_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv",
                "code,name\nSCH-001,Test".getBytes());

        mockMvc.perform(multipart("/api/admin/master-data/imports")
                        .file(file)
                        .param("entityType", "SCHOOL"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void importFile_admin_returns201() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(importService.processImport(any(), eq("SCHOOL"), eq(1L)))
                .thenReturn(sampleJobResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv",
                "code,name\nSCH-001,Test".getBytes());

        mockMvc.perform(multipart("/api/admin/master-data/imports")
                        .file(file)
                        .param("entityType", "SCHOOL"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("test.csv"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getJob_admin_returns200() throws Exception {
        authenticateAs(1L, "admin", Set.of("ADMIN"));

        when(importService.getJobById(1L)).thenReturn(sampleJobResponse());

        mockMvc.perform(get("/api/admin/master-data/imports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.entityType").value("SCHOOL"));

        SecurityContextHolder.clearContext();
    }
}
