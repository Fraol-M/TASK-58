package com.campusfit.inbound.controller;

import com.campusfit.inbound.dto.*;
import com.campusfit.inbound.entity.Discrepancy;
import com.campusfit.inbound.entity.InboundReceipt;
import com.campusfit.inbound.entity.PutawayTask;
import com.campusfit.inbound.service.*;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound/receipts")
public class InboundReceiptController {

    private final InboundReceiptService receiptService;
    private final InboundStateMachine stateMachine;
    private final InboundLineService lineService;
    private final PutawayService putawayService;
    private final DiscrepancyService discrepancyService;

    public InboundReceiptController(InboundReceiptService receiptService,
                                    InboundStateMachine stateMachine,
                                    InboundLineService lineService,
                                    PutawayService putawayService,
                                    DiscrepancyService discrepancyService) {
        this.receiptService = receiptService;
        this.stateMachine = stateMachine;
        this.lineService = lineService;
        this.putawayService = putawayService;
        this.discrepancyService = discrepancyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InboundReceiptResponse>> create(
            @Valid @RequestBody InboundReceiptRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        InboundReceiptResponse response = receiptService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InboundReceiptResponse>>> list(
            @RequestParam(required = false) InboundReceipt.ReceiptStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Page<InboundReceiptResponse> paged = receiptService.list(status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InboundReceiptResponse>> getById(@PathVariable Long id) {
        InboundReceiptResponse response = receiptService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/discrepancies")
    public ResponseEntity<ApiResponse<List<com.campusfit.inbound.entity.Discrepancy>>> getDiscrepancies(
            @PathVariable Long id) {
        List<com.campusfit.inbound.entity.Discrepancy> discrepancies = discrepancyService.getByReceiptId(id);
        return ResponseEntity.ok(ApiResponse.ok(discrepancies));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<ApiResponse<Void>> transition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        stateMachine.transition(id, request.getTargetState(), userId, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/lines")
    public ResponseEntity<ApiResponse<InboundLineResponse>> addLine(
            @PathVariable Long id,
            @Valid @RequestBody InboundLineRequest request) {
        InboundLineResponse response = lineService.addLine(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<InboundLineResponse>> receiveLine(
            @PathVariable Long id,
            @Valid @RequestBody ReceiveLineRequest request) {
        InboundLineResponse response = lineService.updateReceivedQty(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/inspection")
    public ResponseEntity<ApiResponse<InboundLineResponse>> inspect(
            @PathVariable Long id,
            @Valid @RequestBody InspectionRequest request) {
        InboundLineResponse response = lineService.updateInspection(
                id, request.getLineId(), request.getInspectedQty(),
                request.getResult(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/putaway")
    public ResponseEntity<ApiResponse<List<PutawayTask>>> getPutawayTasks(@PathVariable Long id) {
        List<PutawayTask> tasks = putawayService.getByReceiptId(id);
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }

    @PostMapping("/{id}/putaway")
    public ResponseEntity<ApiResponse<List<PutawayTask>>> putaway(
            @PathVariable Long id,
            @RequestBody(required = false) PutawayRequest request) {
        if (request != null && request.getTaskId() != null) {
            Long userId = SecurityContextHelper.getCurrentUserId();
            PutawayTask task = putawayService.completeTask(id, request.getTaskId(), userId, request.getActualLocation());
            return ResponseEntity.ok(ApiResponse.ok(List.of(task)));
        } else {
            List<PutawayTask> tasks = putawayService.generateTasks(id);
            return ResponseEntity.ok(ApiResponse.ok(tasks));
        }
    }

    @PreAuthorize("hasAnyRole('OPERATIONS_STAFF', 'ADMIN')")
    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<Void>> post(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        stateMachine.transition(id, InboundReceipt.ReceiptStatus.POSTED, userId, "Posted to inventory");
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/unpost")
    public ResponseEntity<ApiResponse<Void>> unpost(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        stateMachine.transition(id, InboundReceipt.ReceiptStatus.UNPOSTED, userId, "Unposted from inventory");
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/supervisor-review")
    public ResponseEntity<ApiResponse<Void>> supervisorReview(
            @PathVariable Long id,
            @Valid @RequestBody SupervisorReviewRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        // Separation of duty: the reviewer must not be the receipt creator
        InboundReceiptResponse receipt = receiptService.getById(id);
        if (receipt.getCreatedBy() != null && receipt.getCreatedBy().equals(userId)) {
            throw new com.campusfit.shared.exception.BusinessException(
                    "Supervisor review requires a different user than the receipt creator");
        }

        discrepancyService.resolve(id, request.getDiscrepancyId(), userId,
                request.getReasonCode(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
