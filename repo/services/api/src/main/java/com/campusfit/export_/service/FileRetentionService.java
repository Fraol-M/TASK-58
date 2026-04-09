package com.campusfit.export_.service;

import com.campusfit.export_.entity.ExportJob;
import com.campusfit.export_.repository.ExportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileRetentionService {

    private static final Logger log = LoggerFactory.getLogger(FileRetentionService.class);

    private final ExportJobRepository exportJobRepository;

    public FileRetentionService(ExportJobRepository exportJobRepository) {
        this.exportJobRepository = exportJobRepository;
    }

    /**
     * Runs daily to clean up expired export files.
     */
    @Scheduled(fixedRate = 86400000) // 24 hours
    @Transactional
    public void cleanupExpiredFiles() {
        LocalDateTime now = LocalDateTime.now();
        List<ExportJob> expiredJobs = exportJobRepository
                .findByStatusAndExpiresAtBefore(ExportJob.JobStatus.COMPLETED, now);

        for (ExportJob job : expiredJobs) {
            if (job.getFilePath() != null) {
                try {
                    Path filePath = Paths.get(job.getFilePath());
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Deleted expired export file for job #{}", job.getId());
                    }
                } catch (IOException e) {
                    log.error("Failed to delete expired export file for job #{}: {}", job.getId(), e.getMessage());
                }
            }

            job.setStatus(ExportJob.JobStatus.FAILED);
            exportJobRepository.save(job);
        }

        if (!expiredJobs.isEmpty()) {
            log.info("Cleaned up {} expired export jobs", expiredJobs.size());
        }
    }
}
