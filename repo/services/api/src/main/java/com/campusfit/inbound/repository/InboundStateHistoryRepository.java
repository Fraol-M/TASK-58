package com.campusfit.inbound.repository;

import com.campusfit.inbound.entity.InboundStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboundStateHistoryRepository extends JpaRepository<InboundStateHistory, Long> {

    List<InboundStateHistory> findByReceiptIdOrderByCreatedAtDesc(Long receiptId);
}
