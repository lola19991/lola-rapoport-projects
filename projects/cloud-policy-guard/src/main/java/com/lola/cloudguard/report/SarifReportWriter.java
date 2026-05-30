package com.lola.cloudguard.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lola.cloudguard.domain.ScanResult;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SarifReportWriter {
    private static final String SARIF_SCHEMA = "https://json.schemastore.org/sarif-2.1.0.json";

    private final JsonReportWriter jsonReportWriter;

    public SarifReportWriter() {
        this.jsonReportWriter = new JsonReportWriter();
    }

    public String write(ScanResult result) throws JsonProcessingException {
        return jsonReportWriter.write(toSarif(result));
    }

    public Map<String, Object> toSarif(ScanResult result) {
        List<Map<String, Object>> rules = result.violations().stream()
                .collect(LinkedHashMap<String, Violation>::new, (map, violation) -> map.putIfAbsent(violation.ruleId(), violation), Map::putAll)
                .values()
                .stream()
                .sorted(Comparator.comparing(Violation::ruleId))
                .map(this::rule)
                .toList();

        List<Map<String, Object>> sarifResults = result.violations().stream()
                .map(violation -> sarifResult(violation, result.source()))
                .toList();

        return Map.of(
                "$schema", SARIF_SCHEMA,
                "version", "2.1.0",
                "runs", List.of(Map.of(
                        "tool", Map.of("driver", Map.of(
                                "name", "Cloud Policy Guard",
                                "informationUri", "https://github.com/",
                                "rules", rules
                        )),
                        "results", sarifResults
                ))
        );
    }

    private Map<String, Object> rule(Violation violation) {
        return Map.of(
                "id", violation.ruleId(),
                "name", violation.ruleId(),
                "shortDescription", Map.of("text", violation.message()),
                "help", Map.of("text", violation.recommendation()),
                "properties", Map.of(
                        "security-severity", securitySeverity(violation.severity()),
                        "precision", "high",
                        "tags", List.of("security", "cloud", "network")
                )
        );
    }

    private Map<String, Object> sarifResult(Violation violation, String source) {
        return Map.of(
                "ruleId", violation.ruleId(),
                "level", level(violation.severity()),
                "message", Map.of("text", violation.message()),
                "locations", List.of(Map.of("physicalLocation", Map.of(
                        "artifactLocation", Map.of("uri", source == null ? "inline" : source),
                        "region", Map.of("startLine", 1)
                ))),
                "properties", Map.of(
                        "resource", violation.resource(),
                        "ruleIndex", violation.ruleIndex() == null ? -1 : violation.ruleIndex(),
                        "details", violation.details()
                )
        );
    }

    private String level(Severity severity) {
        return switch (severity) {
            case CRITICAL, HIGH -> "error";
            case MEDIUM -> "warning";
            case LOW -> "note";
        };
    }

    private String securitySeverity(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "9.0";
            case HIGH -> "8.0";
            case MEDIUM -> "5.0";
            case LOW -> "2.0";
        };
    }
}
