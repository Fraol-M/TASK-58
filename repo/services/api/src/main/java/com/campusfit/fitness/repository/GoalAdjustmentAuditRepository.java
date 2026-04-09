package com.campusfit.fitness.repository;

import com.campusfit.fitness.entity.GoalAdjustmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalAdjustmentAuditRepository extends JpaRepository<GoalAdjustmentAudit, Long> {

    List<GoalAdjustmentAudit> findByGoalId(Long goalId);
}
