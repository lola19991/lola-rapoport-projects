package com.lola.cloudguard.scan;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.Violation;
import com.lola.cloudguard.parser.SecurityGroupYamlParser;
import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class GoldenOutputRegressionTest {
    private static final Path GOLDEN_DIR = Path.of("src/test/resources/golden");

    private final SecurityGroupYamlParser parser = new SecurityGroupYamlParser();
    private final PolicyScanner scanner = new PolicyScanner(DefaultPolicyCatalog.rules());
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Test
    void unsafeSecurityGroupsMatchGoldenViolations() throws Exception {
        assertGolden("unsafe-security-groups.yaml", "unsafe-expected.json");
    }

    @Test
    void safeSecurityGroupsMatchGoldenViolations() throws Exception {
        assertGolden("safe-security-groups.yaml", "safe-expected.json");
    }

    private void assertGolden(String inputFile, String expectedFile) throws Exception {
        var groups = parser.parse(GOLDEN_DIR.resolve(inputFile));
        var result = scanner.scan(groups, new ScanContext(inputFile, null));

        JsonNode actual = mapper.valueToTree(normalize(result.violations()));
        JsonNode expected = mapper.readTree(GOLDEN_DIR.resolve(expectedFile).toFile());

        assertThat(actual).isEqualTo(expected);
    }

    private List<NormalizedViolation> normalize(List<Violation> violations) {
        return violations.stream()
                .map(violation -> new NormalizedViolation(
                        violation.severity().name(),
                        violation.ruleId(),
                        violation.resource(),
                        violation.ruleIndex(),
                        violation.message(),
                        violation.recommendation(),
                        normalizeDetails(violation.details())
                ))
                .sorted(Comparator
                        .comparingInt((NormalizedViolation violation) -> severityRank(violation.severity())).reversed()
                        .thenComparing(NormalizedViolation::resource)
                        .thenComparing(NormalizedViolation::rule)
                        .thenComparing(violation -> violation.ruleIndex() == null ? -1 : violation.ruleIndex())
                        .thenComparing(NormalizedViolation::message))
                .toList();
    }

    private Map<String, Object> normalizeDetails(Map<String, Object> details) {
        Map<String, Object> normalized = new TreeMap<>();
        details.forEach((key, value) -> normalized.put(key, normalizeValue(value)));
        return normalized;
    }

    private Object normalizeValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new TreeMap<>();
            map.forEach((key, nestedValue) -> normalized.put(String.valueOf(key), normalizeValue(nestedValue)));
            return normalized;
        }
        return value;
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private record NormalizedViolation(
            String severity,
            String rule,
            String resource,
            Integer ruleIndex,
            String message,
            String recommendation,
            Map<String, Object> details
    ) {
    }
}
