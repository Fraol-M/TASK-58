package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.service.TermService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/admin/terms")
public class TermController {

    private final TermService termService;

    public TermController(TermService termService) {
        this.termService = termService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MasterDataResponse>> create(@Valid @RequestBody MasterDataRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        MasterDataResponse response = termService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MasterDataResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Page<MasterDataResponse> paged = termService.getAll(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterDataResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(termService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterDataResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody MasterDataRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(termService.update(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        termService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
