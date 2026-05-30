package com.lola.cloudguard.rest;

import com.lola.cloudguard.domain.SecurityGroup;
import java.util.List;

public record ScanSubmission(
        String source,
        String environment,
        List<SecurityGroup> securityGroups
) {

    public ScanSubmission {
        securityGroups = securityGroups == null ? List.of() : List.copyOf(securityGroups);
    }
}
