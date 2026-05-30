package com.lola.cloudguard.aws;

import com.lola.cloudguard.domain.Direction;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.domain.SecurityRule;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.Tag;

public class Ec2SecurityGroupReader {
    private final Ec2Client ec2Client;

    public Ec2SecurityGroupReader(Ec2Client ec2Client) {
        this.ec2Client = ec2Client;
    }

    public List<SecurityGroup> readSecurityGroups() {
        List<SecurityGroup> groups = new ArrayList<>();
        String nextToken = null;
        do {
            DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
                    .nextToken(nextToken)
                    .build();
            DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups(request);
            response.securityGroups().forEach(group -> groups.add(toDomain(group)));
            nextToken = response.nextToken();
        } while (nextToken != null && !nextToken.isBlank());
        return groups;
    }

    private SecurityGroup toDomain(software.amazon.awssdk.services.ec2.model.SecurityGroup group) {
        Map<String, String> tags = new LinkedHashMap<>();
        for (Tag tag : group.tags()) {
            tags.put(tag.key(), tag.value());
        }

        List<SecurityRule> rules = new ArrayList<>();
        group.ipPermissions().forEach(permission -> rules.addAll(toRules(Direction.INBOUND, permission, group.groupId())));
        group.ipPermissionsEgress().forEach(permission -> rules.addAll(toRules(Direction.EGRESS, permission, group.groupId())));
        return new SecurityGroup(group.groupName(), group.groupId(), tags.get("Environment"), tags, rules);
    }

    private List<SecurityRule> toRules(Direction direction, IpPermission permission, String source) {
        List<SecurityRule> rules = new ArrayList<>();
        String protocol = permission.ipProtocol();
        Integer fromPort = permission.fromPort();
        Integer toPort = permission.toPort();

        permission.ipRanges().forEach(range -> rules.add(new SecurityRule(
                direction,
                protocol,
                null,
                fromPort,
                toPort,
                range.cidrIp(),
                null,
                source,
                range.description()
        )));
        permission.ipv6Ranges().forEach(range -> rules.add(new SecurityRule(
                direction,
                protocol,
                null,
                fromPort,
                toPort,
                null,
                range.cidrIpv6(),
                source,
                range.description()
        )));
        return rules;
    }
}
