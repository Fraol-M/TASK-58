package com.campusfit.masterdata.repository;

import com.campusfit.masterdata.entity.ChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, Long> {
    List<ChangeHistory> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<ChangeHistory> findByEntityType(String entityType);
}
