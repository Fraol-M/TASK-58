package com.campusfit.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SlowQueryLogService {

    private static final Logger log = LoggerFactory.getLogger(SlowQueryLogService.class);

    private final JdbcTemplate jdbcTemplate;

    public SlowQueryLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns slow query performance data from MySQL performance_schema.
     * Falls back to table-level metrics if performance_schema queries fail (e.g., H2 in tests).
     */
    public List<Map<String, Object>> getPerformanceMetrics() {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT DIGEST_TEXT AS queryDigest,
                           COUNT_STAR AS callCount,
                           ROUND(AVG_TIMER_WAIT / 1000000, 0) AS avgDurationMs,
                           ROUND(MAX_TIMER_WAIT / 1000000, 0) AS maxDurationMs,
                           SUM_ROWS_EXAMINED AS totalRowsExamined,
                           FIRST_SEEN AS firstSeen,
                           LAST_SEEN AS lastSeen
                    FROM performance_schema.events_statements_summary_by_digest
                    WHERE SCHEMA_NAME = DATABASE()
                      AND DIGEST_TEXT IS NOT NULL
                    ORDER BY AVG_TIMER_WAIT DESC
                    LIMIT 25
                    """,
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("query", rs.getString("queryDigest"));
                        row.put("callCount", rs.getLong("callCount"));
                        row.put("avgDurationMs", rs.getLong("avgDurationMs"));
                        row.put("maxDurationMs", rs.getLong("maxDurationMs"));
                        row.put("totalRowsExamined", rs.getLong("totalRowsExamined"));
                        row.put("firstSeen", rs.getString("firstSeen"));
                        row.put("lastSeen", rs.getString("lastSeen"));
                        return row;
                    });
        } catch (Exception e) {
            log.warn("performance_schema query failed (possibly running on H2): {}", e.getMessage());
            return getTableMetrics();
        }
    }

    private List<Map<String, Object>> getTableMetrics() {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT TABLE_NAME AS tableName,
                           TABLE_ROWS AS estimatedRows,
                           ROUND(DATA_LENGTH / 1024, 0) AS dataSizeKb,
                           ROUND(INDEX_LENGTH / 1024, 0) AS indexSizeKb
                    FROM information_schema.TABLES
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_TYPE = 'BASE TABLE'
                    ORDER BY TABLE_ROWS DESC
                    """,
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("tableName", rs.getString("tableName"));
                        row.put("estimatedRows", rs.getLong("estimatedRows"));
                        row.put("dataSizeKb", rs.getLong("dataSizeKb"));
                        row.put("indexSizeKb", rs.getLong("indexSizeKb"));
                        row.put("status", "OK");
                        return row;
                    });
        } catch (Exception e) {
            log.warn("Table metrics query failed: {}", e.getMessage());
            return List.of();
        }
    }
}
