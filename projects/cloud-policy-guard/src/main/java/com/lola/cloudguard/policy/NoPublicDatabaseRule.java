package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoPublicDatabaseRule implements PolicyRule {

    @Override
    public String id() {
        return "PUBLIC_DATABASE_PORT";
    }

    @Override
    public String title() {
        return "No public database ports";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.HIGH;
    }

    @Override
    public String recommendation() {
        return "Expose databases only on private networks and allow access from application security groups.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            if (!RuleSupport.isInboundPublicTcp(rule)) {
                continue;
            }
            for (Map.Entry<Integer, String> entry : RuleSupport.DATABASE_PORTS.entrySet()) {
                int port = entry.getKey();
                String service = entry.getValue();
                if (rule.coversPort(port)) {
                    violations.add(RuleSupport.violation(
                            this,
                            defaultSeverity(),
                            group,
                            i,
                            service + " port " + port + " is open to the internet.",
                            rule,
                            Map.of("port", port, "service", service)
                    ));
                }
            }
        }
        return violations;
    }
}
