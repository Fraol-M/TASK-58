package com.campusfit.reporting.service;

import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.repository.GoalRepository;
import com.campusfit.reporting.entity.DashboardSummary;
import com.campusfit.reporting.repository.DashboardSummaryRepository;
import com.campusfit.study.repository.StudyPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    private final DashboardSummaryRepository summaryRepository;
    private final GoalRepository goalRepository;
    private final StudyPlanRepository studyPlanRepository;

    public AggregationService(DashboardSummaryRepository summaryRepository,
                              GoalRepository goalRepository,
                              StudyPlanRepository studyPlanRepository) {
        this.summaryRepository = summaryRepository;
        this.goalRepository = goalRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    /**
     * Nightly aggregation job - computes dashboard summaries from raw data.
     */
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM daily
    @Transactional
    public void computeNightlyAggregation() {
        log.info("Starting nightly dashboard aggregation");

        String periodValue = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        List<DashboardSummary> summaries = new ArrayList<>();

        // Total goals by status
        long totalGoals = goalRepository.count();
        summaries.add(DashboardSummary.builder()
                .metricKey("total_goals")
                .metricValue(BigDecimal.valueOf(totalGoals))
                .dimension("system")
                .periodType("DAILY")
                .periodValue(periodValue)
                .computedAt(LocalDateTime.now())
                .build());

        // Active goals
        List<Goal> activeGoals = goalRepository.findAll().stream()
                .filter(g -> g.getStatus() == Goal.GoalStatus.ACTIVE)
                .toList();
        summaries.add(DashboardSummary.builder()
                .metricKey("active_goals")
                .metricValue(BigDecimal.valueOf(activeGoals.size()))
                .dimension("system")
                .periodType("DAILY")
                .periodValue(periodValue)
                .computedAt(LocalDateTime.now())
                .build());

        // Total study plans
        long totalPlans = studyPlanRepository.count();
        summaries.add(DashboardSummary.builder()
                .metricKey("total_study_plans")
                .metricValue(BigDecimal.valueOf(totalPlans))
                .dimension("system")
                .periodType("DAILY")
                .periodValue(periodValue)
                .computedAt(LocalDateTime.now())
                .build());

        summaryRepository.saveAll(summaries);
        log.info("Nightly aggregation completed: {} metrics computed", summaries.size());
    }
}
