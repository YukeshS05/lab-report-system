package com.lab;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REPORT GENERATOR CLASS — formats processed LabResults into a readable report.
 *
 * Responsibilities:
 *   1. Generate a full patient report (all results for one patient)
 *   2. Generate a summary report (just the counts/statistics)
 *   3. Generate a flagged-only report (ABNORMAL and INVALID results only)
 *
 * IMPORTANT: This class only FORMATS data — it never validates or modifies results.
 * Always run LabReportProcessor.processBatch() BEFORE calling any method here.
 */
public class ReportGenerator {

    // ─── Formatting Constants ─────────────────────────────────────────────────
    private static final String BORDER     = "=".repeat(65);
    private static final String DIVIDER    = "-".repeat(65);
    private static final String DATE_FMT   = "yyyy-MM-dd HH:mm:ss";
    private static final String COL_HEADER =
        String.format("  %-22s %-8s %-8s %-18s %s",
            "TEST NAME", "VALUE", "UNIT", "RANGE", "STATUS");


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 1 — Full Patient Report
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a complete formatted report for a single patient.
     *
     * Lists every result with its value, unit, reference range, and status.
     * Flags ABNORMAL results with an asterisk (*).
     * Appends a summary line at the bottom.
     *
     * @param patientId the patient's ID (used in the report header)
     * @param results   list of already-processed LabResults for this patient
     * @return the full report as a single formatted String
     * @throws IllegalArgumentException if patientId is blank or results is null
     */
    public String generatePatientReport(String patientId, List<LabResult> results) {

        // ── Input Validation ──────────────────────────────────────────────────
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or blank");
        }
        if (results == null) {
            throw new IllegalArgumentException("Results list must not be null");
        }

        StringBuilder report = new StringBuilder();
        String timestamp = getCurrentTimestamp();

        // ── Header ────────────────────────────────────────────────────────────
        report.append(BORDER).append("\n");
        report.append(centerText("DIAGNOSTIC LAB REPORT", 65)).append("\n");
        report.append(centerText("Patient ID : " + patientId.toUpperCase(), 65)).append("\n");
        report.append(centerText("Generated  : " + timestamp, 65)).append("\n");
        report.append(BORDER).append("\n\n");

        // ── Column Headers ────────────────────────────────────────────────────
        report.append(COL_HEADER).append("\n");
        report.append("  ").append(DIVIDER).append("\n");

        // ── Result Rows ───────────────────────────────────────────────────────
        if (results.isEmpty()) {
            report.append("  No results found for this patient.\n");
        } else {
            for (LabResult result : results) {
                report.append(formatResultRow(result)).append("\n");
            }
        }

        // ── Footer / Summary ──────────────────────────────────────────────────
        report.append("  ").append(DIVIDER).append("\n");
        report.append(buildSummaryLine(results)).append("\n");

        // Only show the footnote if there are any abnormal results
        if (countByStatus(results, ResultStatus.ABNORMAL) > 0 ||
            countByStatus(results, ResultStatus.INVALID)  > 0) {
            report.append("  * = requires immediate attention\n");
        }

        report.append(BORDER).append("\n");
        return report.toString();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 2 — Summary Report (statistics only, no individual rows)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a brief statistics-only report — no individual result rows.
     * Useful when you only need the counts, not the full breakdown.
     *
     * @param results list of already-processed LabResults
     * @return formatted summary string
     */
    public String generateSummaryReport(List<LabResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results list must not be null");
        }

        StringBuilder report = new StringBuilder();

        report.append(BORDER).append("\n");
        report.append(centerText("LAB RESULTS SUMMARY", 65)).append("\n");
        report.append(centerText("Generated : " + getCurrentTimestamp(), 65)).append("\n");
        report.append(BORDER).append("\n\n");

        int total    = results.size();
        int normal   = countByStatus(results, ResultStatus.NORMAL);
        int abnormal = countByStatus(results, ResultStatus.ABNORMAL);
        int invalid  = countByStatus(results, ResultStatus.INVALID);
        int unvalidated = countUnvalidated(results);

        report.append(String.format("  Total results     : %d%n", total));
        report.append(String.format("  Normal            : %d%n", normal));
        report.append(String.format("  Abnormal          : %d%n", abnormal));
        report.append(String.format("  Invalid           : %d%n", invalid));
        report.append(String.format("  Not yet validated : %d%n", unvalidated));
        report.append("\n");

        // Show a simple health indicator
        if (total == 0) {
            report.append("  Status: No results to evaluate.\n");
        } else if (abnormal == 0 && invalid == 0 && unvalidated == 0) {
            report.append("  Status: ALL RESULTS NORMAL - no issues detected.\n");
        } else if (abnormal > 0 || invalid > 0) {
            report.append("  Status: ATTENTION REQUIRED - review flagged results.\n");
        }

        report.append("\n").append(BORDER).append("\n");
        return report.toString();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 3 — Flagged Results Report (ABNORMAL + INVALID only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a report containing ONLY the results that need attention.
     * Filters out NORMAL results entirely.
     * Useful for a doctor who only needs to review problem cases.
     *
     * @param results list of already-processed LabResults
     * @return formatted report of flagged results only
     */
    public String generateFlaggedReport(List<LabResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results list must not be null");
        }

        StringBuilder report = new StringBuilder();

        report.append(BORDER).append("\n");
        report.append(centerText("FLAGGED RESULTS REPORT", 65)).append("\n");
        report.append(centerText("Generated : " + getCurrentTimestamp(), 65)).append("\n");
        report.append(BORDER).append("\n\n");

        report.append(COL_HEADER).append("\n");
        report.append("  ").append(DIVIDER).append("\n");

        boolean anyFlagged = false;

        for (LabResult result : results) {
            // Only include ABNORMAL and INVALID results
            if (result != null &&
                (result.getStatus() == ResultStatus.ABNORMAL ||
                 result.getStatus() == ResultStatus.INVALID)) {
                report.append(formatResultRow(result)).append("\n");
                anyFlagged = true;
            }
        }

        if (!anyFlagged) {
            report.append("  No flagged results — all values are within normal range.\n");
        }

        report.append("  ").append(DIVIDER).append("\n");
        report.append(buildSummaryLine(results)).append("\n");
        report.append(BORDER).append("\n");

        return report.toString();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 4 — Private Helper Methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Formats a single LabResult into a table row string.
     * Appends " *" marker for ABNORMAL and INVALID results.
     *
     * Example output:
     *   "  Blood Sugar            110.5    mg/dL    70.0  - 100.0    ABNORMAL  *"
     */
    private String formatResultRow(LabResult result) {
        if (result == null) return "  [null result - skipped]";

        // Build the range string e.g. "70.0 - 100.0"
        String range = String.format("%.1f - %.1f",
            result.getMinRange(), result.getMaxRange());

        // Get the status string (or "PENDING" if not yet validated)
        String statusStr = result.getStatus() != null
            ? result.getStatus().toString()
            : "PENDING";

        // Add a flag marker for results needing attention
        String flag = (result.getStatus() == ResultStatus.ABNORMAL ||
                       result.getStatus() == ResultStatus.INVALID)
            ? "  *" : "";

        return String.format("  %-22s %-8.1f %-8s %-18s %s%s",
            result.getTestName(),
            result.getValue(),
            result.getUnit(),
            range,
            statusStr,
            flag);
    }

    /**
     * Builds the summary footer line showing counts of each status.
     * Example: "  SUMMARY: Total: 4 | Normal: 2 | Abnormal: 1 | Invalid: 1"
     */
    private String buildSummaryLine(List<LabResult> results) {
        int total    = results.size();
        int normal   = countByStatus(results, ResultStatus.NORMAL);
        int abnormal = countByStatus(results, ResultStatus.ABNORMAL);
        int invalid  = countByStatus(results, ResultStatus.INVALID);

        return String.format(
            "  SUMMARY:  Total: %d  |  Normal: %d  |  Abnormal: %d  |  Invalid: %d",
            total, normal, abnormal, invalid);
    }

    /**
     * Counts results matching a specific status in the list.
     */
    private int countByStatus(List<LabResult> results, ResultStatus status) {
        int count = 0;
        for (LabResult r : results) {
            if (r != null && r.getStatus() == status) count++;
        }
        return count;
    }

    /**
     * Counts results that have not yet been validated (status is null).
     */
    private int countUnvalidated(List<LabResult> results) {
        int count = 0;
        for (LabResult r : results) {
            if (r != null && r.getStatus() == null) count++;
        }
        return count;
    }

    /**
     * Returns the current date and time as a formatted string.
     * Example: "2026-04-05 23:45:00"
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern(DATE_FMT));
    }

    /**
     * Centers a piece of text within a given total width by padding with spaces.
     * Example: centerText("HELLO", 20) → "       HELLO       "
     */
    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int totalPadding = width - text.length();
        int leftPad  = totalPadding / 2;
        return " ".repeat(leftPad) + text;
    }
}
