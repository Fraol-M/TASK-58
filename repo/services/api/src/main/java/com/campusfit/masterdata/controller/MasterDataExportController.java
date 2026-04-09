package com.campusfit.masterdata.controller;

import com.campusfit.masterdata.dto.MasterDataResponse;
import com.campusfit.masterdata.service.*;
import com.campusfit.shared.exception.BusinessException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/master-data/export")
public class MasterDataExportController {

    private final TermService termService;
    private final SchoolService schoolService;
    private final MajorService majorService;
    private final ClassService classService;
    private final CourseService courseService;

    public MasterDataExportController(TermService termService,
                                       SchoolService schoolService,
                                       MajorService majorService,
                                       ClassService classService,
                                       CourseService courseService) {
        this.termService = termService;
        this.schoolService = schoolService;
        this.majorService = majorService;
        this.classService = classService;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<byte[]> export(@RequestParam String entityType,
                                          @RequestParam(defaultValue = "xlsx") String format) {
        List<MasterDataResponse> items = getItems(entityType);

        if ("csv".equalsIgnoreCase(format)) {
            return exportToCsv(items, entityType);
        }
        return exportToExcel(items, entityType);
    }

    private String[] getHeaders(String entityType) {
        switch (entityType.toUpperCase()) {
            case "TERM":
                return new String[]{"Code", "Name", "Effective From", "Effective To", "Active", "Start Date", "End Date"};
            case "MAJOR":
                return new String[]{"Code", "Name", "Effective From", "Effective To", "Active", "School ID"};
            case "CLASS":
                return new String[]{"Code", "Name", "Effective From", "Effective To", "Active", "Major ID", "Year"};
            case "COURSE":
                return new String[]{"Code", "Name", "Effective From", "Effective To", "Active", "Class ID", "Term ID", "Credits"};
            default:
                return new String[]{"Code", "Name", "Effective From", "Effective To", "Active"};
        }
    }

    private String[] getRowValues(MasterDataResponse item, String entityType) {
        String code = item.getCode() != null ? item.getCode() : "";
        String name = item.getName() != null ? item.getName() : "";
        String effFrom = item.getEffectiveFrom() != null ? item.getEffectiveFrom().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String effTo = item.getEffectiveTo() != null ? item.getEffectiveTo().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String active = String.valueOf(item.isActive());

        switch (entityType.toUpperCase()) {
            case "TERM":
                return new String[]{code, name, effFrom, effTo, active,
                        item.getStartDate() != null ? item.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                        item.getEndDate() != null ? item.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : ""};
            case "MAJOR":
                return new String[]{code, name, effFrom, effTo, active,
                        item.getSchoolId() != null ? item.getSchoolId().toString() : ""};
            case "CLASS":
                return new String[]{code, name, effFrom, effTo, active,
                        item.getMajorId() != null ? item.getMajorId().toString() : "",
                        item.getYear() != null ? item.getYear().toString() : ""};
            case "COURSE":
                return new String[]{code, name, effFrom, effTo, active,
                        item.getClassId() != null ? item.getClassId().toString() : "",
                        item.getTermId() != null ? item.getTermId().toString() : "",
                        item.getCredits() != null ? item.getCredits().toString() : ""};
            default:
                return new String[]{code, name, effFrom, effTo, active};
        }
    }

    private ResponseEntity<byte[]> exportToCsv(List<MasterDataResponse> items, String entityType) {
        StringBuilder sb = new StringBuilder();
        String[] headers = getHeaders(entityType);
        sb.append(String.join(",", headers)).append("\n");

        for (MasterDataResponse item : items) {
            String[] values = getRowValues(item, entityType);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(escapeCsv(values[i]));
            }
            sb.append("\n");
        }

        String fileName = entityType.toLowerCase() + "_export.csv";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private ResponseEntity<byte[]> exportToExcel(List<MasterDataResponse> items, String entityType) {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(entityType);

            String[] headers = getHeaders(entityType);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < items.size(); i++) {
                String[] values = getRowValues(items.get(i), entityType);
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < values.length; j++) {
                    row.createCell(j).setCellValue(values[j]);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            String fileName = entityType.toLowerCase() + "_export.xlsx";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(out.toByteArray());

        } catch (Exception e) {
            throw new BusinessException("Failed to generate export: " + e.getMessage());
        }
    }

    private List<MasterDataResponse> getItems(String entityType) {
        switch (entityType.toUpperCase()) {
            case "TERM": return termService.getAll();
            case "SCHOOL": return schoolService.getAll();
            case "MAJOR": return majorService.getAll();
            case "CLASS": return classService.getAll();
            case "COURSE": return courseService.getAll();
            default: throw new BusinessException("Unknown entity type: " + entityType);
        }
    }
}
