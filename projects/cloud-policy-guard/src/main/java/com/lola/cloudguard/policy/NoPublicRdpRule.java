package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoPublicRdpRule implements PolicyRule {
    private static final int RDP_PORT = 3389;

    @Override
    public String id() {
        return "PUBLIC_RDP";
    }

    @Override
    public String title() {
        return "No public RDP";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.HIGH;
    }

    @Override
    public String recommendation() {
        return "Restrict RDP to a VPN, privileged access gateway, or trusted administrator CIDR.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            if (RuleSupport.isInboundPublicTcp(rule) && rule.coversPort(RDP_PORT)) {
                violations.add(RuleSupport.violation(
                        this,
                        defaultSeverity(),
                        group,
                        i,
                        "RDP port 3389 is open to the internet.",
                        rule,
                        Map.of("port", RDP_PORT)
                ));
            }
        }
        return violations;
    }
}
