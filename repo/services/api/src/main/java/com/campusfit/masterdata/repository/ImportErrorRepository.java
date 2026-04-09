package com.campusfit.masterdata.repository;

import com.campusfit.masterdata.entity.ImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
    List<ImportError> findByJobId(Long jobId);
}
