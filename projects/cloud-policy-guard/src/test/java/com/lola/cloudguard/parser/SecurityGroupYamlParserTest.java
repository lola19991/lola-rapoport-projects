package com.lola.cloudguard.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SecurityGroupYamlParserTest {

    @Test
    void parsesSecurityGroupYaml() throws Exception {
        var groups = new SecurityGroupYamlParser().parse(Path.of("examples/security-groups.yaml"));

        assertThat(groups).hasSize(2);
        assertThat(groups.getFirst().displayName()).isEqualTo("production-db");
        assertThat(groups.getFirst().rules()).hasSize(3);
    }
}
