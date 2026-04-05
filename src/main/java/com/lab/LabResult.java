package com.lab;

/**
 * MODEL CLASS — represents a single lab test result.
 *
 * A model class does NOT contain any logic.
 * It only holds data and provides getters/setters to access it.
 *
 * Example usage:
 *   LabResult result = new LabResult("Blood Sugar", 110.5, "mg/dL", 70.0, 100.0, "P001");
 */
public class LabResult {

    // ─── Fields (the data this object holds) ─────────────────────────────────

    private String testName;      // e.g. "Hemoglobin", "Blood Sugar"
    private double value;         // the patient's actual test value
    private String unit;          // e.g. "mg/dL", "g/dL", "%"
    private double minRange;      // lowest value considered healthy
    private double maxRange;      // highest value considered healthy
    private String patientId;     // unique ID of the patient, e.g. "P001"
    private ResultStatus status;  // NORMAL, ABNORMAL, or INVALID — set after validation


    // ─── Constructor (used to create a new LabResult object) ─────────────────

    /**
     * Creates a new LabResult with the core test information.
     * Status is NOT set here — it gets assigned by LabReportProcessor during validation.
     *
     * @param testName  name of the lab test
     * @param value     patient's measured value
     * @param unit      unit of measurement
     * @param minRange  minimum value of the healthy reference range
     * @param maxRange  maximum value of the healthy reference range
     * @param patientId unique patient identifier
     */
    public LabResult(String testName, double value, String unit,
                     double minRange, double maxRange, String patientId) {
        this.testName  = testName;
        this.value     = value;
        this.unit      = unit;
        this.minRange  = minRange;
        this.maxRange  = maxRange;
        this.patientId = patientId;
        this.status    = null; // not yet validated
    }


    // ─── Getters (read the value of a field) ─────────────────────────────────

    public String getTestName()      { return testName; }
    public double getValue()         { return value; }
    public String getUnit()          { return unit; }
    public double getMinRange()      { return minRange; }
    public double getMaxRange()      { return maxRange; }
    public String getPatientId()     { return patientId; }
    public ResultStatus getStatus()  { return status; }


    // ─── Setters (change the value of a field) ────────────────────────────────

    public void setTestName(String testName)      { this.testName  = testName; }
    public void setValue(double value)            { this.value     = value; }
    public void setUnit(String unit)              { this.unit      = unit; }
    public void setMinRange(double minRange)      { this.minRange  = minRange; }
    public void setMaxRange(double maxRange)      { this.maxRange  = maxRange; }
    public void setPatientId(String patientId)    { this.patientId = patientId; }
    public void setStatus(ResultStatus status)    { this.status    = status; }


    // ─── Helper Methods ───────────────────────────────────────────────────────

    /**
     * Returns true if this result has been validated (status has been assigned).
     * Use this to check before generating a report.
     */
    public boolean isValidated() {
        return this.status != null;
    }

    /**
     * Returns a clean, human-readable summary of this result.
     * Used by ReportGenerator to build the final report.
     *
     * Example output:
     *   [P001] Blood Sugar: 110.5 mg/dL | Range: 70.0 - 100.0 | Status: ABNORMAL
     */
    @Override
    public String toString() {
        return String.format(
            "[%s] %s: %.1f %s | Range: %.1f - %.1f | Status: %s",
            patientId,
            testName,
            value,
            unit,
            minRange,
            maxRange,
            status != null ? status : "NOT VALIDATED"
        );
    }
}
