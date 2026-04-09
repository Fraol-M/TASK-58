package com.campusfit.inbound.dto;

import com.campusfit.inbound.entity.InboundReceipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundReceiptResponse {

    private Long id;
    private String receiptNumber;
    private InboundReceipt.ReceiptType receiptType;
    private String referenceNumber;
    private String supplierName;
    private InboundReceipt.ReceiptStatus status;
    private LocalDate expectedDate;
    private LocalDate receivedDate;
    private Long createdBy;
    private boolean supervisorApprovalRequired;
    private Long supervisorApprovedBy;
    private Long postedBy;
    private LocalDateTime postedAt;
    private Long unpostedBy;
    private LocalDateTime unpostedAt;
    private List<InboundLineResponse> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
