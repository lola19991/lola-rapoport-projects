package com.lola.cloudguard.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScanResult(
        UUID id,
        ScanStatus status,
        Instant scannedAt,
        String source,
        String environment,
        int resourcesScanned,
        List<Violation> violations
) {

    public ScanResult {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public boolean hasViolationAtOrAbove(Severity threshold) {
        return violations.stream().anyMatch(violation -> violation.severity().isAtLeast(threshold));
    }
}
