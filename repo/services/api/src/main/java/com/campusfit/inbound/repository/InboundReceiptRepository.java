package com.campusfit.inbound.repository;

import com.campusfit.inbound.entity.InboundReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboundReceiptRepository extends JpaRepository<InboundReceipt, Long> {

    Optional<InboundReceipt> findByReceiptNumber(String receiptNumber);

    List<InboundReceipt> findByStatus(InboundReceipt.ReceiptStatus status);

    Page<InboundReceipt> findByStatus(InboundReceipt.ReceiptStatus status, Pageable pageable);

    List<InboundReceipt> findByCreatedBy(Long createdBy);
}
