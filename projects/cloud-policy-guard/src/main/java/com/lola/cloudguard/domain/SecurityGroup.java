package com.lola.cloudguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SecurityGroup(
        String name,
        String id,
        String environment,
        Map<String, String> tags,
        List<SecurityRule> rules
) {

    public SecurityGroup {
        tags = tags == null ? Map.of() : Map.copyOf(tags);
        rules = rules == null ? List.of() : List.copyOf(rules);
    }

    @JsonIgnore
    public String displayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return id == null || id.isBlank() ? "unknown-security-group" : id;
    }

    public Optional<String> tagValue(String key) {
        return tags.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    @JsonIgnore
    public String effectiveEnvironment(String fallback) {
        if (environment != null && !environment.isBlank()) {
            return environment;
        }
        return tagValue("environment")
                .or(() -> tagValue("env"))
                .orElse(fallback);
    }

    @JsonIgnore
    public Map<String, String> normalizedTags() {
        Map<String, String> normalized = new LinkedHashMap<>();
        tags.forEach((key, value) -> normalized.put(key.toLowerCase(Locale.ROOT), value));
        return normalized;
    }
}
