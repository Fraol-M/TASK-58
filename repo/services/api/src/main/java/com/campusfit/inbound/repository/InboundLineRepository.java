package com.campusfit.inbound.repository;

import com.campusfit.inbound.entity.InboundLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboundLineRepository extends JpaRepository<InboundLine, Long> {

    List<InboundLine> findByReceiptId(Long receiptId);
}
