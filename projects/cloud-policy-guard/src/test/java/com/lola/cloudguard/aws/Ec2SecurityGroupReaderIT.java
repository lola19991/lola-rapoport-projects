package com.lola.cloudguard.aws;

import static org.assertj.core.api.Assertions.assertThat;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.Violation;
import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import com.lola.cloudguard.scan.PolicyScanner;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;

@Testcontainers(disabledWithoutDocker = true)
class Ec2SecurityGroupReaderIT {
    private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack:3.8.1");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(LocalStackContainer.Service.EC2);

    @Test
    void importsSecurityGroupsFromLocalStack() {
        try (Ec2Client ec2Client = Ec2Client.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.EC2))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        localstack.getAccessKey(),
                        localstack.getSecretKey()
                )))
                .region(Region.of(localstack.getRegion()))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build()) {

            var created = ec2Client.createSecurityGroup(request -> request
                    .groupName("localstack-admin")
                    .description("admin access demo"));
            ec2Client.authorizeSecurityGroupIngress(request -> request
                    .groupId(created.groupId())
                    .ipPermissions(IpPermission.builder()
                            .ipProtocol("tcp")
                            .fromPort(22)
                            .toPort(22)
                            .ipRanges(IpRange.builder().cidrIp("0.0.0.0/0").build())
                            .build()));

            var groups = new Ec2SecurityGroupReader(ec2Client).readSecurityGroups();
            var result = new PolicyScanner(DefaultPolicyCatalog.rules()).scan(groups, new ScanContext("localstack", null));

            assertThat(groups).anySatisfy(group -> assertThat(group.displayName()).isEqualTo("localstack-admin"));
            assertThat(result.violations()).extracting(Violation::ruleId).contains("PUBLIC_SSH");
        }
    }
}
