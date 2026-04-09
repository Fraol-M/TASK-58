package com.campusfit.study.repository;

import com.campusfit.study.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByUserIdAndPlanId(Long userId, Long planId);

    List<Streak> findByUserId(Long userId);
}
