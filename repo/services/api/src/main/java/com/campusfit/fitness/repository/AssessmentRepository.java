package com.campusfit.fitness.repository;

import com.campusfit.fitness.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByUserId(Long userId);

    Optional<Assessment> findTopByUserIdOrderByAssessmentDateDescIdDesc(Long userId);

    void deleteByUserId(Long userId);
}
