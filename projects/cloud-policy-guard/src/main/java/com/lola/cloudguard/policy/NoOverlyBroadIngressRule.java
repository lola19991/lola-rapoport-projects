package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoOverlyBroadIngressRule implements PolicyRule {

    @Override
    public String id() {
        return "OVERLY_BROAD_INGRESS";
    }

    @Override
    public String title() {
        return "No overly broad ingress";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.HIGH;
    }

    @Override
    public String recommendation() {
        return "Restrict inbound access to required protocols, ports, and trusted source CIDRs.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            boolean allPorts = rule.isAllTraffic()
                    || (rule.fromPort() != null && rule.toPort() != null && rule.fromPort() <= 0 && rule.toPort() >= 65535);
            if (rule.isInbound() && rule.isPublic() && allPorts) {
                violations.add(RuleSupport.violation(
                        this,
                        defaultSeverity(),
                        group,
                        i,
                        "Inbound access is open to the internet across all ports.",
                        rule,
                        Map.of("exposure", "internet-ingress")
                ));
            }
        }
        return violations;
    }
}
