package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoPublicSshRule implements PolicyRule {
    private static final int SSH_PORT = 22;

    @Override
    public String id() {
        return "PUBLIC_SSH";
    }

    @Override
    public String title() {
        return "No public SSH";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.HIGH;
    }

    @Override
    public String recommendation() {
        return "Restrict SSH to a VPN, bastion host, or trusted administrator CIDR.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            if (RuleSupport.isInboundPublicTcp(rule) && rule.coversPort(SSH_PORT)) {
                violations.add(RuleSupport.violation(
                        this,
                        defaultSeverity(),
                        group,
                        i,
                        "SSH port 22 is open to the internet.",
                        rule,
                        Map.of("port", SSH_PORT)
                ));
            }
        }
        return violations;
    }
}
