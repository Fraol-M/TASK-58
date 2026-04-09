package com.campusfit.reporting.service;

import com.campusfit.auth.repository.UserRepository;
import com.campusfit.fitness.entity.Goal;
import com.campusfit.fitness.repository.CheckInRepository;
import com.campusfit.fitness.repository.GoalRepository;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.repository.DiscrepancyRepository;
import com.campusfit.inbound.repository.InboundReceiptRepository;
import com.campusfit.masterdata.repository.ImportJobRepository;
import com.campusfit.notification.repository.NotificationTargetRepository;
import com.campusfit.reporting.dto.DashboardResponse;
import com.campusfit.reporting.entity.DashboardSummary;
import com.campusfit.reporting.repository.DashboardSummaryRepository;
import com.campusfit.shared.security.UserPrincipal;
import com.campusfit.study.entity.Streak;
import com.campusfit.study.repository.StreakRepository;
import com.campusfit.study.repository.StudyPlanRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DashboardSummaryRepository summaryRepository;
    private final GoalRepository goalRepository;
    private final CheckInRepository checkInRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StreakRepository streakRepository;
    private final InboundReceiptRepository receiptRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final NotificationTargetRepository notificationTargetRepository;
    private final UserRepository userRepository;
    private final ImportJobRepository importJobRepository;

    public DashboardService(DashboardSummaryRepository summaryRepository,
                            GoalRepository goalRepository,
                            CheckInRepository checkInRepository,
                            StudyPlanRepository studyPlanRepository,
                            StreakRepository streakRepository,
                            InboundReceiptRepository receiptRepository,
                            DiscrepancyRepository discrepancyRepository,
                            NotificationTargetRepository notificationTargetRepository,
                            UserRepository userRepository,
                            ImportJobRepository importJobRepository) {
        this.summaryRepository = summaryRepository;
        this.goalRepository = goalRepository;
        this.checkInRepository = checkInRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.streakRepository = streakRepository;
        this.receiptRepository = receiptRepository;
        this.discrepancyRepository = discrepancyRepository;
        this.notificationTargetRepository = notificationTargetRepository;
        this.userRepository = userRepository;
        this.importJobRepository = importJobRepository;
    }

    @Cacheable(value = "dashboard", key = "#principal.id")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UserPrincipal principal) {
        Set<String> roles = principal.getRoles();

        if (roles.contains("ADMIN")) {
            return buildAdminDashboard();
        } else if (roles.contains("OPERATIONS_STAFF")) {
            return buildOpsDashboard();
        } else {
            return buildUserDashboard(principal.getId());
        }
    }

    private DashboardResponse buildUserDashboard(Long userId) {
        List<DashboardResponse.MetricEntry> metrics = new ArrayList<>();
        Map<String, Object> summary = new HashMap<>();

        // Fitness goal progress
        List<Goal> activeGoals = goalRepository.findByUserIdAndStatus(userId, Goal.GoalStatus.ACTIVE);
        int achievedCount = goalRepository.findByUserIdAndStatus(userId, Goal.GoalStatus.ACHIEVED).size();
        summary.put("fitnessGoals", activeGoals.size());
        summary.put("activeGoals", activeGoals.size());
        summary.put("achievedGoals", achievedCount);

        // Study plan stats
        int planCount = studyPlanRepository.findByUserId(userId).size();
        summary.put("activePlans", planCount);

        // Streaks
        List<Streak> streaks = streakRepository.findByUserId(userId);
        int maxStreak = streaks.stream().mapToInt(Streak::getLongestStreak).max().orElse(0);
        summary.put("studyStreak", maxStreak);
        summary.put("longestStreak", maxStreak);
        summary.put("recentCheckIns", checkInRepository.countByUserId(userId));
        summary.put("pendingNotifications", notificationTargetRepository.countByUserIdAndReadAtIsNull(userId));

        // Recent activity
        List<DashboardResponse.ActivityEntry> recentActivity = new ArrayList<>();
        if (achievedCount > 0) {
            recentActivity.add(DashboardResponse.ActivityEntry.builder()
                    .label("Goals achieved").value(String.valueOf(achievedCount)).build());
        }
        if (planCount > 0) {
            recentActivity.add(DashboardResponse.ActivityEntry.builder()
                    .label("Active study plans").value(String.valueOf(planCount)).build());
        }

        // Charts — goal progress percentages
        Map<String, List<Number>> charts = new HashMap<>();
        List<Number> goalProgress = activeGoals.stream()
                .map(g -> {
                    if (g.getTargetValue().compareTo(g.getStartValue()) == 0) return 100;
                    double progress = g.getCurrentValue().subtract(g.getStartValue())
                            .divide(g.getTargetValue().subtract(g.getStartValue()), 4, java.math.RoundingMode.HALF_UP)
                            .multiply(java.math.BigDecimal.valueOf(100)).doubleValue();
                    return (Number) Math.max(0, Math.min(100, progress));
                })
                .collect(Collectors.toList());
        charts.put("goalProgress", goalProgress);

        return DashboardResponse.builder()
                .userRole("REGULAR_USER")
                .metrics(metrics)
                .summary(summary)
                .recentActivity(recentActivity)
                .charts(charts)
                .build();
    }

    private DashboardResponse buildOpsDashboard() {
        Map<String, Object> summary = new HashMap<>();

        int totalActive = 0;
        int totalCompleted = 0;
        List<Number> receiptsByStatus = new ArrayList<>();

        for (InboundReceipt.ReceiptStatus status : InboundReceipt.ReceiptStatus.values()) {
            List<InboundReceipt> receipts = receiptRepository.findByStatus(status);
            int count = receipts.size();
            summary.put("receipts_" + status.name().toLowerCase(), count);
            receiptsByStatus.add(count);
            if (status == InboundReceipt.ReceiptStatus.RECEIVING || status == InboundReceipt.ReceiptStatus.INSPECTION
                    || status == InboundReceipt.ReceiptStatus.PUTAWAY) {
                totalActive += count;
            }
            if (status == InboundReceipt.ReceiptStatus.COMPLETED) {
                totalCompleted = count;
            }
        }

        summary.put("activeReceipts", totalActive);
        summary.put("pendingDiscrepancies", discrepancyRepository.countByResolvedByIsNull());
        summary.put("putawayQueueSize", summary.getOrDefault("receipts_putaway", 0));
        summary.put("operationsProcessed", totalCompleted);

        Map<String, List<Number>> charts = new HashMap<>();
        charts.put("receiptsByStatus", receiptsByStatus);

        return DashboardResponse.builder()
                .userRole("OPERATIONS_STAFF")
                .metrics(List.of())
                .summary(summary)
                .recentActivity(List.of())
                .charts(charts)
                .build();
    }

    private DashboardResponse buildAdminDashboard() {
        Map<String, Object> summary = new HashMap<>();
        List<DashboardResponse.MetricEntry> metrics = new ArrayList<>();

        List<DashboardSummary> allSummaries = summaryRepository.findAll();
        for (DashboardSummary ds : allSummaries) {
            metrics.add(DashboardResponse.MetricEntry.builder()
                    .key(ds.getMetricKey())
                    .value(ds.getMetricValue())
                    .dimension(ds.getDimension())
                    .periodType(ds.getPeriodType())
                    .periodValue(ds.getPeriodValue())
                    .build());
        }

        summary.put("totalUsers", userRepository.count());
        summary.put("activePlans", studyPlanRepository.count());
        summary.put("importJobs", importJobRepository.count());
        summary.put("totalMetrics", allSummaries.size());

        List<DashboardResponse.ActivityEntry> recentActivity = new ArrayList<>();
        recentActivity.add(DashboardResponse.ActivityEntry.builder()
                .label("Total metrics tracked").value(String.valueOf(allSummaries.size())).build());
        recentActivity.add(DashboardResponse.ActivityEntry.builder()
                .label("Registered users").value(String.valueOf(userRepository.count())).build());
        recentActivity.add(DashboardResponse.ActivityEntry.builder()
                .label("Import jobs processed").value(String.valueOf(importJobRepository.count())).build());

        Map<String, List<Number>> charts = new HashMap<>();
        charts.put("performance", List.of());

        return DashboardResponse.builder()
                .userRole("ADMIN")
                .metrics(metrics)
                .summary(summary)
                .recentActivity(recentActivity)
                .charts(charts)
                .build();
    }
}
