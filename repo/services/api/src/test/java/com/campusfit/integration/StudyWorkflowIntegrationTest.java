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
class StudyWorkflowIntegrationTest {

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

    // ---- Study plan CRUD ----

    @Test
    void studyPlan_createListGetById_fullFlow() throws Exception {
        String token = signUpAndGetToken("sw_plan_user");

        MvcResult createResult = mockMvc.perform(post("/api/study/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Integration Study Plan"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Integration Study Plan"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        long planId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/study/plans")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/study/plans/" + planId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(planId))
                .andExpect(jsonPath("$.data.title").value("Integration Study Plan"));
    }

    @Test
    void studyPlan_missingTitle_returns422() throws Exception {
        String token = signUpAndGetToken("sw_notitle_user");

        mockMvc.perform(post("/api/study/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", "no title here"))))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---- Daily completion ----

    @Test
    void dailyCompletion_createAndList() throws Exception {
        String token = signUpAndGetToken("sw_comp_user");

        MvcResult planResult = mockMvc.perform(post("/api/study/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Completion Plan"))))
                .andExpect(status().isCreated())
                .andReturn();

        long planId = extractId(planResult, "/data/id");

        mockMvc.perform(post("/api/study/plans/" + planId + "/completions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "completedDate", LocalDate.now().toString(),
                                "completed", true,
                                "notes", "Good session"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/study/plans/" + planId + "/completions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ---- Forgetting point + SM-2 review ----

    @Test
    void forgettingPoint_createListAndReview_sm2Flow() throws Exception {
        String token = signUpAndGetToken("sw_fp_user");

        MvcResult planResult = mockMvc.perform(post("/api/study/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "FP Plan"))))
                .andExpect(status().isCreated())
                .andReturn();

        long planId = extractId(planResult, "/data/id");

        MvcResult fpResult = mockMvc.perform(post("/api/study/plans/" + planId + "/forgetting-points")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "topic", "Calculus derivatives"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.topic").value("Calculus derivatives"))
                .andReturn();

        long fpId = extractId(fpResult, "/data/id");

        mockMvc.perform(get("/api/study/plans/" + planId + "/forgetting-points")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // SM-2 review: quality=4 means good recall; next review date must be scheduled
        mockMvc.perform(post("/api/study/forgetting-points/" + fpId + "/review")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quality", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextReviewDate").exists());
    }

    @Test
    void forgettingPoint_review_qualityBelowThreshold_shortensInterval() throws Exception {
        String token = signUpAndGetToken("sw_fp_low_user");

        MvcResult planResult = mockMvc.perform(post("/api/study/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Low Quality Plan"))))
                .andExpect(status().isCreated())
                .andReturn();

        long planId = extractId(planResult, "/data/id");

        MvcResult fpResult = mockMvc.perform(post("/api/study/plans/" + planId + "/forgetting-points")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("topic", "Organic chemistry"))))
                .andExpect(status().isCreated())
                .andReturn();

        long fpId = extractId(fpResult, "/data/id");

        // quality=1 (poor recall) — SM-2 resets interval to 1 day
        mockMvc.perform(post("/api/study/forgetting-points/" + fpId + "/review")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quality", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextReviewDate").value(
                        LocalDate.now().plusDays(1).toString()));
    }

    // ---- Export / import round-trip ----

    @Test
    void studyExport_thenImport_roundTrip() throws Exception {
        String token = signUpAndGetToken("sw_exp_user");

        mockMvc.perform(post("/api/study/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "Export Me"))))
                .andExpect(status().isCreated());

        MvcResult exportResult = mockMvc.perform(get("/api/study/export")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        String exportedData = objectMapper.readTree(
                exportResult.getResponse().getContentAsString()).at("/data").toString();

        mockMvc.perform(post("/api/study/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exportedData))
                .andExpect(status().isOk());
    }

    // ---- Security ----

    @Test
    void studyEndpoints_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/study/plans")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/study/export")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/study/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
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
