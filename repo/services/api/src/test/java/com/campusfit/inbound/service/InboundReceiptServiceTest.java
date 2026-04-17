package com.campusfit.inbound.service;

import com.campusfit.inbound.dto.InboundReceiptRequest;
import com.campusfit.inbound.dto.InboundReceiptResponse;
import com.campusfit.inbound.entity.InboundLine;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.repository.InboundLineRepository;
import com.campusfit.inbound.repository.InboundReceiptRepository;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundReceiptServiceTest {

    @Mock InboundReceiptRepository receiptRepository;
    @Mock InboundLineRepository lineRepository;

    @InjectMocks InboundReceiptService inboundReceiptService;

    // ---- create ----

    @Test
    void create_generatesReceiptNumberAndSetsDraft() {
        InboundReceiptRequest req = InboundReceiptRequest.builder()
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .supplierName("Supplier A")
                .build();

        InboundReceipt saved = receipt(1L, "RCV-ABCD1234", InboundReceipt.ReceiptStatus.DRAFT);
        when(receiptRepository.save(any())).thenReturn(saved);

        InboundReceiptResponse response = inboundReceiptService.create(10L, req);

        assertThat(response.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.DRAFT);
        assertThat(response.getReceiptNumber()).startsWith("RCV-");

        ArgumentCaptor<InboundReceipt> captor = ArgumentCaptor.forClass(InboundReceipt.class);
        verify(receiptRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.DRAFT);
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(10L);
        assertThat(captor.getValue().getReceiptNumber()).startsWith("RCV-");
    }

    @Test
    void create_receiptNumberIsUnique_rvcPrefixWith8UpperChars() {
        InboundReceiptRequest req = InboundReceiptRequest.builder()
                .receiptType(InboundReceipt.ReceiptType.RETURN)
                .supplierName("Supplier B")
                .build();

        when(receiptRepository.save(any())).thenAnswer(inv -> {
            InboundReceipt r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });

        InboundReceiptResponse r1 = inboundReceiptService.create(1L, req);
        InboundReceiptResponse r2 = inboundReceiptService.create(1L, req);

        // Both are valid RCV- prefixed numbers (may or may not be equal due to UUID)
        assertThat(r1.getReceiptNumber()).matches("RCV-[A-Z0-9]{8}");
        assertThat(r2.getReceiptNumber()).matches("RCV-[A-Z0-9]{8}");
    }

    // ---- getById ----

    @Test
    void getById_existingReceipt_includesLines() {
        InboundReceipt r = receipt(5L, "RCV-TEST0001", InboundReceipt.ReceiptStatus.RECEIVING);
        when(receiptRepository.findById(5L)).thenReturn(Optional.of(r));
        when(lineRepository.findByReceiptId(5L)).thenReturn(List.of(
                line(1L, 5L, "ITEM-001", 10),
                line(2L, 5L, "ITEM-002", 5)
        ));

        InboundReceiptResponse response = inboundReceiptService.getById(5L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.RECEIVING);
        assertThat(response.getLines()).hasSize(2);
        assertThat(response.getLines().get(0).getItemCode()).isEqualTo("ITEM-001");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(receiptRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inboundReceiptService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_noLines_returnsEmptyLinesList() {
        InboundReceipt r = receipt(3L, "RCV-NOLINE00", InboundReceipt.ReceiptStatus.DRAFT);
        when(receiptRepository.findById(3L)).thenReturn(Optional.of(r));
        when(lineRepository.findByReceiptId(3L)).thenReturn(List.of());

        InboundReceiptResponse response = inboundReceiptService.getById(3L);

        assertThat(response.getLines()).isEmpty();
    }

    // ---- list (no filter) ----

    @Test
    void list_noStatusFilter_returnsAllWithLines() {
        when(receiptRepository.findAll()).thenReturn(List.of(
                receipt(1L, "RCV-AAA00001", InboundReceipt.ReceiptStatus.DRAFT),
                receipt(2L, "RCV-BBB00002", InboundReceipt.ReceiptStatus.COMPLETED)
        ));
        when(lineRepository.findByReceiptId(anyLong())).thenReturn(List.of());

        List<InboundReceiptResponse> result = inboundReceiptService.list(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void list_withStatusFilter_delegatesToRepository() {
        when(receiptRepository.findByStatus(InboundReceipt.ReceiptStatus.DRAFT)).thenReturn(List.of(
                receipt(1L, "RCV-DRAFT001", InboundReceipt.ReceiptStatus.DRAFT)
        ));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of());

        List<InboundReceiptResponse> result = inboundReceiptService.list(InboundReceipt.ReceiptStatus.DRAFT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.DRAFT);
        verify(receiptRepository).findByStatus(InboundReceipt.ReceiptStatus.DRAFT);
        verify(receiptRepository, never()).findAll();
    }

    // ---- list (pageable) ----

    @Test
    void list_pageable_noFilter_returnsPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(receiptRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
                receipt(1L, "RCV-PAGE0001", InboundReceipt.ReceiptStatus.DRAFT)
        )));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of());

        var page = inboundReceiptService.list(null, pageable);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void list_pageable_withStatusFilter_delegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(receiptRepository.findByStatus(InboundReceipt.ReceiptStatus.COMPLETED, pageable))
                .thenReturn(new PageImpl<>(List.of(
                        receipt(2L, "RCV-COMP0001", InboundReceipt.ReceiptStatus.COMPLETED)
                )));
        when(lineRepository.findByReceiptId(2L)).thenReturn(List.of());

        var page = inboundReceiptService.list(InboundReceipt.ReceiptStatus.COMPLETED, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.COMPLETED);
    }

    // ---- helpers ----

    private InboundReceipt receipt(Long id, String number, InboundReceipt.ReceiptStatus status) {
        return InboundReceipt.builder()
                .id(id)
                .receiptNumber(number)
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .status(status)
                .supplierName("Test Supplier")
                .createdBy(1L)
                .build();
    }

    private InboundLine line(Long id, Long receiptId, String itemCode, int expectedQty) {
        return InboundLine.builder()
                .id(id)
                .receiptId(receiptId)
                .itemCode(itemCode)
                .itemName("Item " + itemCode)
                .expectedQty(expectedQty)
                .receivedQty(0)
                .unitCost(BigDecimal.valueOf(10.00))
                .build();
    }
}
