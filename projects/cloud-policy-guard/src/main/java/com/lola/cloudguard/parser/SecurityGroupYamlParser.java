package com.lola.cloudguard.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lola.cloudguard.domain.SecurityGroup;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SecurityGroupYamlParser {
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public SecurityGroupYamlParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory())
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.jsonMapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<SecurityGroup> parse(Path path) throws IOException {
        String filename = path.getFileName().toString().toLowerCase();
        ObjectMapper mapper = isJsonFile(filename) ? jsonMapper : yamlMapper;
        SecurityGroupDocument document = mapper.readValue(path.toFile(), SecurityGroupDocument.class);
        return document.securityGroups();
    }

    public List<SecurityGroup> parse(String content) throws IOException {
        return parse(content, null);
    }

    public List<SecurityGroup> parse(String content, String filename) throws IOException {
        ObjectMapper mapper = (filename != null && isJsonFile(filename)) ? jsonMapper : yamlMapper;
        SecurityGroupDocument document = mapper.readValue(content, SecurityGroupDocument.class);
        return document.securityGroups();
    }

    private boolean isJsonFile(String filename) {
        return filename != null && filename.endsWith(".json");
    }
}
