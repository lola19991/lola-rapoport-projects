package com.lola.cloudguard.report;

public enum ReportFormat {
    JSON,
    SARIF;

    public static ReportFormat from(String value) {
        if (value == null || value.isBlank()) {
            return JSON;
        }
        return ReportFormat.valueOf(value.trim().replace('-', '_').toUpperCase());
    }
}
