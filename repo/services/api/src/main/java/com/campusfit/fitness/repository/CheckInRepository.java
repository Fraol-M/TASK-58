package com.campusfit.fitness.repository;

import com.campusfit.fitness.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByGoalIdOrderByWeekNumberDesc(Long goalId);

    long countByGoalIdAndWeekNumberGreaterThan(Long goalId, int weekNumber);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}
