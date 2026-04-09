package com.campusfit.inbound.service;

import com.campusfit.inbound.entity.Discrepancy;
import com.campusfit.inbound.entity.InboundLine;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.repository.DiscrepancyRepository;
import com.campusfit.inbound.repository.InboundReceiptRepository;
import com.campusfit.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscrepancyServiceTest {

    @Mock
    private DiscrepancyRepository discrepancyRepository;

    @Mock
    private InboundReceiptRepository receiptRepository;

    private DiscrepancyService discrepancyService;

    @BeforeEach
    void setUp() {
        discrepancyService = new DiscrepancyService(discrepancyRepository, receiptRepository);
    }

    @Test
    void detectDiscrepancy_noDiscrepancy_whenWithinThreshold() {
        InboundLine line = InboundLine.builder()
                .id(1L)
                .receiptId(10L)
                .itemCode("ITEM-1")
                .itemName("Item 1")
                .expectedQty(new BigDecimal("100.00"))
                .receivedQty(new BigDecimal("99.50"))
                .build();

        Discrepancy result = discrepancyService.detectAndCreate(line);

        assertThat(result).isNull();
        verify(discrepancyRepository, never()).save(any());
    }

    @Test
    void detectDiscrepancy_createsRecord_whenVarianceOver2Percent() {
        InboundLine line = InboundLine.builder()
                .id(1L)
                .receiptId(10L)
                .itemCode("ITEM-1")
                .itemName("Item 1")
                .expectedQty(new BigDecimal("100.00"))
                .receivedQty(new BigDecimal("96.00"))
                .build();

        InboundReceipt receipt = InboundReceipt.builder()
                .id(10L).receiptNumber("REC-10")
                .status(InboundReceipt.ReceiptStatus.RECEIVING)
                .createdBy(1L).build();

        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any())).thenReturn(receipt);
        when(discrepancyRepository.save(any(Discrepancy.class))).thenAnswer(inv -> inv.getArgument(0));

        Discrepancy result = discrepancyService.detectAndCreate(line);

        assertThat(result).isNotNull();
        assertThat(result.getDiscrepancyType()).isEqualTo(Discrepancy.DiscrepancyType.QUANTITY);
        assertThat(result.getExpectedValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getActualValue()).isEqualByComparingTo(new BigDecimal("96.00"));
        assertThat(result.getVariancePercent()).isGreaterThan(new BigDecimal("2.00"));
    }

    @Test
    void detectDiscrepancy_createsRecord_whenVarianceOver5Units() {
        InboundLine line = InboundLine.builder()
                .id(1L)
                .receiptId(10L)
                .itemCode("ITEM-1")
                .itemName("Item 1")
                .expectedQty(new BigDecimal("1000.00"))
                .receivedQty(new BigDecimal("994.00"))
                .build();

        InboundReceipt receipt = InboundReceipt.builder()
                .id(10L).receiptNumber("REC-10")
                .status(InboundReceipt.ReceiptStatus.RECEIVING)
                .createdBy(1L).build();

        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any())).thenReturn(receipt);
        when(discrepancyRepository.save(any(Discrepancy.class))).thenAnswer(inv -> inv.getArgument(0));

        Discrepancy result = discrepancyService.detectAndCreate(line);

        assertThat(result).isNotNull();
        // 6 units difference > 5 unit threshold
        verify(discrepancyRepository).save(any(Discrepancy.class));
    }

    @Test
    void detectDiscrepancy_requiresSupervisor_whenThresholdExceeded() {
        InboundLine line = InboundLine.builder()
                .id(1L)
                .receiptId(10L)
                .itemCode("ITEM-1")
                .itemName("Item 1")
                .expectedQty(new BigDecimal("100.00"))
                .receivedQty(new BigDecimal("90.00"))
                .build();

        InboundReceipt receipt = InboundReceipt.builder()
                .id(10L).receiptNumber("REC-10")
                .status(InboundReceipt.ReceiptStatus.RECEIVING)
                .createdBy(1L).build();

        when(receiptRepository.findById(10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any())).thenReturn(receipt);
        when(discrepancyRepository.save(any(Discrepancy.class))).thenAnswer(inv -> inv.getArgument(0));

        Discrepancy result = discrepancyService.detectAndCreate(line);

        assertThat(result).isNotNull();
        assertThat(result.isSupervisorRequired()).isTrue();
        assertThat(receipt.isSupervisorApprovalRequired()).isTrue();
    }

    @Test
    void resolveDiscrepancy_requiresReasonCode() {
        Discrepancy discrepancy = Discrepancy.builder()
                .id(1L)
                .receiptId(10L)
                .lineId(1L)
                .discrepancyType(Discrepancy.DiscrepancyType.QUANTITY)
                .expectedValue(new BigDecimal("100"))
                .actualValue(new BigDecimal("90"))
                .variancePercent(new BigDecimal("10.00"))
                .supervisorRequired(true)
                .build();

        when(discrepancyRepository.findById(1L)).thenReturn(Optional.of(discrepancy));
        when(discrepancyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Fixed: resolve(receiptId, discrepancyId, resolvedBy, reasonCode, notes) — 5 args
        Discrepancy result = discrepancyService.resolve(10L, 1L, 42L, Discrepancy.ReasonCode.SHORT_SHIP, "Supplier shortage");

        assertThat(result.getReasonCode()).isEqualTo(Discrepancy.ReasonCode.SHORT_SHIP);
        assertThat(result.getResolvedBy()).isEqualTo(42L);
        assertThat(result.getResolvedAt()).isNotNull();
        assertThat(result.getNotes()).isEqualTo("Supplier shortage");
    }

    @Test
    void resolveDiscrepancy_wrongReceiptId_throws() {
        Discrepancy discrepancy = Discrepancy.builder()
                .id(1L)
                .receiptId(10L)   // belongs to receipt 10
                .lineId(1L)
                .discrepancyType(Discrepancy.DiscrepancyType.QUANTITY)
                .expectedValue(new BigDecimal("100"))
                .actualValue(new BigDecimal("90"))
                .variancePercent(new BigDecimal("10.00"))
                .supervisorRequired(false)
                .build();

        when(discrepancyRepository.findById(1L)).thenReturn(Optional.of(discrepancy));

        // receiptId=99 does not match discrepancy.receiptId=10
        assertThatThrownBy(() -> discrepancyService.resolve(99L, 1L, 42L, Discrepancy.ReasonCode.OTHER, "Wrong receipt"))
                .isInstanceOf(com.campusfit.shared.exception.BusinessException.class)
                .hasMessageContaining("Discrepancy does not belong to the specified receipt");
    }

    @Test
    void resolveDiscrepancy_alreadyResolved_throws() {
        Discrepancy discrepancy = Discrepancy.builder()
                .id(2L)
                .receiptId(10L)
                .lineId(1L)
                .discrepancyType(Discrepancy.DiscrepancyType.QUANTITY)
                .expectedValue(new BigDecimal("100"))
                .actualValue(new BigDecimal("90"))
                .variancePercent(new BigDecimal("10.00"))
                .resolvedBy(5L)   // already resolved
                .build();

        when(discrepancyRepository.findById(2L)).thenReturn(Optional.of(discrepancy));

        assertThatThrownBy(() -> discrepancyService.resolve(10L, 2L, 42L, Discrepancy.ReasonCode.OTHER, "Duplicate"))
                .isInstanceOf(com.campusfit.shared.exception.BusinessException.class)
                .hasMessageContaining("Discrepancy has already been resolved");
    }
}
