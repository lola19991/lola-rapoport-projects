package com.lola.cloudguard.scan;

import static org.assertj.core.api.Assertions.assertThat;

import com.lola.cloudguard.domain.Direction;
import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Violation;
import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PolicyScannerTest {

    private final PolicyScanner scanner = new PolicyScanner(DefaultPolicyCatalog.rules());

    @Test
    void flagsPublicDatabaseAndProductionExposure() {
        SecurityGroup group = new SecurityGroup(
                "production-db",
                "sg-123",
                "production",
                Map.of("team", "payments"),
                List.of(new SecurityRule(Direction.INBOUND, "tcp", 5432, null, null, "0.0.0.0/0", null, "test", null))
        );

        var result = scanner.scan(List.of(group), new ScanContext("unit-test", null));

        assertThat(result.violations())
                .extracting(Violation::ruleId)
                .contains("PUBLIC_DATABASE_PORT", "ENVIRONMENT_POLICY_VIOLATION");
    }

    @Test
    void flagsDuplicateRulesMissingOwnerAndBroadEgress() {
        SecurityRule egress = new SecurityRule(Direction.EGRESS, "-1", null, null, null, "0.0.0.0/0", null, "test", null);
        SecurityRule rdp = new SecurityRule(Direction.INBOUND, "tcp", 3389, null, null, null, "::/0", "test", null);
        SecurityGroup group = new SecurityGroup("admin", "sg-456", "staging", Map.of(), List.of(egress, rdp, rdp));

        var result = scanner.scan(List.of(group), new ScanContext("unit-test", null));

        assertThat(result.violations())
                .extracting(Violation::ruleId)
                .contains("OVERLY_BROAD_EGRESS", "PUBLIC_RDP", "MISSING_OWNER_TAG", "DUPLICATE_RULE");
    }
}
