package com.lola.cloudguard.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lola.cloudguard.domain.SecurityGroup;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SecurityGroupYamlParser {
    private final ObjectMapper mapper;

    public SecurityGroupYamlParser() {
        this.mapper = new ObjectMapper(new YAMLFactory())
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<SecurityGroup> parse(Path path) throws IOException {
        SecurityGroupDocument document = mapper.readValue(path.toFile(), SecurityGroupDocument.class);
        return document.securityGroups();
    }

    public List<SecurityGroup> parse(String content) throws IOException {
        SecurityGroupDocument document = mapper.readValue(content, SecurityGroupDocument.class);
        return document.securityGroups();
    }
}
