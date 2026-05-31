package com.lola.cloudguard.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ScanCommandTest {

    @Test
    void writesJsonReportAndReturnsPolicyExitCodeWhenThresholdIsMet() throws Exception {
        Path report = Files.createTempFile("policy-guard", ".json");

        int exitCode = CommandFactory.create().execute(
                "scan",
                "examples/security-groups.yaml",
                "--output",
                report.toString(),
                "--fail-on",
                "high"
        );

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.readString(report)).contains("PUBLIC_DATABASE_PORT");
    }

    @Test
    void writesStructuredViolationsWithRecommendationsForRiskyInput() throws Exception {
        Path report = Files.createTempFile("policy-guard-golden", ".json");

        int exitCode = CommandFactory.create().execute(
                "scan",
                "src/test/resources/golden/unsafe-security-groups.yaml",
                "--output",
                report.toString(),
                "--fail-on",
                "high"
        );

        var root = new ObjectMapper().readTree(report.toFile());
        var violations = root.path("violations");

        assertThat(exitCode).isEqualTo(2);
        assertThat(violations.findValuesAsText("rule"))
                .contains("PUBLIC_SSH", "PUBLIC_RDP", "PUBLIC_DATABASE_PORT", "OVERLY_BROAD_INGRESS");
        assertThat(violations)
                .allSatisfy(violation -> assertThat(violation.path("recommendation").asText()).isNotBlank());
    }
}
