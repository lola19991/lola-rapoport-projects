package com.lola.cloudguard.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Locale;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SecurityRule(
        Direction direction,
        String protocol,
        Integer port,
        Integer fromPort,
        Integer toPort,
        String cidr,
        String ipv6Cidr,
        String source,
        String description
) {

    public SecurityRule {
        direction = direction == null ? Direction.INBOUND : direction;
        protocol = normalizeProtocol(protocol);
        if (port != null && fromPort == null && toPort == null) {
            fromPort = port;
            toPort = port;
        }
    }

    @JsonIgnore
    public boolean isInbound() {
        return direction == Direction.INBOUND;
    }

    @JsonIgnore
    public boolean isEgress() {
        return direction == Direction.EGRESS;
    }

    @JsonIgnore
    public boolean isPublic() {
        return "0.0.0.0/0".equals(cidr) || "::/0".equals(cidr) || "::/0".equals(ipv6Cidr);
    }

    @JsonIgnore
    public boolean isAllTraffic() {
        return protocol == null
                || protocol.equals("-1")
                || protocol.equals("all")
                || (fromPort == null && toPort == null);
    }

    @JsonIgnore
    public boolean isTcpLike() {
        return isAllTraffic() || "tcp".equals(protocol);
    }

    public boolean coversPort(int targetPort) {
        if (isAllTraffic()) {
            return true;
        }
        int start = fromPort == null ? targetPort : fromPort;
        int end = toPort == null ? start : toPort;
        return start <= targetPort && targetPort <= end;
    }

    @JsonIgnore
    public String portExpression() {
        if (isAllTraffic()) {
            return "all ports";
        }
        if (Objects.equals(fromPort, toPort)) {
            return String.valueOf(fromPort);
        }
        return fromPort + "-" + toPort;
    }

    @JsonIgnore
    public String cidrDisplay() {
        if (cidr != null) {
            return cidr;
        }
        if (ipv6Cidr != null) {
            return ipv6Cidr;
        }
        return "non-cidr-source";
    }

    @JsonIgnore
    public String normalizedKey() {
        return direction + "|" + protocol + "|" + fromPort + "|" + toPort + "|" + cidr + "|" + ipv6Cidr + "|" + source;
    }

    private static String normalizeProtocol(String protocol) {
        if (protocol == null || protocol.isBlank()) {
            return "tcp";
        }
        return protocol.trim().toLowerCase(Locale.ROOT);
    }
}
