package com.campusfit.masterdata.repository;

import com.campusfit.masterdata.entity.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {
    Optional<AcademicClass> findByCode(String code);
    boolean existsByCode(String code);
    List<AcademicClass> findByMajorId(Long majorId);
}
