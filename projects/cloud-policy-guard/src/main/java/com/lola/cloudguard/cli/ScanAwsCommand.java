package com.lola.cloudguard.cli;

import com.lola.cloudguard.aws.AwsEc2ClientFactory;
import com.lola.cloudguard.aws.Ec2SecurityGroupReader;
import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.ScanResult;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.report.JsonReportWriter;
import com.lola.cloudguard.report.ReportFormat;
import com.lola.cloudguard.report.SarifReportWriter;
import com.lola.cloudguard.scan.PolicyScanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.amazon.awssdk.services.ec2.Ec2Client;

@Command(
        name = "scan-aws",
        mixinStandardHelpOptions = true,
        description = "Scan AWS EC2 security groups using AWS SDK v2."
)
public class ScanAwsCommand implements Callable<Integer> {
    private final PolicyScanner scanner;
    private final AwsEc2ClientFactory clientFactory;
    private final JsonReportWriter jsonReportWriter;
    private final SarifReportWriter sarifReportWriter;

    @Option(names = "--region", defaultValue = "us-east-1", description = "AWS region.")
    private String region;

    @Option(names = "--endpoint-url", description = "Custom EC2 endpoint, useful for LocalStack.")
    private String endpointUrl;

    @Option(names = "--access-key", defaultValue = "test", description = "Access key used with --endpoint-url.")
    private String accessKey;

    @Option(names = "--secret-key", defaultValue = "test", description = "Secret key used with --endpoint-url.")
    private String secretKey;

    @Option(names = "--format", defaultValue = "json", description = "json or sarif.")
    private String reportFormat;

    @Option(names = {"-o", "--output"}, description = "Write report to a file instead of stdout.")
    private Path output;

    @Option(names = "--environment", description = "Environment override, for example dev, staging, or production.")
    private String environment;

    @Option(names = "--fail-on", defaultValue = "NONE", description = "Exit with code 2 at this severity or above: none, low, medium, high, critical.")
    private String failOn;

    public ScanAwsCommand(
            PolicyScanner scanner,
            AwsEc2ClientFactory clientFactory,
            JsonReportWriter jsonReportWriter,
            SarifReportWriter sarifReportWriter
    ) {
        this.scanner = scanner;
        this.clientFactory = clientFactory;
        this.jsonReportWriter = jsonReportWriter;
        this.sarifReportWriter = sarifReportWriter;
    }

    @Override
    public Integer call() {
        try (Ec2Client ec2Client = clientFactory.create(region, endpointUrl, accessKey, secretKey)) {
            List<SecurityGroup> groups = new Ec2SecurityGroupReader(ec2Client).readSecurityGroups();
            String source = endpointUrl == null ? "aws://" + region + "/ec2/security-groups" : endpointUrl;
            ScanResult result = scanner.scan(groups, new ScanContext(source, environment));
            String rendered = switch (ReportFormat.from(reportFormat)) {
                case JSON -> jsonReportWriter.write(result);
                case SARIF -> sarifReportWriter.write(result);
            };
            write(rendered);
            return ExitThreshold.from(failOn).isViolatedBy(result) ? 2 : 0;
        } catch (Exception exception) {
            System.err.println("AWS scan failed: " + exception.getMessage());
            return 1;
        }
    }

    private void write(String rendered) throws Exception {
        if (output == null) {
            System.out.println(rendered);
            return;
        }
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, rendered);
    }
}
