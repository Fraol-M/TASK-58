package com.campusfit.integration;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.entity.Role;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.entity.UserRole;
import com.campusfit.auth.repository.RoleRepository;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;

    @BeforeEach
    void seedRoles() {
        seedRole("REGULAR_USER", "Regular User");
        seedRole("ADMIN", "Administrator");
    }

    // ---- Dashboard ----

    @Test
    void dashboard_regularUser_returnsOk() throws Exception {
        String token = signUpAndGetToken("rpt_reg_user", "REGULAR_USER");

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void dashboard_adminUser_returnsOk() throws Exception {
        String token = signUpAndGetToken("rpt_admin_dash_user", "ADMIN");

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void dashboard_responseContainsExpectedFields() throws Exception {
        String token = signUpAndGetToken("rpt_fields_user", "REGULAR_USER");

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void dashboard_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // ---- Performance (admin-only) ----

    @Test
    void performance_adminUser_returnsOk() throws Exception {
        String token = signUpAndGetToken("rpt_admin_perf_user", "ADMIN");

        mockMvc.perform(get("/api/admin/performance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void performance_regularUser_returns403() throws Exception {
        String token = signUpAndGetToken("rpt_regular_perf_user", "REGULAR_USER");

        mockMvc.perform(get("/api/admin/performance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void performance_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/performance"))
                .andExpect(status().isUnauthorized());
    }

    // ---- Helpers ----

    private void seedRole(String code, String name) {
        if (roleRepository.findByCode(code).isEmpty()) {
            roleRepository.save(Role.builder().code(code).name(name).description(name).build());
        }
    }

    private String signUpAndGetToken(String username, String roleCode) throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        SignUpRequest.builder().username(username).password("password123").build())));

        if (!"REGULAR_USER".equals(roleCode)) {
            User user = userRepository.findByUsername(username).orElseThrow();
            Role role = roleRepository.findByCode(roleCode).orElseThrow();
            userRoleRepository.save(UserRole.builder()
                    .userId(user.getId()).roleId(role.getId()).build());
        }

        MvcResult r = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().username(username).password("password123").build())))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).at("/data/token").asText();
    }
}
