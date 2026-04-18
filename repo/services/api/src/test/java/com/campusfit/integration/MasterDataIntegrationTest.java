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

import org.springframework.mock.web.MockMultipartFile;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MasterDataIntegrationTest {

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

    // ---- RBAC guard ----

    @Test
    void adminEndpoints_regularUser_returns403() throws Exception {
        String token = signUpAndGetToken("md_regular_u", "REGULAR_USER");

        mockMvc.perform(get("/api/admin/terms").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/schools").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/majors").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/classes").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/courses").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---- Term CRUD ----

    @Test
    void term_createListGetUpdateDelete() throws Exception {
        String token = signUpAndGetToken("md_term_admin", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/admin/terms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "TERM-2030-S1",
                                "name", "Spring 2030",
                                "effectiveFrom", "01/01/2030",
                                "startDate", "02/01/2030",
                                "endDate", "06/30/2030"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("TERM-2030-S1"))
                .andExpect(jsonPath("$.data.name").value("Spring 2030"))
                .andReturn();

        long termId = extractId(createResult, "/data/id");

        // List
        mockMvc.perform(get("/api/admin/terms").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        // Get by ID
        mockMvc.perform(get("/api/admin/terms/" + termId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(termId));

        // Update
        mockMvc.perform(put("/api/admin/terms/" + termId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "TERM-2030-S1",
                                "name", "Spring 2030 Updated",
                                "effectiveFrom", "01/01/2030",
                                "startDate", "02/01/2030",
                                "endDate", "06/30/2030"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Spring 2030 Updated"));

        // Delete
        mockMvc.perform(delete("/api/admin/terms/" + termId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Delete is soft-delete, so the record remains addressable but inactive
        mockMvc.perform(get("/api/admin/terms/" + termId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    // ---- School CRUD ----

    @Test
    void school_createListGetUpdateDelete() throws Exception {
        String token = signUpAndGetToken("md_school_admin", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/admin/schools")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SCH-ENG",
                                "name", "School of Engineering",
                                "effectiveFrom", "01/01/2025"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("SCH-ENG"))
                .andReturn();

        long schoolId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/admin/schools").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/admin/schools/" + schoolId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(schoolId));

        mockMvc.perform(put("/api/admin/schools/" + schoolId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SCH-ENG",
                                "name", "School of Engineering (Updated)",
                                "effectiveFrom", "01/01/2025"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("School of Engineering (Updated)"));

        mockMvc.perform(delete("/api/admin/schools/" + schoolId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ---- Hierarchy chain: school → major → class + term → course ----

    @Test
    void masterData_hierarchyChain_schoolMajorClassTermCourse() throws Exception {
        String token = signUpAndGetToken("md_chain_admin", "ADMIN");

        // Create school
        MvcResult schoolResult = mockMvc.perform(post("/api/admin/schools")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SCH-CHAIN", "name", "Chain School", "effectiveFrom", "01/01/2025"))))
                .andExpect(status().isCreated())
                .andReturn();
        long schoolId = extractId(schoolResult, "/data/id");

        // Create major (depends on school)
        MvcResult majorResult = mockMvc.perform(post("/api/admin/majors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "MAJ-CS", "name", "Computer Science",
                                "effectiveFrom", "01/01/2025", "schoolId", schoolId))))
                .andExpect(status().isCreated())
                .andReturn();
        long majorId = extractId(majorResult, "/data/id");

        // Create class (depends on major)
        MvcResult classResult = mockMvc.perform(post("/api/admin/classes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CLS-CS-2024", "name", "CS Class 2024",
                                "effectiveFrom", "01/01/2025", "majorId", majorId, "year", 2024))))
                .andExpect(status().isCreated())
                .andReturn();
        long classId = extractId(classResult, "/data/id");

        // Create term (independent)
        MvcResult termResult = mockMvc.perform(post("/api/admin/terms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "TERM-CHAIN-S1", "name", "Chain Spring",
                                "effectiveFrom", "01/01/2025",
                                "startDate", "02/01/2025",
                                "endDate", "06/30/2025"))))
                .andExpect(status().isCreated())
                .andReturn();
        long termId = extractId(termResult, "/data/id");

        // Create course (depends on class + term)
        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CRS-ALGO", "name", "Algorithms",
                                "effectiveFrom", "01/01/2025",
                                "classId", classId, "termId", termId, "credits", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("CRS-ALGO"));

        // Verify each list contains items
        mockMvc.perform(get("/api/admin/majors").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/admin/classes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isArray());
        mockMvc.perform(get("/api/admin/courses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isArray());
    }

    // ---- Change history, export, merge ----

    @Test
    void changeHistory_adminCanAccess() throws Exception {
        String token = signUpAndGetToken("md_history_admin", "ADMIN");

        mockMvc.perform(get("/api/admin/master-data/history")
                        .param("entityType", "TERM")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void masterDataExport_adminCanAccess() throws Exception {
        String token = signUpAndGetToken("md_export_admin", "ADMIN");

        mockMvc.perform(get("/api/admin/master-data/export")
                        .param("entityType", "TERM")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void masterDataMerge_dryRun_adminCanAccess() throws Exception {
        String token = signUpAndGetToken("md_merge_admin", "ADMIN");

        mockMvc.perform(get("/api/admin/master-data/merge")
                        .param("entityType", "TERM")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ---- Major individual CRUD by ID ----

    @Test
    void major_getByIdUpdateDeleteById() throws Exception {
        String token = signUpAndGetToken("md_maj_crud_admin", "ADMIN");

        MvcResult schoolResult = mockMvc.perform(post("/api/admin/schools")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SCH-MAJ-CRUD", "name", "School for Major CRUD",
                                "effectiveFrom", "01/01/2025"))))
                .andExpect(status().isCreated())
                .andReturn();
        long schoolId = extractId(schoolResult, "/data/id");

        MvcResult createResult = mockMvc.perform(post("/api/admin/majors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "MAJ-CRUD-TST", "name", "Major CRUD Test",
                                "effectiveFrom", "01/01/2025", "schoolId", schoolId))))
                .andExpect(status().isCreated())
                .andReturn();
        long majorId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/admin/majors/" + majorId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("MAJ-CRUD-TST"));

        mockMvc.perform(put("/api/admin/majors/" + majorId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "MAJ-CRUD-TST", "name", "Major CRUD Updated",
                                "effectiveFrom", "01/01/2025", "schoolId", schoolId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Major CRUD Updated"));

        mockMvc.perform(delete("/api/admin/majors/" + majorId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/majors/" + majorId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    // ---- Class individual CRUD by ID ----

    @Test
    void class_getByIdUpdateDeleteById() throws Exception {
        String token = signUpAndGetToken("md_cls_crud_admin", "ADMIN");

        MvcResult schoolResult = mockMvc.perform(post("/api/admin/schools")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SCH-CLS-CRUD", "name", "School for Class CRUD",
                                "effectiveFrom", "01/01/2025"))))
                .andExpect(status().isCreated())
                .andReturn();
        long schoolId = extractId(schoolResult, "/data/id");

        MvcResult majorResult = mockMvc.perform(post("/api/admin/majors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "MAJ-CLS-CRUD", "name", "Major for Class CRUD",
                                "effectiveFrom", "01/01/2025", "schoolId", schoolId))))
                .andExpect(status().isCreated())
                .andReturn();
        long majorId = extractId(majorResult, "/data/id");

        MvcResult createResult = mockMvc.perform(post("/api/admin/classes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CLS-CRUD-TST", "name", "Class CRUD Test",
                                "effectiveFrom", "01/01/2025", "majorId", majorId, "year", 2025))))
                .andExpect(status().isCreated())
                .andReturn();
        long classId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/admin/classes/" + classId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CLS-CRUD-TST"));

        mockMvc.perform(put("/api/admin/classes/" + classId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CLS-CRUD-TST", "name", "Class CRUD Updated",
                                "effectiveFrom", "01/01/2025", "majorId", majorId, "year", 2025))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Class CRUD Updated"));

        mockMvc.perform(delete("/api/admin/classes/" + classId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/classes/" + classId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    // ---- Course individual CRUD by ID ----

    @Test
    void course_getByIdUpdateDeleteById() throws Exception {
        String token = signUpAndGetToken("md_crs_crud_admin", "ADMIN");

        MvcResult schoolResult = mockMvc.perform(post("/api/admin/schools")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SCH-CRS-CRUD", "name", "School for Course CRUD",
                                "effectiveFrom", "01/01/2025"))))
                .andExpect(status().isCreated())
                .andReturn();
        long schoolId = extractId(schoolResult, "/data/id");

        MvcResult majorResult = mockMvc.perform(post("/api/admin/majors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "MAJ-CRS-CRUD", "name", "Major for Course CRUD",
                                "effectiveFrom", "01/01/2025", "schoolId", schoolId))))
                .andExpect(status().isCreated())
                .andReturn();
        long majorId = extractId(majorResult, "/data/id");

        MvcResult classResult = mockMvc.perform(post("/api/admin/classes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CLS-CRS-CRUD", "name", "Class for Course CRUD",
                                "effectiveFrom", "01/01/2025", "majorId", majorId, "year", 2025))))
                .andExpect(status().isCreated())
                .andReturn();
        long classId = extractId(classResult, "/data/id");

        MvcResult termResult = mockMvc.perform(post("/api/admin/terms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "TERM-CRS-CRUD", "name", "Term for Course CRUD",
                                "effectiveFrom", "01/01/2025",
                                "startDate", "02/01/2025",
                                "endDate", "06/30/2025"))))
                .andExpect(status().isCreated())
                .andReturn();
        long termId = extractId(termResult, "/data/id");

        MvcResult createResult = mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CRS-CRUD-TST", "name", "Course CRUD Test",
                                "effectiveFrom", "01/01/2025",
                                "classId", classId, "termId", termId, "credits", 3))))
                .andExpect(status().isCreated())
                .andReturn();
        long courseId = extractId(createResult, "/data/id");

        mockMvc.perform(get("/api/admin/courses/" + courseId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CRS-CRUD-TST"));

        mockMvc.perform(put("/api/admin/courses/" + courseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CRS-CRUD-TST", "name", "Course CRUD Updated",
                                "effectiveFrom", "01/01/2025",
                                "classId", classId, "termId", termId, "credits", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Course CRUD Updated"));

        mockMvc.perform(delete("/api/admin/courses/" + courseId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/courses/" + courseId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    // ---- Master data imports (multipart file) ----

    @Test
    void masterDataImport_uploadFile_adminCanAccess() throws Exception {
        String token = signUpAndGetToken("md_imp_admin", "ADMIN");

        String csvContent = "code,name,effectiveFrom\nTERM-IMP-01,Imported Term,01/01/2030";
        MockMultipartFile file = new MockMultipartFile(
                "file", "terms.csv", "text/csv", csvContent.getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/admin/master-data/imports")
                        .file(file)
                        .param("entityType", "TERM")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotIn(401, 403);
    }

    @Test
    void masterDataImport_getJobById_adminCanAccess() throws Exception {
        String token = signUpAndGetToken("md_imp_get_admin", "ADMIN");

        // Non-existent job → 4xx, not 401/403
        mockMvc.perform(get("/api/admin/master-data/imports/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError());
    }

    // ---- Master data merge (POST) ----

    @Test
    void masterDataMerge_post_adminCanMergeEntities() throws Exception {
        String token = signUpAndGetToken("md_merge_post_adm", "ADMIN");

        MvcResult src = mockMvc.perform(post("/api/admin/terms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "TERM-MERGE-SRC", "name", "Spring 2029",
                                "effectiveFrom", "01/01/2029",
                                "startDate", "02/01/2029",
                                "endDate", "06/30/2029"))))
                .andExpect(status().isCreated())
                .andReturn();
        long srcId = extractId(src, "/data/id");

        MvcResult tgt = mockMvc.perform(post("/api/admin/terms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "TERM-MERGE-TGT", "name", "Spring 2029 Target",
                                "effectiveFrom", "01/01/2029",
                                "startDate", "02/01/2029",
                                "endDate", "06/30/2029"))))
                .andExpect(status().isCreated())
                .andReturn();
        long tgtId = extractId(tgt, "/data/id");

        mockMvc.perform(post("/api/admin/master-data/merge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "entityType", "TERM",
                                "sourceId", srcId,
                                "targetId", tgtId))))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoints_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/terms")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/schools")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/master-data/history")).andExpect(status().isUnauthorized());
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
