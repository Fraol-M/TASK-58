package com.campusfit.integration;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.entity.Role;
import com.campusfit.auth.repository.RoleRepository;
import com.campusfit.export_.entity.ExportJob;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test covering the export lifecycle end-to-end against a real H2 database
 * and the full Spring Security filter chain.
 *
 * Exercises: create export without password (→ 400), create export with password
 * (→ 201, COMPLETED, downloadReady=true, expiresAt set), and list exports for the user.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.export.base-dir=${java.io.tmpdir}/campusfit-test-exports")
class ExportLifecycleIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void seedRoles() {
        if (roleRepository.findByCode("REGULAR_USER").isEmpty()) {
            roleRepository.save(Role.builder()
                    .code("REGULAR_USER")
                    .name("Regular User")
                    .description("Default role assigned on sign-up")
                    .build());
        }
    }

    // ---- Guard: password is mandatory ----

    @Test
    void export_missingPassword_returns400() throws Exception {
        String token = signUpAndGetToken("ex_no_pw_u", "password123");

        mockMvc.perform(post("/api/exports/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "exportType", "ACCOUNT_DATA",
                                "passwordProtected", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "Export password is required. All exports must be password-protected."));
    }

    // ---- Happy path: full lifecycle ----

    @Test
    void export_withPassword_completesAndIsListable() throws Exception {
        String token = signUpAndGetToken("ex_happy_u", "password123");

        // 1. Create export — should complete synchronously in test environment
        MvcResult createResult = mockMvc.perform(post("/api/exports/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "exportType", "ACCOUNT_DATA",
                                "passwordProtected", true,
                                "exportPassword", "SecurePass!99"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(ExportJob.JobStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.data.downloadReady").value(true))
                .andExpect(jsonPath("$.data.passwordProtected").value(true))
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andReturn();

        long exportId = extractId(createResult, "/data/id");
        assertThat(exportId).isPositive();

        // 2. GET /api/exports/{id} — single export matches what was created
        mockMvc.perform(get("/api/exports/" + exportId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(exportId))
                .andExpect(jsonPath("$.data.status").value(ExportJob.JobStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.data.downloadReady").value(true));

        // 3. GET /api/exports — list includes the created export
        mockMvc.perform(get("/api/exports")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(exportId));
    }

    @Test
    void export_studyData_completesSuccessfully() throws Exception {
        String token = signUpAndGetToken("ex_study_u", "password123");

        mockMvc.perform(post("/api/exports/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "exportType", "STUDY_DATA",
                                "passwordProtected", true,
                                "exportPassword", "SecureStudy!88"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value(ExportJob.JobStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.data.downloadReady").value(true));
    }

    @Test
    void export_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/exports/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "exportType", "ACCOUNT_DATA",
                                "passwordProtected", true,
                                "exportPassword", "SomePass!77"))))
                .andExpect(status().isUnauthorized());
    }

    // ---- Download ----

    @Test
    void exportDownload_completedExport_returnsOctetStream() throws Exception {
        String token = signUpAndGetToken("ex_dl_u", "password123");

        MvcResult createResult = mockMvc.perform(post("/api/exports/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "exportType", "ACCOUNT_DATA",
                                "passwordProtected", true,
                                "exportPassword", "DownloadPass!55"))))
                .andExpect(status().isCreated())
                .andReturn();

        long exportId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/exports/" + exportId + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")));
    }

    // ---- Import (JSON) ----

    @Test
    void importAccount_json_endpointAccessible() throws Exception {
        String token = signUpAndGetToken("ex_impjson_u", "password123");

        MvcResult result = mockMvc.perform(post("/api/imports/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotIn(401, 403, 500, 503);
    }

    // ---- Import (file) ----

    @Test
    void importAccountFromFile_invalidBytes_returns4xx() throws Exception {
        String token = signUpAndGetToken("ex_impfile_u", "password123");

        MockMultipartFile file = new MockMultipartFile(
                "file", "export.enc", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "not-valid-encrypted-content".getBytes());

        mockMvc.perform(multipart("/api/imports/account/file")
                        .file(file)
                        .param("password", "WrongPassword!1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }

    // ---- Account deletion ----

    @Test
    void deleteAccount_withCorrectPassword_returns200() throws Exception {
        String token = signUpAndGetToken("ex_delete_u", "password123");

        mockMvc.perform(delete("/api/account")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ---- Helpers ----

    private String signUpAndGetToken(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        SignUpRequest.builder().username(username).password(password).build())));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        LoginRequest.builder().username(username).password(password).build())))
                .andReturn();
        return extractToken(loginResult);
    }

    private String extractToken(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at("/data/token").asText();
    }

    private long extractId(MvcResult result, String jsonPath) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at(jsonPath).asLong();
    }
}
