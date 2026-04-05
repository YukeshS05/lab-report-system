package com.lab;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // ── Step 1: Create sample lab results ─────────────────────────────────
        List<LabResult> results = Arrays.asList(
            new LabResult("Blood Sugar",    110.5, "mg/dL",  70.0,  100.0, "P001"),
            new LabResult("Hemoglobin",      13.5, "g/dL",   12.0,   17.0, "P001"),
            new LabResult("Platelet Count",  -5.0, "k/uL",  150.0,  400.0, "P001"),
            new LabResult("WBC Count",        7.2, "k/uL",    4.0,   11.0, "P001"),
            new LabResult("Creatinine",       1.8, "mg/dL",  0.6,    1.2,  "P001")
        );

        // ── Step 2: Process all results using LabReportProcessor ──────────────
        LabReportProcessor processor = new LabReportProcessor();
        processor.processBatch(results);

        // ── Step 3: Generate reports using ReportGenerator ────────────────────
        ReportGenerator generator = new ReportGenerator();

        System.out.println("\n\n========== REPORT 1: FULL PATIENT REPORT ==========\n");
        System.out.println(generator.generatePatientReport("P001", results));

        System.out.println("\n========== REPORT 2: SUMMARY ONLY ==========\n");
        System.out.println(generator.generateSummaryReport(results));

        System.out.println("\n========== REPORT 3: FLAGGED RESULTS ONLY ==========\n");
        System.out.println(generator.generateFlaggedReport(results));
    }
}
