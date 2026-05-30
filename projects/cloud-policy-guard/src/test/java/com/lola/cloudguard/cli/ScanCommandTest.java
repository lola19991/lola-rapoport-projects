package com.lola.cloudguard.cli;

import static org.assertj.core.api.Assertions.assertThat;

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
}
