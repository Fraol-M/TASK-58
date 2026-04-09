package com.campusfit.masterdata.service;

import com.campusfit.masterdata.dto.ImportJobResponse;
import com.campusfit.masterdata.dto.MasterDataRequest;
import com.campusfit.masterdata.entity.ImportError;
import com.campusfit.masterdata.entity.ImportJob;
import com.campusfit.masterdata.repository.ImportErrorRepository;
import com.campusfit.masterdata.repository.ImportJobRepository;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterDataImportService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataImportService.class);
    private static final DateTimeFormatter DATE_FORMAT_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_FORMAT_US = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final ImportJobRepository importJobRepository;
    private final ImportErrorRepository importErrorRepository;
    private final TermService termService;
    private final SchoolService schoolService;
    private final MajorService majorService;
    private final ClassService classService;
    private final CourseService courseService;

    public MasterDataImportService(ImportJobRepository importJobRepository,
                                   ImportErrorRepository importErrorRepository,
                                   TermService termService,
                                   SchoolService schoolService,
                                   MajorService majorService,
                                   ClassService classService,
                                   CourseService courseService) {
        this.importJobRepository = importJobRepository;
        this.importErrorRepository = importErrorRepository;
        this.termService = termService;
        this.schoolService = schoolService;
        this.majorService = majorService;
        this.classService = classService;
        this.courseService = courseService;
    }

    @Transactional
    public ImportJobResponse processImport(MultipartFile file, String entityType, Long userId) {
        ImportJob job = ImportJob.builder()
                .fileName(file.getOriginalFilename())
                .entityType(entityType)
                .status(ImportJob.JobStatus.PROCESSING)
                .uploadedBy(userId)
                .build();
        job = importJobRepository.save(job);

        List<ImportError> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        boolean isExcel = fileName.endsWith(".xlsx") || fileName.endsWith(".xls");

        try {
            List<String[]> rows;
            if (isExcel) {
                rows = parseExcel(file.getInputStream());
            } else {
                rows = parseCsv(file.getInputStream());
            }

            for (int i = 0; i < rows.size(); i++) {
                int rowNum = i + 2; // 1-based, skip header
                totalRows++;
                String[] fields = rows.get(i);
                try {
                    if (fields.length < 2) {
                        errors.add(buildError(job.getId(), rowNum, "line",
                                "Insufficient fields - expected at least code,name", String.join(",", fields)));
                        continue;
                    }

                    String code = fields[0].trim();
                    String name = fields[1].trim();

                    if (code.isEmpty()) {
                        errors.add(buildError(job.getId(), rowNum, "code", "Code is required", code));
                        continue;
                    }

                    if (name.isEmpty()) {
                        errors.add(buildError(job.getId(), rowNum, "name", "Name is required", name));
                        continue;
                    }

                    persistEntity(entityType, code, name, fields, userId);
                    successCount++;

                } catch (Exception e) {
                    errors.add(buildError(job.getId(), rowNum, "line",
                            "Error: " + e.getMessage(), String.join(",", fields)));
                }
            }
        } catch (Exception e) {
            log.error("Import failed for job {}: {}", job.getId(), e.getMessage());
            job.setStatus(ImportJob.JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            importJobRepository.save(job);
            return toResponse(job, errors);
        }

        if (!errors.isEmpty()) {
            importErrorRepository.saveAll(errors);
        }

        job.setTotalRows(totalRows);
        job.setSuccessCount(successCount);
        job.setErrorCount(errors.size());
        job.setStatus(ImportJob.JobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        importJobRepository.save(job);

        log.info("Import job {} completed: {} success, {} errors out of {} rows",
                job.getId(), successCount, errors.size(), totalRows);

        return toResponse(job, errors);
    }

    private List<String[]> parseCsv(InputStream inputStream) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                .withSkipLines(1)  // skip header
                .build()) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (row.length > 0 && !String.join("", row).trim().isEmpty()) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private List<String[]> parseExcel(InputStream inputStream) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            boolean headerSkipped = false;
            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                int lastCell = row.getLastCellNum();
                if (lastCell <= 0) continue;
                String[] fields = new String[lastCell];
                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    fields[i] = formatter.formatCellValue(cell);
                }
                if (fields.length > 0 && !fields[0].trim().isEmpty()) {
                    rows.add(fields);
                }
            }
        }
        return rows;
    }

    // Export column layout: Code(0), Name(1), EffectiveFrom(2), EffectiveTo(3), Active(4), then entity-specific at 5+
    private void persistEntity(String entityType, String code, String name,
                                String[] fields, Long userId) {
        LocalDate effectiveFrom;
        if (fields.length > 2 && !fields[2].trim().isEmpty()) {
            effectiveFrom = parseDate(fields[2].trim(), "effectiveFrom");
        } else {
            effectiveFrom = LocalDate.now();
        }

        LocalDate effectiveTo = null;
        if (fields.length > 3 && !fields[3].trim().isEmpty()) {
            effectiveTo = parseDate(fields[3].trim(), "effectiveTo");
        }

        MasterDataRequest request = MasterDataRequest.builder()
                .code(code)
                .name(name)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .build();

        switch (entityType.toUpperCase()) {
            case "TERM":
                // columns 5=startDate, 6=endDate
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    request.setStartDate(parseDate(fields[5].trim(), "startDate"));
                }
                if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                    request.setEndDate(parseDate(fields[6].trim(), "endDate"));
                }
                termService.create(request, userId);
                break;
            case "SCHOOL":
                schoolService.create(request, userId);
                break;
            case "MAJOR":
                // column 5=schoolId
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    request.setSchoolId(parseId(fields[5].trim(), "schoolId"));
                }
                majorService.create(request, userId);
                break;
            case "CLASS":
                // columns 5=majorId, 6=year
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    request.setMajorId(parseId(fields[5].trim(), "majorId"));
                }
                if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                    try {
                        request.setYear(Integer.parseInt(fields[6].trim()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid year: '" + fields[6].trim() + "'");
                    }
                }
                classService.create(request, userId);
                break;
            case "COURSE":
                // columns 5=classId, 6=termId, 7=credits
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    request.setClassId(parseId(fields[5].trim(), "classId"));
                }
                if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                    request.setTermId(parseId(fields[6].trim(), "termId"));
                }
                if (fields.length > 7 && !fields[7].trim().isEmpty()) {
                    try {
                        request.setCredits(Integer.parseInt(fields[7].trim()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid credits: '" + fields[7].trim() + "'");
                    }
                }
                courseService.create(request, userId);
                break;
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        // Accept ISO format (yyyy-MM-dd) first, then US format (MM/dd/yyyy)
        try {
            return LocalDate.parse(value, DATE_FORMAT_ISO);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT_US);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format for " + fieldName + ": '" + value + "' (expected yyyy-MM-dd or MM/dd/yyyy)");
        }
    }

    private Long parseId(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid numeric ID for " + fieldName + ": '" + value + "'");
        }
    }

    @Transactional(readOnly = true)
    public ImportJobResponse getJobById(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("ImportJob", jobId));
        List<ImportError> errors = importErrorRepository.findByJobId(jobId);
        return toResponse(job, errors);
    }

    private ImportError buildError(Long jobId, int rowNum, String field, String message, String rawValue) {
        return ImportError.builder()
                .jobId(jobId)
                .rowNumber(rowNum)
                .field(field)
                .message(message)
                .rawValue(rawValue)
                .build();
    }

    private ImportJobResponse toResponse(ImportJob job, List<ImportError> errors) {
        List<ImportJobResponse.ImportErrorResponse> errorResponses = errors.stream()
                .map(e -> ImportJobResponse.ImportErrorResponse.builder()
                        .rowNumber(e.getRowNumber())
                        .field(e.getField())
                        .message(e.getMessage())
                        .rawValue(e.getRawValue())
                        .build())
                .collect(Collectors.toList());

        return ImportJobResponse.builder()
                .id(job.getId())
                .fileName(job.getFileName())
                .entityType(job.getEntityType())
                .totalRows(job.getTotalRows())
                .successCount(job.getSuccessCount())
                .errorCount(job.getErrorCount())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .errors(errorResponses)
                .build();
    }
}
