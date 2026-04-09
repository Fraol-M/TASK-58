package com.campusfit.masterdata.repository;

import com.campusfit.masterdata.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByCode(String code);
    boolean existsByCode(String code);
    List<Major> findBySchoolId(Long schoolId);
}
