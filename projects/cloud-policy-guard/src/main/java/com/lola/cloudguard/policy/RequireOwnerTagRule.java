package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.List;
import java.util.Map;

public class RequireOwnerTagRule implements PolicyRule {

    @Override
    public String id() {
        return "MISSING_OWNER_TAG";
    }

    @Override
    public String title() {
        return "Require owner or team tag";
    }

    @Override
    public Severity defaultSeverity() {
        return Severity.LOW;
    }

    @Override
    public String recommendation() {
        return "Add an owner or team tag so risky access can be routed to the responsible group.";
    }

    @Override
    public List<Violation> evaluate(SecurityGroup group, ScanContext context) {
        boolean hasOwner = group.tagValue("owner").isPresent()
                || group.tagValue("team").isPresent()
                || group.tagValue("service-owner").isPresent();
        if (hasOwner) {
            return List.of();
        }
        return List.of(new Violation(
                defaultSeverity(),
                id(),
                "Security group does not have an owner, team, or service-owner tag.",
                group.displayName(),
                null,
                recommendation(),
                Map.of("tags", group.normalizedTags())
        ));
    }
}
