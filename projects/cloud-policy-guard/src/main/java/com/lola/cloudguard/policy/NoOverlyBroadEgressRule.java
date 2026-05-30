package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoOverlyBroadEgressRule implements PolicyRule {

    @Override
    public String id() {
        return "OVERLY_BROAD_EGRESS";
    }

    @Override
    public String title() {
        return "No overly broad egress";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.MEDIUM;
    }

    @Override
    public String recommendation() {
        return "Limit outbound access to required destinations, protocols, and ports.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            boolean allPorts = rule.isAllTraffic()
                    || (rule.fromPort() != null && rule.toPort() != null && rule.fromPort() <= 0 && rule.toPort() >= 65535);
            if (rule.isEgress() && rule.isPublic() && allPorts) {
                violations.add(RuleSupport.violation(
                        this,
                        defaultSeverity(),
                        group,
                        i,
                        "Outbound access is open to the internet across all ports.",
                        rule,
                        Map.of("exposure", "internet-egress")
                ));
            }
        }
        return violations;
    }
}
