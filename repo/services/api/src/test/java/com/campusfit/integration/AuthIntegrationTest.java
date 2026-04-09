package com.campusfit.integration;

import com.campusfit.auth.dto.LoginRequest;
import com.campusfit.auth.dto.SignUpRequest;
import com.campusfit.auth.entity.Role;
import com.campusfit.auth.repository.RoleRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @SpringBootTest integration suite covering the auth/session flow against a real H2 database
 * and the full Spring Security filter chain.  This complements the @WebMvcTest / Mockito-only
 * unit layer by exercising DB persistence, session-token lookup, and security-filter interactions
 * together without any mocking.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Seed baseline role data that Flyway would normally insert via V2.
     * The test profile disables Flyway, so we seed manually before each test.
     */
    @BeforeEach
    void seedRoles() {
        if (roleRepository.findByCode("REGULAR_USER").isEmpty()) {
            roleRepository.save(Role.builder()
                    .code("REGULAR_USER")
                    .name("Regular User")
                    .description("Default role assigned on sign-up")
                    .build());
        }
        if (roleRepository.findByCode("ADMIN").isEmpty()) {
            roleRepository.save(Role.builder()
                    .code("ADMIN")
                    .name("Administrator")
                    .description("Platform administrator")
                    .build());
        }
    }

    // -------------------------------------------------------------------------
    // Happy path: sign-up → sign-in → GET /api/me
    // Verifies the full DB + session-filter chain in one pass.
    // -------------------------------------------------------------------------

    @Test
    void signUp_signIn_getMe_fullFlow() throws Exception {
        SignUpRequest signUp = SignUpRequest.builder()
                .username("int_full_flow")
                .password("password123")
                .email("full@integration.test")
                .build();

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("int_full_flow"))
                .andExpect(jsonPath("$.data.roles[0]").value("REGULAR_USER"));

        LoginRequest login = LoginRequest.builder()
                .username("int_full_flow")
                .password("password123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String token = extractToken(loginResult);
        assertThat(token).isNotEmpty();

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("int_full_flow"))
                .andExpect(jsonPath("$.data.email").value("full@integration.test"));
    }

    // -------------------------------------------------------------------------
    // Security filter chain: unauthenticated requests must be rejected
    // -------------------------------------------------------------------------

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer non-existent-session-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_regularUser_returns403() throws Exception {
        // Sign up + sign in as a regular user
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SignUpRequest.builder()
                        .username("int_regular")
                        .password("password123")
                        .build())));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .username("int_regular")
                                .password("password123")
                                .build())))
                .andReturn();

        String token = extractToken(loginResult);

        // /api/admin/** requires ADMIN role → regular user gets 403
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // Auth business logic: wrong password, duplicate username
    // -------------------------------------------------------------------------

    @Test
    void signIn_wrongPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SignUpRequest.builder()
                        .username("int_bad_pw")
                        .password("correctpassword")
                        .build())));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .username("int_bad_pw")
                                .password("wrongpassword")
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void signUp_duplicateUsername_returns400() throws Exception {
        SignUpRequest req = SignUpRequest.builder()
                .username("int_duplicate")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists: int_duplicate"));
    }

    // -------------------------------------------------------------------------
    // Session lifecycle: sign-out invalidates the token
    // -------------------------------------------------------------------------

    @Test
    void signOut_invalidatesSession_tokenNoLongerWorks() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SignUpRequest.builder()
                        .username("int_signout")
                        .password("password123")
                        .build())));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .username("int_signout")
                                .password("password123")
                                .build())))
                .andReturn();

        String token = extractToken(loginResult);

        // Token works before sign-out
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Sign out
        mockMvc.perform(post("/api/auth/sign-out")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Token no longer valid after sign-out
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Lockout chain: max failed attempts locks the account (DB + service)
    // -------------------------------------------------------------------------

    @Test
    void signIn_maxFailedAttempts_accountLockedOut() throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SignUpRequest.builder()
                        .username("int_lockout")
                        .password("correctpassword")
                        .build())));

        LoginRequest badLogin = LoginRequest.builder()
                .username("int_lockout")
                .password("wrongpassword")
                .build();

        // 5 failed attempts (matches app.lockout.max-attempts=5 in test profile)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badLogin)))
                    .andExpect(status().isBadRequest());
        }

        // 6th attempt — account should now be locked
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Account is locked due to too many failed attempts. Please try again later."));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private String extractToken(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        return root.at("/data/token").asText();
    }
}
