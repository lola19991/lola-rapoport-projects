package com.lola.cloudguard.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lola.cloudguard.domain.SecurityGroup;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SecurityGroupDocument(List<SecurityGroup> securityGroups) {

    public SecurityGroupDocument {
        securityGroups = securityGroups == null ? List.of() : List.copyOf(securityGroups);
    }
}
