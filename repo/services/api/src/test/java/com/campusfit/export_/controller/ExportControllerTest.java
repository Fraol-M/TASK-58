package com.campusfit.export_.controller;

import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.service.PasswordService;
import com.campusfit.shared.audit.AuditLogService;
import com.campusfit.export_.dto.ExportRequest;
import com.campusfit.export_.dto.ExportResponse;
import com.campusfit.export_.entity.ExportJob;
import com.campusfit.export_.service.AccountDeletionService;
import com.campusfit.export_.service.AccountImportService;
import com.campusfit.export_.service.ExportService;
import com.campusfit.shared.config.SecurityConfig;
import com.campusfit.shared.exception.BusinessException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        AuthenticationEntryPointImpl.class, AccessDeniedHandlerImpl.class})
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExportService exportService;

    @MockBean
    private AccountDeletionService accountDeletionService;

    @MockBean
    private AccountImportService accountImportService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private AuditLogService auditLogService;

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

    private ExportResponse sampleExport(Long id) {
        return ExportResponse.builder()
                .id(id)
                .userId(1L)
                .exportType(ExportJob.ExportType.ACCOUNT_DATA)
                .status(ExportJob.JobStatus.PENDING)
                .downloadReady(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    void requestExport_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.ACCOUNT_DATA)
                .build();

        mockMvc.perform(post("/api/exports/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listExports_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/exports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void importAccount_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/imports/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestDeletion_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "pass"))))
                .andExpect(status().isUnauthorized());
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void requestExport_missingType_returns422() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        // exportType null — @NotNull should reject
        ExportRequest request = ExportRequest.builder().build();

        mockMvc.perform(post("/api/exports/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.exportType").value("Export type is required"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void requestDeletion_missingPassword_returns400() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password confirmation is required for account deletion"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void requestDeletion_wrongPassword_returns400() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        User user = new User();
        user.setPasswordHash("$2a$hashed");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.matches("wrongpass", "$2a$hashed")).thenReturn(false);

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "wrongpass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect password"));

        SecurityContextHolder.clearContext();
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    void requestExport_authenticated_returns201() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        ExportRequest request = ExportRequest.builder()
                .exportType(ExportJob.ExportType.ACCOUNT_DATA)
                .passwordProtected(false)
                .build();

        when(exportService.createExportJob(eq(1L), any(ExportRequest.class)))
                .thenReturn(sampleExport(10L));

        mockMvc.perform(post("/api/exports/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.exportType").value("ACCOUNT_DATA"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void listExports_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(exportService.getByUserId(1L)).thenReturn(List.of(sampleExport(10L), sampleExport(11L)));

        mockMvc.perform(get("/api/exports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getExport_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(exportService.getById(10L, 1L)).thenReturn(sampleExport(10L));

        mockMvc.perform(get("/api/exports/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getExport_otherUsersExport_returns400() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(exportService.getById(99L, 1L))
                .thenThrow(new BusinessException("Export not found or access denied"));

        mockMvc.perform(get("/api/exports/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Export not found or access denied"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void requestDeletion_correctPassword_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        User user = new User();
        user.setPasswordHash("$2a$hashed");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordService.matches("correctpass", "$2a$hashed")).thenReturn(true);
        when(accountDeletionService.requestDeletion(1L)).thenReturn(null);

        mockMvc.perform(delete("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "correctpass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void importAccount_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        when(accountImportService.importAccountData(eq(1L), any()))
                .thenReturn("Import completed: 5 records restored");

        mockMvc.perform(post("/api/imports/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profile\":{\"displayName\":\"Test\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Import completed: 5 records restored"));

        SecurityContextHolder.clearContext();
    }

    // ── Download export ─────────────────────────────────────────────────────

    @Test
    void downloadExport_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/exports/10/download"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void downloadExport_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        Path tempFile = java.nio.file.Files.createTempFile("export-test-", ".enc");
        java.nio.file.Files.writeString(tempFile, "encrypted-data");
        when(exportService.getExportFilePath(10L, 1L)).thenReturn(tempFile);

        mockMvc.perform(get("/api/exports/10/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("attachment")));

        java.nio.file.Files.deleteIfExists(tempFile);
        SecurityContextHolder.clearContext();
    }

    // ── Import account from file ────────────────────────────────────────────

    @Test
    void importAccountFromFile_unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        MockMultipartFile file = new MockMultipartFile(
                "file", "export.enc", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/imports/account/file")
                        .file(file)
                        .param("password", "secret"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void importAccountFromFile_authenticated_returns200() throws Exception {
        authenticateAs(1L, "user", Set.of("REGULAR_USER"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "export.enc", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[]{1, 2, 3});

        Map<String, Object> decryptedData = Map.of("profile", Map.of("displayName", "Test"));
        when(exportService.decryptExportFile(any(byte[].class), eq("secret")))
                .thenReturn(decryptedData);
        when(accountImportService.importAccountData(eq(1L), any()))
                .thenReturn("Import completed: 3 records restored");

        mockMvc.perform(multipart("/api/imports/account/file")
                        .file(file)
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Import completed: 3 records restored"));

        SecurityContextHolder.clearContext();
    }
}
