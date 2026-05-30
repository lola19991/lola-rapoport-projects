package com.lola.cloudguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Violation(
        Severity severity,
        @JsonProperty("rule") String ruleId,
        String message,
        String resource,
        Integer ruleIndex,
        String recommendation,
        Map<String, Object> details
) {

    public Violation {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
