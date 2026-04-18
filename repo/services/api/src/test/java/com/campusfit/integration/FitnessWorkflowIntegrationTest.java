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

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FitnessWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void seedRoles() {
        if (roleRepository.findByCode("REGULAR_USER").isEmpty()) {
            roleRepository.save(Role.builder()
                    .code("REGULAR_USER").name("Regular User").description("Default role").build());
        }
    }

    // ---- Assessment ----

    @Test
    void assessment_upsertThenGet_persistsCorrectly() throws Exception {
        String token = signUpAndGetToken("fw_assess_user");

        mockMvc.perform(put("/api/fitness/assessment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "heightFeet", 5,
                                "heightInches", 10,
                                "weightLbs", 175.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.heightFeet").value(5))
                .andExpect(jsonPath("$.data.heightInches").value(10))
                .andExpect(jsonPath("$.data.weightLbs").value(175.0));

        mockMvc.perform(get("/api/fitness/assessment")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.heightFeet").value(5))
                .andExpect(jsonPath("$.data.weightLbs").value(175.0));
    }

    @Test
    void assessment_updateOverwritesPrevious() throws Exception {
        String token = signUpAndGetToken("fw_assess_upd_user");

        mockMvc.perform(put("/api/fitness/assessment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "heightFeet", 5, "heightInches", 8, "weightLbs", 200.0))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/fitness/assessment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "heightFeet", 5, "heightInches", 8, "weightLbs", 195.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weightLbs").value(195.0));

        mockMvc.perform(get("/api/fitness/assessment")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weightLbs").value(195.0));
    }

    // ---- Goal ----

    @Test
    void goal_createListGetById() throws Exception {
        String token = signUpAndGetToken("fw_goal_user");
        String targetDate = LocalDate.now().plusYears(1).toString();

        MvcResult createResult = mockMvc.perform(post("/api/fitness/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "goalType", "WEIGHT_LOSS",
                                "targetValue", 150,
                                "unit", "lbs",
                                "startDate", LocalDate.now().toString(),
                                "targetDate", targetDate))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.goalType").value("WEIGHT_LOSS"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        long goalId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/fitness/goals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/fitness/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(goalId))
                .andExpect(jsonPath("$.data.goalType").value("WEIGHT_LOSS"));
    }

    @Test
    void goal_nonExistentId_returns4xx() throws Exception {
        String token = signUpAndGetToken("fw_notfound_user");

        mockMvc.perform(get("/api/fitness/goals/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void goal_pastTargetDate_returns422() throws Exception {
        String token = signUpAndGetToken("fw_pastdate_user");

        mockMvc.perform(post("/api/fitness/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "goalType", "STRENGTH",
                                "targetValue", 200,
                                "unit", "lbs",
                                "startDate", LocalDate.now().toString(),
                                "targetDate", LocalDate.now().minusDays(1).toString()))))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---- Check-in ----

    @Test
    void checkIn_createAndList() throws Exception {
        String token = signUpAndGetToken("fw_checkin_user");
        String targetDate = LocalDate.now().plusYears(1).toString();

        MvcResult goalResult = mockMvc.perform(post("/api/fitness/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "goalType", "ENDURANCE",
                                "targetValue", 10,
                                "unit", "km",
                                "startDate", LocalDate.now().toString(),
                                "targetDate", targetDate))))
                .andExpect(status().isCreated())
                .andReturn();

        long goalId = extractId(goalResult, "/data/id");

        mockMvc.perform(post("/api/fitness/goals/" + goalId + "/check-ins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "value", 8.5,
                                "notes", "Good run today"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/fitness/goals/" + goalId + "/check-ins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void checkIn_multipleEntries_allPersisted() throws Exception {
        String token = signUpAndGetToken("fw_multi_checkin_user");
        String targetDate = LocalDate.now().plusYears(1).toString();

        MvcResult goalResult = mockMvc.perform(post("/api/fitness/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "goalType", "FLEXIBILITY",
                                "targetValue", 90,
                                "unit", "degrees",
                                "startDate", LocalDate.now().toString(),
                                "targetDate", targetDate))))
                .andExpect(status().isCreated())
                .andReturn();

        long goalId = extractId(goalResult, "/data/id");

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/fitness/goals/" + goalId + "/check-ins")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("value", 60 + i * 5))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/fitness/goals/" + goalId + "/check-ins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ---- Security ----

    @Test
    void fitnessEndpoints_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/fitness/assessment")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/fitness/goals")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/fitness/goals/1/check-ins")).andExpect(status().isUnauthorized());
    }

    // ---- Helpers ----

    private String signUpAndGetToken(String username) throws Exception {
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        SignUpRequest.builder().username(username).password("password123").build())));

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
