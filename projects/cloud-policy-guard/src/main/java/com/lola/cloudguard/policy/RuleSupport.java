package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.LinkedHashMap;
import java.util.Map;

final class RuleSupport {
    static final Map<Integer, String> DATABASE_PORTS = Map.ofEntries(
            Map.entry(1433, "Microsoft SQL Server"),
            Map.entry(1521, "Oracle"),
            Map.entry(3306, "MySQL"),
            Map.entry(5432, "PostgreSQL"),
            Map.entry(6379, "Redis"),
            Map.entry(9042, "Cassandra"),
            Map.entry(11211, "Memcached"),
            Map.entry(27017, "MongoDB"),
            Map.entry(9200, "Elasticsearch")
    );

    private RuleSupport() {
    }

    static boolean isInboundPublicTcp(SecurityRule rule) {
        return rule.isInbound() && rule.isPublic() && rule.isTcpLike();
    }

    static Violation violation(
            PolicyRule policyRule,
            Severity severity,
            SecurityGroup group,
            int ruleIndex,
            String message,
            SecurityRule securityRule,
            Map<String, Object> details
    ) {
        Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("cidr", securityRule.cidrDisplay());
        merged.put("ports", securityRule.portExpression());
        merged.put("direction", securityRule.direction().name());
        if (details != null) {
            merged.putAll(details);
        }
        return new Violation(
                severity,
                policyRule.id(),
                message,
                group.displayName(),
                ruleIndex,
                policyRule.recommendation(),
                merged
        );
    }
}
