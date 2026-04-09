package com.campusfit.inbound.repository;

import com.campusfit.inbound.entity.Discrepancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscrepancyRepository extends JpaRepository<Discrepancy, Long> {

    List<Discrepancy> findByReceiptId(Long receiptId);

    List<Discrepancy> findByLineId(Long lineId);

    List<Discrepancy> findByReceiptIdAndSupervisorRequiredTrueAndResolvedByIsNull(Long receiptId);

    long countByResolvedByIsNull();
}
