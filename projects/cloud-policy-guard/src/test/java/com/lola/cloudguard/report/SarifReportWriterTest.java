package com.lola.cloudguard.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lola.cloudguard.domain.Direction;
import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import com.lola.cloudguard.scan.PolicyScanner;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SarifReportWriterTest {

    @Test
    void writesSarifVersionAndResults() throws Exception {
        SecurityGroup group = new SecurityGroup(
                "production-db",
                "sg-123",
                "production",
                Map.of("team", "payments"),
                List.of(new SecurityRule(Direction.INBOUND, "tcp", 5432, null, null, "0.0.0.0/0", null, "test", null))
        );
        var result = new PolicyScanner(DefaultPolicyCatalog.rules()).scan(List.of(group), new ScanContext("example.yaml", null));

        String sarif = new SarifReportWriter().write(result);
        JsonNode root = new ObjectMapper().readTree(sarif);

        assertThat(root.path("version").asText()).isEqualTo("2.1.0");
        assertThat(root.path("runs").get(0).path("results")).isNotEmpty();
        assertThat(sarif).contains("PUBLIC_DATABASE_PORT");
    }
}
