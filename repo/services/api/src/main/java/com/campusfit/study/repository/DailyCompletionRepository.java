package com.campusfit.study.repository;

import com.campusfit.study.entity.DailyCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyCompletionRepository extends JpaRepository<DailyCompletion, Long> {

    List<DailyCompletion> findByPlanId(Long planId);

    List<DailyCompletion> findByPlanIdAndCompletedDate(Long planId, LocalDate completedDate);

    boolean existsByPlanIdAndCompletedDateAndCompleted(Long planId, LocalDate completedDate, boolean completed);
}
