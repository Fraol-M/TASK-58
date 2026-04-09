package com.campusfit.inbound.service;

import com.campusfit.inbound.dto.InboundLineRequest;
import com.campusfit.inbound.dto.InboundLineResponse;
import com.campusfit.inbound.dto.ReceiveLineRequest;
import com.campusfit.inbound.entity.InboundLine;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.repository.InboundLineRepository;
import com.campusfit.inbound.repository.InboundReceiptRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class InboundLineService {

    private final InboundLineRepository lineRepository;
    private final InboundReceiptRepository receiptRepository;
    private final DiscrepancyService discrepancyService;

    public InboundLineService(InboundLineRepository lineRepository,
                              InboundReceiptRepository receiptRepository,
                              DiscrepancyService discrepancyService) {
        this.lineRepository = lineRepository;
        this.receiptRepository = receiptRepository;
        this.discrepancyService = discrepancyService;
    }

    @Transactional
    public InboundLineResponse addLine(Long receiptId, InboundLineRequest request) {
        InboundReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("InboundReceipt", receiptId));

        if (receipt.getStatus() != InboundReceipt.ReceiptStatus.DRAFT) {
            throw new BusinessException("Can only add lines to receipts in DRAFT status");
        }

        InboundLine line = InboundLine.builder()
                .receiptId(receiptId)
                .itemCode(request.getItemCode())
                .itemName(request.getItemName())
                .expectedQty(request.getExpectedQty())
                .unitCost(request.getUnitCost() != null ? request.getUnitCost() : BigDecimal.ZERO)
                .build();

        InboundLine saved = lineRepository.save(line);
        return toResponse(saved);
    }

    @Transactional
    public InboundLineResponse updateReceivedQty(Long receiptId, ReceiveLineRequest request) {
        InboundLine line = lineRepository.findById(request.getLineId())
                .orElseThrow(() -> new ResourceNotFoundException("InboundLine", request.getLineId()));

        if (!line.getReceiptId().equals(receiptId)) {
            throw new BusinessException("Line does not belong to the specified receipt");
        }

        line.setReceivedQty(request.getReceivedQty());
        InboundLine saved = lineRepository.save(line);

        // Auto-detect discrepancy
        discrepancyService.detectAndCreate(saved);

        return toResponse(saved);
    }

    @Transactional
    public InboundLineResponse updateInspection(Long receiptId, Long lineId, BigDecimal inspectedQty,
                                                 InboundLine.InspectionResult result, String notes) {
        InboundLine line = lineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("InboundLine", lineId));

        if (!line.getReceiptId().equals(receiptId)) {
            throw new BusinessException("Line does not belong to the specified receipt");
        }

        line.setInspectedQty(inspectedQty);
        line.setInspectionResult(result);
        line.setInspectionNotes(notes);

        InboundLine saved = lineRepository.save(line);
        return toResponse(saved);
    }

    private InboundLineResponse toResponse(InboundLine l) {
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
