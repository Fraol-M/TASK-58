package com.campusfit.inbound.service;

import com.campusfit.inbound.entity.*;
import com.campusfit.inbound.repository.*;
import com.campusfit.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundStateMachineTest {

    @Mock
    private InboundReceiptRepository receiptRepository;
    @Mock
    private InboundLineRepository lineRepository;
    @Mock
    private InboundStateHistoryRepository stateHistoryRepository;
    @Mock
    private DiscrepancyRepository discrepancyRepository;
    @Mock
    private PutawayTaskRepository putawayTaskRepository;

    private InboundStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new InboundStateMachine(receiptRepository, lineRepository,
                stateHistoryRepository, discrepancyRepository, putawayTaskRepository);
    }

    private InboundReceipt createReceipt(Long id, InboundReceipt.ReceiptStatus status) {
        return InboundReceipt.builder()
                .id(id)
                .receiptNumber("REC-" + id)
                .receiptType(InboundReceipt.ReceiptType.PURCHASE)
                .status(status)
                .createdBy(1L)
                .build();
    }

    private InboundLine createLine(Long id, Long receiptId, BigDecimal received) {
        return InboundLine.builder()
                .id(id)
                .receiptId(receiptId)
                .itemCode("ITEM-" + id)
                .itemName("Item " + id)
                .expectedQty(new BigDecimal("100"))
                .receivedQty(received)
                .inspectionResult(InboundLine.InspectionResult.PASS)
                .build();
    }

    @Test
    void transition_draftToReceiving_success() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.DRAFT);
        InboundLine line = createLine(1L, 1L, BigDecimal.ZERO);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.RECEIVING, 1L, "Starting");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.RECEIVING);
    }

    @Test
    void transition_receivingToInspection_success() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.RECEIVING);
        InboundLine line = createLine(1L, 1L, new BigDecimal("100"));

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.INSPECTION, 1L, "All received");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.INSPECTION);
    }

    @Test
    void transition_inspectionToPutaway_success() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.INSPECTION);
        InboundLine line = createLine(1L, 1L, new BigDecimal("100"));

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));
        when(discrepancyRepository.findByReceiptIdAndSupervisorRequiredTrueAndResolvedByIsNull(1L))
                .thenReturn(List.of());
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.PUTAWAY, 1L, "Inspected");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.PUTAWAY);
    }

    @Test
    void transition_putawayToCompleted_success() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.PUTAWAY);
        PutawayTask completedTask = PutawayTask.builder()
                .id(1L).receiptId(1L).lineId(1L)
                .status(PutawayTask.TaskStatus.COMPLETED)
                .build();

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        // Must have at least one task (allTasks check)
        when(putawayTaskRepository.findByReceiptId(1L)).thenReturn(List.of(completedTask));
        // No pending tasks
        when(putawayTaskRepository.findByReceiptIdAndStatus(1L, PutawayTask.TaskStatus.PENDING))
                .thenReturn(List.of());
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.COMPLETED, 1L, "Done");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.COMPLETED);
    }

    @Test
    void transition_putawayToCompleted_noTasksGenerated_fails() {
        // Blocker fix: completing without ever generating putaway tasks must be rejected
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.PUTAWAY);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(putawayTaskRepository.findByReceiptId(1L)).thenReturn(List.of()); // no tasks at all

        assertThatThrownBy(() -> stateMachine.transition(1L, InboundReceipt.ReceiptStatus.COMPLETED, 1L, "Skip putaway"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Putaway tasks must be generated and completed");
    }

    @Test
    void transition_inspectionToPutaway_autoGeneratesTasks() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.INSPECTION);
        InboundLine line = createLine(1L, 1L, new BigDecimal("100"));

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));
        when(discrepancyRepository.findByReceiptIdAndSupervisorRequiredTrueAndResolvedByIsNull(1L))
                .thenReturn(List.of());
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);
        // No existing tasks — generation path
        when(putawayTaskRepository.findByReceiptId(1L)).thenReturn(List.of());
        when(putawayTaskRepository.saveAll(any())).thenReturn(List.of(
                PutawayTask.builder().receiptId(1L).lineId(1L).status(PutawayTask.TaskStatus.PENDING).build()
        ));

        stateMachine.transition(1L, InboundReceipt.ReceiptStatus.PUTAWAY, 1L, "Inspected");

        verify(putawayTaskRepository).saveAll(any());
    }

    @Test
    void transition_anyToRejected_success() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.RECEIVING);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.REJECTED, 1L, "Quality issue");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.REJECTED);
    }

    @Test
    void transition_draftToCompleted_fails() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.DRAFT);
        InboundLine line = createLine(1L, 1L, BigDecimal.ZERO);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));

        assertThatThrownBy(() -> stateMachine.transition(1L, InboundReceipt.ReceiptStatus.COMPLETED, 1L, "Skip"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT can only transition to RECEIVING");
    }

    @Test
    void transition_completedToDraft_fails() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.COMPLETED);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> stateMachine.transition(1L, InboundReceipt.ReceiptStatus.DRAFT, 1L, "Revert"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("COMPLETED can only transition to POSTED");
    }

    @Test
    void transition_receivingToPutaway_fails() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.RECEIVING);
        InboundLine line = createLine(1L, 1L, new BigDecimal("100"));

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));

        assertThatThrownBy(() -> stateMachine.transition(1L, InboundReceipt.ReceiptStatus.PUTAWAY, 1L, "Skip inspection"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RECEIVING can only transition to INSPECTION");
    }

    @Test
    void transition_completedToPosted_succeeds() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.COMPLETED);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.POSTED, 1L, "Post");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.POSTED);
    }

    @Test
    void transition_postedToUnposted_succeeds() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.POSTED);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        InboundReceipt result = stateMachine.transition(1L, InboundReceipt.ReceiptStatus.UNPOSTED, 1L, "Unpost");

        assertThat(result.getStatus()).isEqualTo(InboundReceipt.ReceiptStatus.UNPOSTED);
    }

    @Test
    void transition_completedToInvalid_fails() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.COMPLETED);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> stateMachine.transition(1L, InboundReceipt.ReceiptStatus.DRAFT, 1L, "Revert"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("COMPLETED can only transition to POSTED");
    }

    @Test
    void transition_createsStateHistory() {
        InboundReceipt receipt = createReceipt(1L, InboundReceipt.ReceiptStatus.DRAFT);
        InboundLine line = createLine(1L, 1L, BigDecimal.ZERO);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(lineRepository.findByReceiptId(1L)).thenReturn(List.of(line));
        when(stateHistoryRepository.save(any())).thenReturn(InboundStateHistory.builder().build());
        when(receiptRepository.save(any())).thenReturn(receipt);

        stateMachine.transition(1L, InboundReceipt.ReceiptStatus.RECEIVING, 42L, "Begin receiving");

        ArgumentCaptor<InboundStateHistory> historyCaptor = ArgumentCaptor.forClass(InboundStateHistory.class);
        verify(stateHistoryRepository).save(historyCaptor.capture());

        InboundStateHistory history = historyCaptor.getValue();
        assertThat(history.getReceiptId()).isEqualTo(1L);
        assertThat(history.getFromState()).isEqualTo("DRAFT");
        assertThat(history.getToState()).isEqualTo("RECEIVING");
        assertThat(history.getChangedBy()).isEqualTo(42L);
        assertThat(history.getReason()).isEqualTo("Begin receiving");
    }
}
