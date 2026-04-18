package com.campusfit.integration;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.entity.Role;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.entity.UserRole;
import com.campusfit.auth.repository.RoleRepository;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

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

    // ---- Happy path: admin creates, user reads ----

    @Test
    void notification_adminCreates_userReceivesMarkReadAndStatusCheck() throws Exception {
        String regularToken = signUpAndGetToken("nt_reg_user", "REGULAR_USER");
        String adminToken   = signUpAndGetToken("nt_admin_user", "ADMIN");

        User targetUser = userRepository.findByUsername("nt_reg_user").orElseThrow();

        // Admin creates notification targeting the regular user
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "ANNOUNCEMENT",
                                "title", "Integration Announcement",
                                "body", "Welcome to CampusFit",
                                "targetUserIds", List.of(targetUser.getId())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Regular user lists their notifications (should be paginated)
        MvcResult notificationsResult = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Integration Announcement"))
                .andReturn();

        long notificationId = objectMapper.readTree(notificationsResult.getResponse().getContentAsString())
                .at("/data/content/0/id")
                .asLong();

        // Regular user marks notification as read
        mockMvc.perform(post("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isOk());

        // Admin checks delivery status
        mockMvc.perform(get("/api/notifications/" + notificationId + "/status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void notification_multipleTypes_allCreated() throws Exception {
        String adminToken = signUpAndGetToken("nt_multi_admin", "ADMIN");
        String userToken  = signUpAndGetToken("nt_multi_user", "REGULAR_USER");

        User target = userRepository.findByUsername("nt_multi_user").orElseThrow();

        for (String type : new String[]{"ANNOUNCEMENT", "REMINDER", "FOLLOW_UP"}) {
            mockMvc.perform(post("/api/notifications")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "type", type,
                                    "title", type + " title",
                                    "targetUserIds", List.of(target.getId())))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ---- RBAC ----

    @Test
    void notification_regularUser_cannotCreate_returns403() throws Exception {
        String regularToken = signUpAndGetToken("nt_nocreate_user", "REGULAR_USER");

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + regularToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "ANNOUNCEMENT",
                                "title", "Should be blocked",
                                "targetUserIds", List.of(1L)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void notification_regularUser_cannotGetDeliveryStatus_returns403() throws Exception {
        String adminToken   = signUpAndGetToken("nt_status_admin", "ADMIN");
        String regularToken = signUpAndGetToken("nt_status_user", "REGULAR_USER");

        User target = userRepository.findByUsername("nt_status_user").orElseThrow();

        MvcResult createResult = mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "REMINDER",
                                "title", "Status check test",
                                "targetUserIds", List.of(target.getId())))))
                .andExpect(status().isCreated())
                .andReturn();

        long notificationId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/notifications/" + notificationId + "/status")
                        .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isForbidden());
    }

    // ---- Security ----

    @Test
    void notificationEndpoints_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/notifications/1/read")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/notifications/1/status")).andExpect(status().isUnauthorized());
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

    private long extractId(MvcResult result, String path) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at(path).asLong();
    }
}
