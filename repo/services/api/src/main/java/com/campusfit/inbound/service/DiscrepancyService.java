package com.campusfit.inbound.service;

import com.campusfit.inbound.entity.Discrepancy;
import com.campusfit.inbound.entity.InboundLine;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.repository.DiscrepancyRepository;
import com.campusfit.inbound.repository.InboundReceiptRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiscrepancyService {

    private static final BigDecimal VARIANCE_THRESHOLD_PERCENT = new BigDecimal("2.00");
    private static final BigDecimal VARIANCE_THRESHOLD_UNITS = new BigDecimal("5.00");

    private final DiscrepancyRepository discrepancyRepository;
    private final InboundReceiptRepository receiptRepository;

    public DiscrepancyService(DiscrepancyRepository discrepancyRepository,
                              InboundReceiptRepository receiptRepository) {
        this.discrepancyRepository = discrepancyRepository;
        this.receiptRepository = receiptRepository;
    }

    /**
     * Auto-detect discrepancy when received qty differs from expected qty.
     * Creates a discrepancy record if variance > 2% or > 5 units.
     * Sets supervisorRequired=true if those thresholds are exceeded.
     */
    @Transactional
    public Discrepancy detectAndCreate(InboundLine line) {
        BigDecimal expected = line.getExpectedQty();
        BigDecimal actual = line.getReceivedQty();
        BigDecimal difference = actual.subtract(expected).abs();

        if (expected.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal variancePercent = difference
                .divide(expected, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        boolean exceedsPercentThreshold = variancePercent.compareTo(VARIANCE_THRESHOLD_PERCENT) > 0;
        boolean exceedsUnitThreshold = difference.compareTo(VARIANCE_THRESHOLD_UNITS) > 0;

        if (!exceedsPercentThreshold && !exceedsUnitThreshold) {
            return null; // No discrepancy
        }

        boolean supervisorRequired = exceedsPercentThreshold || exceedsUnitThreshold;

        Discrepancy discrepancy = Discrepancy.builder()
                .receiptId(line.getReceiptId())
                .lineId(line.getId())
                .discrepancyType(Discrepancy.DiscrepancyType.QUANTITY)
                .expectedValue(expected)
                .actualValue(actual)
                .variancePercent(variancePercent)
                .supervisorRequired(supervisorRequired)
                .build();

        // Flag the receipt as needing supervisor approval
        if (supervisorRequired) {
            InboundReceipt receipt = receiptRepository.findById(line.getReceiptId())
                    .orElseThrow(() -> new ResourceNotFoundException("InboundReceipt", line.getReceiptId()));
            receipt.setSupervisorApprovalRequired(true);
            receiptRepository.save(receipt);
        }

        return discrepancyRepository.save(discrepancy);
    }

    @Transactional
    public Discrepancy resolve(Long receiptId, Long discrepancyId, Long resolvedBy, Discrepancy.ReasonCode reasonCode, String notes) {
        Discrepancy discrepancy = discrepancyRepository.findById(discrepancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Discrepancy", discrepancyId));

        if (!discrepancy.getReceiptId().equals(receiptId)) {
            throw new BusinessException("Discrepancy does not belong to the specified receipt");
        }

        if (discrepancy.getResolvedBy() != null) {
            throw new BusinessException("Discrepancy has already been resolved");
        }

        discrepancy.setResolvedBy(resolvedBy);
        discrepancy.setResolvedAt(LocalDateTime.now());
        discrepancy.setReasonCode(reasonCode);
        discrepancy.setNotes(notes);

        return discrepancyRepository.save(discrepancy);
    }

    @Transactional(readOnly = true)
    public List<Discrepancy> getByReceiptId(Long receiptId) {
        return discrepancyRepository.findByReceiptId(receiptId);
    }
}
