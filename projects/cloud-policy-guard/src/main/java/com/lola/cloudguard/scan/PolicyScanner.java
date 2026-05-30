package com.lola.cloudguard.scan;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.ScanResult;
import com.lola.cloudguard.domain.ScanStatus;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.Violation;
import com.lola.cloudguard.policy.PolicyRule;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PolicyScanner {
    private final List<PolicyRule> rules;

    public PolicyScanner(List<PolicyRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public ScanResult scan(List<SecurityGroup> groups, ScanContext context) {
        List<SecurityGroup> safeGroups = groups == null ? List.of() : groups;
        ScanContext safeContext = context == null ? new ScanContext("inline", null) : context;
        List<Violation> violations = safeGroups.stream()
                .flatMap(group -> rules.stream().flatMap(rule -> rule.evaluate(group, safeContext).stream()))
                .sorted(Comparator.comparing(Violation::severity).reversed().thenComparing(Violation::resource))
                .toList();

        return new ScanResult(
                UUID.randomUUID(),
                ScanStatus.COMPLETED,
                Instant.now(),
                safeContext.source(),
                safeContext.environment(),
                safeGroups.size(),
                violations
        );
    }
}
