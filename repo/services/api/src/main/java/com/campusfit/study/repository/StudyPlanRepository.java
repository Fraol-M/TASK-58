package com.campusfit.study.repository;

import com.campusfit.study.entity.StudyPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByUserId(Long userId);

    Page<StudyPlan> findByUserId(Long userId, Pageable pageable);

    List<StudyPlan> findByUserIdAndStatus(Long userId, StudyPlan.PlanStatus status);

    List<StudyPlan> findByCourseId(Long courseId);

    List<StudyPlan> findByTermId(Long termId);

    List<StudyPlan> findBySchoolId(Long schoolId);

    List<StudyPlan> findByMajorId(Long majorId);

    List<StudyPlan> findByClassId(Long classId);

    void deleteByUserId(Long userId);
}
