package com.lab;

/**
 * Represents the possible outcomes of a lab result validation.
 *
 * NORMAL   → value is within the healthy reference range
 * ABNORMAL → value is outside the healthy reference range
 * INVALID  → value is missing, negative, or not a real number
 */
public enum ResultStatus {
    NORMAL,
    ABNORMAL,
    INVALID
}
