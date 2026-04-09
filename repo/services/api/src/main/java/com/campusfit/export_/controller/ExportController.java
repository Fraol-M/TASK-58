package com.campusfit.export_.controller;

import com.campusfit.export_.dto.ExportRequest;
import com.campusfit.export_.dto.ExportResponse;
import com.campusfit.export_.entity.DeletionRequest;
import com.campusfit.export_.service.AccountDeletionService;
import com.campusfit.export_.service.ExportService;
import com.campusfit.shared.dto.ApiResponse;
import com.campusfit.shared.security.SecurityContextHelper;
import com.campusfit.auth.entity.User;
import com.campusfit.auth.repository.UserRepository;
import com.campusfit.auth.service.PasswordService;
import com.campusfit.export_.service.AccountImportService;
import jakarta.validation.Valid;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExportService exportService;
    private final AccountDeletionService accountDeletionService;
    private final AccountImportService accountImportService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public ExportController(ExportService exportService,
                            AccountDeletionService accountDeletionService,
                            AccountImportService accountImportService,
                            UserRepository userRepository,
                            PasswordService passwordService) {
        this.exportService = exportService;
        this.accountDeletionService = accountDeletionService;
        this.accountImportService = accountImportService;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @PostMapping("/exports/account")
    public ResponseEntity<ApiResponse<ExportResponse>> requestExport(@Valid @RequestBody ExportRequest request) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        ExportResponse response = exportService.createExportJob(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/exports")
    public ResponseEntity<ApiResponse<List<ExportResponse>>> listExports() {
        Long userId = SecurityContextHelper.getCurrentUserId();
        List<ExportResponse> exports = exportService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(exports));
    }

    @GetMapping("/exports/{id}")
    public ResponseEntity<ApiResponse<ExportResponse>> getExport(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        ExportResponse response = exportService.getById(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/exports/{id}/download")
    public ResponseEntity<Resource> downloadExport(@PathVariable Long id) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        Path filePath = exportService.getExportFilePath(id, userId);
        Resource resource = new PathResource(filePath);

        String fileName = filePath.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping("/imports/account")
    public ResponseEntity<ApiResponse<String>> importAccount(@RequestBody java.util.Map<String, Object> data) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        String result = accountImportService.importAccountData(userId, data);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping(value = "/imports/account/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> importAccountFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {
        Long userId = SecurityContextHelper.getCurrentUserId();
        try {
            byte[] fileBytes = file.getBytes();
            java.util.Map<String, Object> data = exportService.decryptExportFile(fileBytes, password);
            String result = accountImportService.importAccountData(userId, data);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (java.io.IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> requestDeletion(
            @RequestBody(required = false) java.util.Map<String, String> body) {
        Long userId = SecurityContextHelper.getCurrentUserId();

        // Verify password before processing deletion
        String password = body != null ? body.get("password") : null;
        if (password == null || password.isBlank()) {
            throw new com.campusfit.shared.exception.BusinessException("Password confirmation is required for account deletion");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.campusfit.shared.exception.ResourceNotFoundException("User", userId));
        if (!passwordService.matches(password, user.getPasswordHash())) {
            throw new com.campusfit.shared.exception.BusinessException("Incorrect password");
        }

        accountDeletionService.requestDeletion(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
