package com.lola.cloudguard.policy;

import java.util.List;

public final class DefaultPolicyCatalog {
    private DefaultPolicyCatalog() {
    }

    public static List<PolicyRule> rules() {
        return List.of(
                new NoPublicSshRule(),
                new NoPublicRdpRule(),
                new NoPublicDatabaseRule(),
                new NoWideOpenAdminPortsRule(),
                new NoOverlyBroadEgressRule(),
                new RequireOwnerTagRule(),
                new NoDuplicateRule(),
                new EnvironmentPolicyRule()
        );
    }
}
