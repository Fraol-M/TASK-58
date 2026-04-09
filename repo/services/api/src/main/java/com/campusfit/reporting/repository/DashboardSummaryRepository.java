package com.campusfit.reporting.repository;

import com.campusfit.reporting.entity.DashboardSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardSummaryRepository extends JpaRepository<DashboardSummary, Long> {

    List<DashboardSummary> findByMetricKey(String metricKey);

    List<DashboardSummary> findByPeriodTypeAndPeriodValue(String periodType, String periodValue);

    List<DashboardSummary> findByDimension(String dimension);
}
