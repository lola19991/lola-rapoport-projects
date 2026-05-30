package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoDuplicateRule implements PolicyRule {

    @Override
    public String id() {
        return "DUPLICATE_RULE";
    }

    @Override
    public String title() {
        return "No duplicate network rules";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.LOW;
    }

    @Override
    public String recommendation() {
        return "Remove duplicate rules to reduce policy noise and make reviews easier.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        Map<String, Integer> firstSeen = new HashMap<>();
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < group.rules().size(); i++) {
            SecurityRule rule = group.rules().get(i);
            Integer firstIndex = firstSeen.putIfAbsent(rule.normalizedKey(), i);
            if (firstIndex != null) {
                violations.add(RuleSupport.violation(
                        this,
                        defaultSeverity(),
                        group,
                        i,
                        "Rule duplicates rule index " + firstIndex + ".",
                        rule,
                        Map.of("firstRuleIndex", firstIndex)
                ));
            }
        }
        return violations;
    }
}
