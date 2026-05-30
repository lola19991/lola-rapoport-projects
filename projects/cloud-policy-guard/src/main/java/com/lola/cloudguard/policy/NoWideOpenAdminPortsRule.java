package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoWideOpenAdminPortsRule implements PolicyRule {
    private static final Map<Integer, String> ADMIN_PORTS = Map.of(
            22, "SSH",
            3389, "RDP",
            5900, "VNC",
            5985, "WinRM",
            5986, "WinRM over HTTPS",
            6443, "Kubernetes API",
            2375, "Docker API",
            2376, "Docker API over TLS"
    );

    @Override
    public String id() {
        return "WIDE_OPEN_ADMIN_PORT";
    }

    @Override
    public String title() {
        return "No wide-open administration ports";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.HIGH;
    }

    @Override
    public String recommendation() {
        return "Move administrative access behind identity-aware access, VPN, or a private bastion path.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            if (!RuleSupport.isInboundPublicTcp(rule)) {
                continue;
            }
            for (Map.Entry<Integer, String> entry : ADMIN_PORTS.entrySet()) {
                if (rule.coversPort(entry.getKey())) {
                    violations.add(RuleSupport.violation(
                            this,
                            defaultSeverity(),
                            group,
                            i,
                            entry.getValue() + " administration port " + entry.getKey() + " is open to the internet.",
                            rule,
                            Map.of("port", entry.getKey(), "service", entry.getValue())
                    ));
                }
            }
        }
        return violations;
    }
}
