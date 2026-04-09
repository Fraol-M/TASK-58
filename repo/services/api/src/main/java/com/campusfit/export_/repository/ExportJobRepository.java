package com.campusfit.export_.repository;

import com.campusfit.export_.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    List<ExportJob> findByUserId(Long userId);

    List<ExportJob> findByStatusAndExpiresAtBefore(ExportJob.JobStatus status, LocalDateTime before);
}
