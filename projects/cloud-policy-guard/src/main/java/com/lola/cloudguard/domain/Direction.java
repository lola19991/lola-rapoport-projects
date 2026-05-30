package com.lola.cloudguard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Direction {
    INBOUND,
    EGRESS;

    @JsonCreator
    public static Direction from(String value) {
        if (value == null || value.isBlank()) {
            return INBOUND;
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "ingress", "inbound", "in" -> INBOUND;
            case "egress", "outbound", "out" -> EGRESS;
            default -> throw new IllegalArgumentException("Unsupported direction: " + value);
        };
    }
}
