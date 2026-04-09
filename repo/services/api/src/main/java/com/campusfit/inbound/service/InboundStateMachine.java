package com.campusfit.inbound.service;

import com.campusfit.inbound.entity.*;
import com.campusfit.inbound.repository.*;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InboundStateMachine {

    private final InboundReceiptRepository receiptRepository;
    private final InboundLineRepository lineRepository;
    private final InboundStateHistoryRepository stateHistoryRepository;
    private final DiscrepancyRepository discrepancyRepository;
    private final PutawayTaskRepository putawayTaskRepository;

    public InboundStateMachine(InboundReceiptRepository receiptRepository,
                               InboundLineRepository lineRepository,
                               InboundStateHistoryRepository stateHistoryRepository,
                               DiscrepancyRepository discrepancyRepository,
                               PutawayTaskRepository putawayTaskRepository) {
        this.receiptRepository = receiptRepository;
        this.lineRepository = lineRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.discrepancyRepository = discrepancyRepository;
        this.putawayTaskRepository = putawayTaskRepository;
    }

    @Transactional
    public InboundReceipt transition(Long receiptId, InboundReceipt.ReceiptStatus targetState,
                                     Long userId, String reason) {
        InboundReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("InboundReceipt", receiptId));

        InboundReceipt.ReceiptStatus currentState = receipt.getStatus();

        // Validate transition
        validateTransition(receipt, currentState, targetState);

        // Record state history
        InboundStateHistory history = InboundStateHistory.builder()
                .receiptId(receiptId)
                .fromState(currentState.name())
                .toState(targetState.name())
                .changedBy(userId)
                .reason(reason)
                .build();
        stateHistoryRepository.save(history);

        // Apply transition
        receipt.setStatus(targetState);
        if (targetState == InboundReceipt.ReceiptStatus.POSTED) {
            receipt.setPostedBy(userId);
            receipt.setPostedAt(LocalDateTime.now());
        } else if (targetState == InboundReceipt.ReceiptStatus.UNPOSTED) {
            receipt.setUnpostedBy(userId);
            receipt.setUnpostedAt(LocalDateTime.now());
        }
        InboundReceipt saved = receiptRepository.save(receipt);

        // Auto-generate putaway tasks when entering PUTAWAY state (idempotent)
        if (targetState == InboundReceipt.ReceiptStatus.PUTAWAY) {
            generatePutawayTasksIfAbsent(receiptId);
        }

        return saved;
    }

    private void generatePutawayTasksIfAbsent(Long receiptId) {
        List<PutawayTask> existing = putawayTaskRepository.findByReceiptId(receiptId);
        if (!existing.isEmpty()) {
            return;
        }
        List<InboundLine> lines = lineRepository.findByReceiptId(receiptId);
        List<PutawayTask> tasks = new ArrayList<>();
        for (InboundLine line : lines) {
            if (line.getInspectionResult() == InboundLine.InspectionResult.PASS) {
                tasks.add(PutawayTask.builder()
                        .receiptId(receiptId)
                        .lineId(line.getId())
                        .suggestedLocation("ZONE-A-" + line.getItemCode())
                        .status(PutawayTask.TaskStatus.PENDING)
                        .build());
            }
        }
        putawayTaskRepository.saveAll(tasks);
    }

    private void validateTransition(InboundReceipt receipt, InboundReceipt.ReceiptStatus from,
                                    InboundReceipt.ReceiptStatus to) {
        // Any state can transition to REJECTED
        if (to == InboundReceipt.ReceiptStatus.REJECTED) {
            return;
        }

        List<InboundLine> lines = lineRepository.findByReceiptId(receipt.getId());

        switch (from) {
            case DRAFT:
                if (to != InboundReceipt.ReceiptStatus.RECEIVING) {
                    throw new BusinessException("DRAFT can only transition to RECEIVING");
                }
                if (lines.isEmpty()) {
                    throw new BusinessException("Receipt must have at least one line to begin receiving");
                }
                break;

            case RECEIVING:
                if (to != InboundReceipt.ReceiptStatus.INSPECTION) {
                    throw new BusinessException("RECEIVING can only transition to INSPECTION");
                }
                boolean allReceived = lines.stream()
                        .allMatch(l -> l.getReceivedQty().compareTo(BigDecimal.ZERO) > 0);
                if (!allReceived) {
                    throw new BusinessException("All lines must have received quantity before inspection");
                }
                break;

            case INSPECTION:
                if (to != InboundReceipt.ReceiptStatus.PUTAWAY) {
                    throw new BusinessException("INSPECTION can only transition to PUTAWAY");
                }
                boolean allInspected = lines.stream()
                        .allMatch(l -> l.getInspectionResult() != InboundLine.InspectionResult.PENDING);
                if (!allInspected) {
                    throw new BusinessException("All lines must be inspected before putaway");
                }
                // Check for unresolved discrepancies requiring supervisor
                List<Discrepancy> unresolvedSupervisor = discrepancyRepository
                        .findByReceiptIdAndSupervisorRequiredTrueAndResolvedByIsNull(receipt.getId());
                if (!unresolvedSupervisor.isEmpty()) {
                    throw new BusinessException("There are " + unresolvedSupervisor.size() +
                            " unresolved discrepancies requiring supervisor approval");
                }
                break;

            case PUTAWAY:
                if (to != InboundReceipt.ReceiptStatus.COMPLETED) {
                    throw new BusinessException("PUTAWAY can only transition to COMPLETED");
                }
                List<PutawayTask> allTasks = putawayTaskRepository.findByReceiptId(receipt.getId());
                if (allTasks.isEmpty()) {
                    throw new BusinessException("Putaway tasks must be generated and completed before marking receipt as COMPLETED");
                }
                List<PutawayTask> pendingTasks = putawayTaskRepository
                        .findByReceiptIdAndStatus(receipt.getId(), PutawayTask.TaskStatus.PENDING);
                if (!pendingTasks.isEmpty()) {
                    throw new BusinessException("All putaway tasks must be completed before marking receipt as COMPLETED");
                }
                break;

            case COMPLETED:
                if (to != InboundReceipt.ReceiptStatus.POSTED) {
                    throw new BusinessException("COMPLETED can only transition to POSTED");
                }
                break;

            case POSTED:
                if (to != InboundReceipt.ReceiptStatus.UNPOSTED) {
                    throw new BusinessException("POSTED can only transition to UNPOSTED");
                }
                break;

            default:
                throw new BusinessException("Cannot transition from " + from + " to " + to);
        }
    }
}
