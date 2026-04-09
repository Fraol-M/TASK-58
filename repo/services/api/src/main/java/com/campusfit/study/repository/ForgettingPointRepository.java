package com.campusfit.study.repository;

import com.campusfit.study.entity.ForgettingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ForgettingPointRepository extends JpaRepository<ForgettingPoint, Long> {

    List<ForgettingPoint> findByPlanId(Long planId);

    List<ForgettingPoint> findByPlanIdAndNextReviewDateLessThanEqual(Long planId, LocalDate date);
}
