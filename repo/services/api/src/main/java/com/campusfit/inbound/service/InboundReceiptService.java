package com.campusfit.inbound.service;

import com.campusfit.inbound.dto.InboundLineResponse;
import com.campusfit.inbound.dto.InboundReceiptRequest;
import com.campusfit.inbound.dto.InboundReceiptResponse;
import com.campusfit.inbound.entity.InboundLine;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.repository.InboundLineRepository;
import com.campusfit.inbound.repository.InboundReceiptRepository;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InboundReceiptService {

    private final InboundReceiptRepository receiptRepository;
    private final InboundLineRepository lineRepository;

    public InboundReceiptService(InboundReceiptRepository receiptRepository,
                                 InboundLineRepository lineRepository) {
        this.receiptRepository = receiptRepository;
        this.lineRepository = lineRepository;
    }

    @Transactional
    public InboundReceiptResponse create(Long userId, InboundReceiptRequest request) {
        String receiptNumber = "RCV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        InboundReceipt receipt = InboundReceipt.builder()
                .receiptNumber(receiptNumber)
                .receiptType(request.getReceiptType())
                .referenceNumber(request.getReferenceNumber())
                .supplierName(request.getSupplierName())
                .status(InboundReceipt.ReceiptStatus.DRAFT)
                .expectedDate(request.getExpectedDate())
                .createdBy(userId)
                .build();

        InboundReceipt saved = receiptRepository.save(receipt);
        return toResponse(saved, List.of());
    }

    @Transactional(readOnly = true)
    public InboundReceiptResponse getById(Long receiptId) {
        InboundReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("InboundReceipt", receiptId));

        List<InboundLine> lines = lineRepository.findByReceiptId(receiptId);
        return toResponse(receipt, lines);
    }

    @Transactional(readOnly = true)
    public List<InboundReceiptResponse> list(InboundReceipt.ReceiptStatus status) {
        List<InboundReceipt> receipts;
        if (status != null) {
            receipts = receiptRepository.findByStatus(status);
        } else {
            receipts = receiptRepository.findAll();
        }

        return receipts.stream()
                .map(r -> {
                    List<InboundLine> lines = lineRepository.findByReceiptId(r.getId());
                    return toResponse(r, lines);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<InboundReceiptResponse> list(InboundReceipt.ReceiptStatus status, Pageable pageable) {
        Page<InboundReceipt> receipts;
        if (status != null) {
            receipts = receiptRepository.findByStatus(status, pageable);
        } else {
            receipts = receiptRepository.findAll(pageable);
        }

        return receipts.map(r -> {
            List<InboundLine> lines = lineRepository.findByReceiptId(r.getId());
            return toResponse(r, lines);
        });
    }

    private InboundReceiptResponse toResponse(InboundReceipt r, List<InboundLine> lines) {
        List<InboundLineResponse> lineResponses = lines.stream()
                .map(this::toLineResponse)
                .collect(Collectors.toList());

        return InboundReceiptResponse.builder()
                .id(r.getId())
                .receiptNumber(r.getReceiptNumber())
                .receiptType(r.getReceiptType())
                .referenceNumber(r.getReferenceNumber())
                .supplierName(r.getSupplierName())
                .status(r.getStatus())
                .expectedDate(r.getExpectedDate())
                .receivedDate(r.getReceivedDate())
                .createdBy(r.getCreatedBy())
                .supervisorApprovalRequired(r.isSupervisorApprovalRequired())
                .supervisorApprovedBy(r.getSupervisorApprovedBy())
                .postedBy(r.getPostedBy())
                .postedAt(r.getPostedAt())
                .unpostedBy(r.getUnpostedBy())
                .unpostedAt(r.getUnpostedAt())
                .lines(lineResponses)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private InboundLineResponse toLineResponse(InboundLine l) {
        return InboundLineResponse.builder()
                .id(l.getId())
                .receiptId(l.getReceiptId())
                .itemCode(l.getItemCode())
                .itemName(l.getItemName())
                .expectedQty(l.getExpectedQty())
                .receivedQty(l.getReceivedQty())
                .inspectedQty(l.getInspectedQty())
                .unitCost(l.getUnitCost())
                .inspectionResult(l.getInspectionResult())
                .inspectionNotes(l.getInspectionNotes())
                .build();
    }
}
