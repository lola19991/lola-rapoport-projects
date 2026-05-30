package com.lola.cloudguard.cli;

import com.lola.cloudguard.domain.ScanResult;
import com.lola.cloudguard.domain.Severity;

enum ExitThreshold {
    NONE(null),
    LOW(Severity.LOW),
    MEDIUM(Severity.MEDIUM),
    HIGH(Severity.HIGH),
    CRITICAL(Severity.CRITICAL);

    private final Severity severity;

    ExitThreshold(Severity severity) {
        this.severity = severity;
    }

    static ExitThreshold from(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return ExitThreshold.valueOf(value.trim().replace('-', '_').toUpperCase());
    }

    boolean isViolatedBy(ScanResult result) {
        return severity != null && result.hasViolationAtOrAbove(severity);
    }
}
