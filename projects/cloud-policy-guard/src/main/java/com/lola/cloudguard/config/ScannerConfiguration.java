package com.lola.cloudguard.config;

import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import com.lola.cloudguard.policy.PolicyRule;
import com.lola.cloudguard.scan.PolicyScanner;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScannerConfiguration {

    @Bean
    List<PolicyRule> policyRules() {
        return DefaultPolicyCatalog.rules();
    }

    @Bean
    PolicyScanner policyScanner(List<PolicyRule> policyRules) {
        return new PolicyScanner(policyRules);
    }
}
