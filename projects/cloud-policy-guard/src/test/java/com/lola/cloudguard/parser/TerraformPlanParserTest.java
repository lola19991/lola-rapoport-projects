package com.lola.cloudguard.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.Violation;
import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import com.lola.cloudguard.scan.PolicyScanner;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TerraformPlanParserTest {

    @Test
    void parsesTerraformPlanAndFindsViolations() throws Exception {
        var groups = new TerraformPlanParser().parse(Path.of("examples/terraform-plan.json"));
        var result = new PolicyScanner(DefaultPolicyCatalog.rules()).scan(groups, new ScanContext("terraform-plan", "production"));

        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().rules()).hasSize(3);
        assertThat(result.violations())
                .extracting(Violation::ruleId)
                .contains("PUBLIC_DATABASE_PORT", "PUBLIC_SSH", "OVERLY_BROAD_EGRESS");
    }
}
