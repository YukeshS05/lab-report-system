package com.lab;

import java.util.ArrayList;
import java.util.List;

/**
 * SERVICE CLASS — contains all the business logic for processing lab results.
 *
 * Responsibilities:
 *   1. Validate a single LabResult (check for bad/missing data)
 *   2. Determine if the result is NORMAL, ABNORMAL, or INVALID
 *   3. Process a batch (list) of results all at once
 *   4. Filter results by status for easy reporting
 *
 * This class does NOT hold any data of its own.
 * It only contains methods that operate on LabResult objects.
 */
public class LabReportProcessor {

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 1 — Core Validation Logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates and processes a single LabResult.
     * Sets the result's status to NORMAL, ABNORMAL, or INVALID.
     *
     * This is the most important method in the entire project.
     *
     * @param result the LabResult to validate (must not be null)
     * @throws IllegalArgumentException if result itself is null
     */
    public void processResult(LabResult result) {

        // Guard: the result object itself must not be null
        if (result == null) {
            throw new IllegalArgumentException("LabResult cannot be null");
        }

        // ── Check 1: Is the test name missing or blank? ──────────────────────
        if (result.getTestName() == null || result.getTestName().trim().isEmpty()) {
            result.setStatus(ResultStatus.INVALID);
            return; // stop here — no point checking further
        }

        // ── Check 2: Is the patient ID missing or blank? ─────────────────────
        if (result.getPatientId() == null || result.getPatientId().trim().isEmpty()) {
            result.setStatus(ResultStatus.INVALID);
            return;
        }

        // ── Check 3: Is the value a real number? (not NaN or Infinity) ───────
        // Double.isNaN()      → catches values like 0.0 / 0.0
        // Double.isInfinite() → catches values like 1.0 / 0.0
        if (Double.isNaN(result.getValue()) || Double.isInfinite(result.getValue())) {
            result.setStatus(ResultStatus.INVALID);
            return;
        }

        // ── Check 4: Is the value negative? ──────────────────────────────────
        // Lab values like blood sugar, hemoglobin etc. can never be negative
        if (result.getValue() < 0) {
            result.setStatus(ResultStatus.INVALID);
            return;
        }

        // ── Check 5: Is the reference range itself valid? ─────────────────────
        // minRange must be less than maxRange — otherwise the range makes no sense
        if (result.getMinRange() >= result.getMaxRange()) {
            result.setStatus(ResultStatus.INVALID);
            return;
        }

        // ── Check 6: Is the value within the healthy reference range? ─────────
        // If value is below minimum OR above maximum → ABNORMAL
        if (result.getValue() < result.getMinRange() ||
            result.getValue() > result.getMaxRange()) {
            result.setStatus(ResultStatus.ABNORMAL);
            return;
        }

        // ── All checks passed → NORMAL ─────────────────────────────────────
        result.setStatus(ResultStatus.NORMAL);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 2 — Batch Processing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Processes a list of LabResults all at once.
     * Calls processResult() on each item in the list.
     *
     * Skips any null entries in the list safely (logs a warning instead of crashing).
     *
     * @param results list of LabResult objects to process
     * @throws IllegalArgumentException if the list itself is null
     */
    public void processBatch(List<LabResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results list cannot be null");
        }

        for (int i = 0; i < results.size(); i++) {
            LabResult result = results.get(i);

            if (result == null) {
                // Skip null entries gracefully instead of crashing
                System.out.println("Warning: null entry found at index " + i + ", skipping.");
                continue;
            }

            processResult(result);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 3 — Filtering Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns only the results that match a given status.
     * Useful for generating targeted reports (e.g. "show me all ABNORMAL results").
     *
     * @param results list of already-processed LabResults
     * @param status  the status to filter by (NORMAL, ABNORMAL, or INVALID)
     * @return a new list containing only results with the matching status
     */
    public List<LabResult> filterByStatus(List<LabResult> results, ResultStatus status) {
        if (results == null || status == null) {
            throw new IllegalArgumentException("Results and status must not be null");
        }

        List<LabResult> filtered = new ArrayList<>();

        for (LabResult result : results) {
            if (result != null && result.getStatus() == status) {
                filtered.add(result);
            }
        }

        return filtered;
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 4 — Summary Statistics
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Counts how many results have a given status in a list.
     *
     * @param results list of processed LabResults
     * @param status  the status to count
     * @return number of results matching that status
     */
    public int countByStatus(List<LabResult> results, ResultStatus status) {
        if (results == null || status == null) {
            throw new IllegalArgumentException("Results and status must not be null");
        }

        int count = 0;
        for (LabResult result : results) {
            if (result != null && result.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if all results in the list have been validated (status is not null).
     * Useful to call before generating a report — ensures nothing was missed.
     *
     * @param results list of LabResults to check
     * @return true if every result has been validated, false otherwise
     */
    public boolean allResultsValidated(List<LabResult> results) {
        if (results == null || results.isEmpty()) {
            return false;
        }

        for (LabResult result : results) {
            if (result == null || !result.isValidated()) {
                return false;
            }
        }
        return true;
    }
}
