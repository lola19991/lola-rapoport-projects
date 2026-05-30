package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EnvironmentPolicyRule implements PolicyRule {
    private static final Set<Integer> APPROVED_PUBLIC_PRODUCTION_PORTS = Set.of(80, 443);

    @Override
    public String id() {
        return "ENVIRONMENT_POLICY_VIOLATION";
    }

    @Override
    public String title() {
        return "Environment-specific public exposure policy";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.CRITICAL;
    }

    @Override
    public String recommendation() {
        return "For production, expose only approved public entry points and keep service/database/admin ports private.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        String environment = group.effectiveEnvironment(context.environment());
        if (environment == null || !isProduction(environment)) {
            return List.of();
        }

        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            if (RuleSupport.isInboundPublicTcp(rule) && !isApprovedProductionExposure(rule)) {
                violations.add(RuleSupport.violation(
                        this,
                        defaultSeverity(),
                        group,
                        i,
                        "Production security group exposes " + rule.portExpression() + " to the internet.",
                        rule,
                        Map.of("environment", environment, "approvedPublicPorts", APPROVED_PUBLIC_PRODUCTION_PORTS)
                ));
            }
        }
        return violations;
    }

    private boolean isProduction(String environment) {
        String normalized = environment.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("prod") || normalized.equals("production");
    }

    private boolean isApprovedProductionExposure(SecurityRule rule) {
        if (rule.isAllTraffic() || rule.fromPort() == null || rule.toPort() == null) {
            return false;
        }
        return rule.fromPort().equals(rule.toPort()) && APPROVED_PUBLIC_PRODUCTION_PORTS.contains(rule.fromPort());
    }
}
