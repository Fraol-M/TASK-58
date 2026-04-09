package com.campusfit.fitness.repository;

import com.campusfit.fitness.entity.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserId(Long userId);

    Page<Goal> findByUserId(Long userId, Pageable pageable);

    List<Goal> findByUserIdAndStatus(Long userId, Goal.GoalStatus status);

    void deleteByUserId(Long userId);
}
