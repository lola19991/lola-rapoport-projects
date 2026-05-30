package com.lola.cloudguard.policy;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.Severity;
import com.lola.cloudguard.domain.Violation;
import java.util.List;

public interface PolicyRule {

    String id();

    String title();

    Severity defaultSeverity();

    String recommendation();

    List<Violation> evaluate(SecurityGroup group, ScanContext context);
}
