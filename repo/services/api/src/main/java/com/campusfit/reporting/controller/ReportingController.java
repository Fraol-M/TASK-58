package com.campusfit.reporting.controller;

import com.campusfit.reporting.dto.DashboardResponse;
import com.campusfit.reporting.service.DashboardService;
import com.campusfit.reporting.service.SlowQueryLogService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import com.campusfit.shared.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportingController {

    private final DashboardService dashboardService;
    private final SlowQueryLogService slowQueryLogService;

    public ReportingController(DashboardService dashboardService,
                               SlowQueryLogService slowQueryLogService) {
        this.dashboardService = dashboardService;
        this.slowQueryLogService = slowQueryLogService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        UserPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DashboardResponse response = dashboardService.getDashboard(principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/performance")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPerformance() {
        List<Map<String, Object>> metrics = slowQueryLogService.getPerformanceMetrics();
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }
}
