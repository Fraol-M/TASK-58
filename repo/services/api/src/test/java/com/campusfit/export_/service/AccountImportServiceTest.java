package com.campusfit.export_.service;

import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.service.AssessmentService;
import com.campusfit.fitness.service.CheckInService;
import com.campusfit.fitness.service.GoalService;
import com.campusfit.study.service.StudyExportImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountImportServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudyExportImportService studyExportImportService;
    @Mock private AssessmentService assessmentService;
    @Mock private GoalService goalService;
    @Mock private CheckInService checkInService;

    private AccountImportService importService;

    @BeforeEach
    void setUp() {
        importService = new AccountImportService(
                userRepository, studyExportImportService, assessmentService,
                goalService, checkInService);
    }

    @Test
    void importAccountData_remapsGoalIdsForCheckIns() {
        // Simulate exported data with old goal ID 99
        Map<String, Object> goalData = new LinkedHashMap<>();
        goalData.put("id", 99L);
        goalData.put("goalType", "ENDURANCE");
        goalData.put("targetValue", 42.2);
        goalData.put("unit", "km");
        goalData.put("startDate", "2026-01-01");
        goalData.put("targetDate", "2026-12-31");

        Map<String, Object> checkInData = new LinkedHashMap<>();
        checkInData.put("goalId", 99L);  // references OLD goal ID
        checkInData.put("value", 10.0);
        checkInData.put("notes", "Week 1");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("goalsData", List.of(goalData));
        data.put("checkInsData", List.of(checkInData));

        // goalService.create returns a response with NEW id 500
        GoalResponse createdGoal = GoalResponse.builder()
                .id(500L)
                .userId(1L)
                .goalType(Goal.GoalType.ENDURANCE)
                .targetValue(new BigDecimal("42.20"))
                .startValue(BigDecimal.ZERO)
                .currentValue(BigDecimal.ZERO)
                .unit("km")
                .startDate(LocalDate.of(2026, 1, 1))
                .targetDate(LocalDate.of(2026, 12, 31))
                .status(Goal.GoalStatus.ACTIVE)
                .milestones(List.of())
                .build();
        when(goalService.create(eq(1L), any())).thenReturn(createdGoal);

        String result = importService.importAccountData(1L, data);

        // Verify check-in was created with the NEW goal ID (500), not the old (99)
        verify(checkInService).create(eq(500L), eq(1L), any());
        assertThat(result).contains("Goals: 1").contains("Check-ins: 1");
    }

    @Test
    void importAccountData_restoresProfileMetadata() {
        User user = User.builder().id(1L).username("testuser").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("email", "restored@campus.edu");
        profile.put("phone", "555-0100");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("profileData", profile);

        importService.importAccountData(1L, data);

        verify(userRepository).save(argThat(u ->
                "restored@campus.edu".equals(u.getEmail()) &&
                "555-0100".equals(u.getPhone())
        ));
    }
}
