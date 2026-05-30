package com.lola.cloudguard.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lola.cloudguard.domain.Direction;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TerraformPlanParser {
    private final ObjectMapper mapper;

    public TerraformPlanParser() {
        this.mapper = new ObjectMapper().findAndRegisterModules();
    }

    public List<SecurityGroup> parse(Path path) throws IOException {
        return parse(mapper.readTree(path.toFile()));
    }

    public List<SecurityGroup> parse(String json) throws IOException {
        return parse(mapper.readTree(json));
    }

    public List<SecurityGroup> parse(JsonNode root) {
        Map<String, MutableGroup> groups = new LinkedHashMap<>();
        JsonNode resourceChanges = root.path("resource_changes");
        if (!resourceChanges.isArray()) {
            return List.of();
        }

        for (JsonNode change : resourceChanges) {
            JsonNode after = change.path("change").path("after");
            if (after.isMissingNode() || after.isNull()) {
                continue;
            }
            String type = text(change, "type");
            String address = text(change, "address", type);
            switch (type) {
                case "aws_security_group" -> parseSecurityGroupResource(groups, address, after);
                case "aws_security_group_rule" -> parseClassicRuleResource(groups, address, after);
                case "aws_vpc_security_group_ingress_rule" -> parseVpcRuleResource(groups, address, after, Direction.INBOUND);
                case "aws_vpc_security_group_egress_rule" -> parseVpcRuleResource(groups, address, after, Direction.EGRESS);
                default -> {
                }
            }
        }

        return groups.values().stream().map(MutableGroup::toSecurityGroup).toList();
    }

    private void parseSecurityGroupResource(Map<String, MutableGroup> groups, String address, JsonNode after) {
        String id = text(after, "id", address);
        String name = text(after, "name", address);
        MutableGroup group = groups.computeIfAbsent(id, ignored -> new MutableGroup(name, id, tags(after)));
        group.mergeTags(tags(after));
        parseInlineRules(group, address, after.path("ingress"), Direction.INBOUND);
        parseInlineRules(group, address, after.path("egress"), Direction.EGRESS);
    }

    private void parseClassicRuleResource(Map<String, MutableGroup> groups, String address, JsonNode after) {
        Direction direction = Direction.from(text(after, "type", "ingress"));
        String groupId = text(after, "security_group_id", address);
        MutableGroup group = groups.computeIfAbsent(groupId, ignored -> new MutableGroup(groupId, groupId, Map.of()));
        addCidrRules(
                group,
                direction,
                text(after, "protocol", "tcp"),
                integer(after, "from_port"),
                integer(after, "to_port"),
                after.path("cidr_blocks"),
                after.path("ipv6_cidr_blocks"),
                address,
                text(after, "description")
        );
    }

    private void parseVpcRuleResource(Map<String, MutableGroup> groups, String address, JsonNode after, Direction direction) {
        String groupId = text(after, "security_group_id", address);
        MutableGroup group = groups.computeIfAbsent(groupId, ignored -> new MutableGroup(groupId, groupId, tags(after)));
        group.mergeTags(tags(after));
        String cidr = text(after, "cidr_ipv4");
        String ipv6Cidr = text(after, "cidr_ipv6");
        if (cidr != null || ipv6Cidr != null) {
            group.rules.add(new SecurityRule(
                    direction,
                    text(after, "ip_protocol", "tcp"),
                    null,
                    integer(after, "from_port"),
                    integer(after, "to_port"),
                    cidr,
                    ipv6Cidr,
                    address,
                    text(after, "description")
            ));
        }
    }

    private void parseInlineRules(MutableGroup group, String address, JsonNode rules, Direction direction) {
        if (!rules.isArray()) {
            return;
        }
        for (JsonNode rule : rules) {
            addCidrRules(
                    group,
                    direction,
                    text(rule, "protocol", "tcp"),
                    integer(rule, "from_port"),
                    integer(rule, "to_port"),
                    rule.path("cidr_blocks"),
                    rule.path("ipv6_cidr_blocks"),
                    address,
                    text(rule, "description")
            );
        }
    }

    private void addCidrRules(
            MutableGroup group,
            Direction direction,
            String protocol,
            Integer fromPort,
            Integer toPort,
            JsonNode cidrBlocks,
            JsonNode ipv6CidrBlocks,
            String source,
            String description
    ) {
        if (cidrBlocks.isArray()) {
            for (JsonNode cidr : cidrBlocks) {
                group.rules.add(new SecurityRule(direction, protocol, null, fromPort, toPort, cidr.asText(), null, source, description));
            }
        }
        if (ipv6CidrBlocks.isArray()) {
            for (JsonNode cidr : ipv6CidrBlocks) {
                group.rules.add(new SecurityRule(direction, protocol, null, fromPort, toPort, null, cidr.asText(), source, description));
            }
        }
    }

    private Map<String, String> tags(JsonNode node) {
        Map<String, String> tags = new LinkedHashMap<>();
        JsonNode tagsNode = node.path("tags");
        if (tagsNode.isObject()) {
            tagsNode.fields().forEachRemaining(entry -> tags.put(entry.getKey(), entry.getValue().asText()));
        }
        return tags;
    }

    private String text(JsonNode node, String field) {
        return text(node, field, null);
    }

    private String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        String text = value.asText();
        return text.isBlank() ? fallback : text;
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asInt();
    }

    private static final class MutableGroup {
        private final String name;
        private final String id;
        private final Map<String, String> tags = new LinkedHashMap<>();
        private final List<SecurityRule> rules = new ArrayList<>();

        private MutableGroup(String name, String id, Map<String, String> tags) {
            this.name = name;
            this.id = id;
            mergeTags(tags);
        }

        private void mergeTags(Map<String, String> newTags) {
            tags.putAll(newTags);
        }

        private SecurityGroup toSecurityGroup() {
            return new SecurityGroup(name, id, tags.get("Environment"), tags, rules);
        }
    }
}
