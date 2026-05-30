package com.lola.cloudguard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Severity {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    Severity(int rank) {
        this.rank = rank;
    }

    public boolean isAtLeast(Severity other) {
        return rank >= other.rank;
    }

    @JsonCreator
    public static Severity from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Severity is required");
        }
        return Severity.valueOf(value.trim().toUpperCase());
    }
}
