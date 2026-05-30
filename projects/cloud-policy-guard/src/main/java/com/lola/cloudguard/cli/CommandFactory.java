package com.lola.cloudguard.cli;

import com.lola.cloudguard.aws.AwsEc2ClientFactory;
import com.lola.cloudguard.parser.SecurityGroupYamlParser;
import com.lola.cloudguard.parser.TerraformPlanParser;
import com.lola.cloudguard.policy.DefaultPolicyCatalog;
import com.lola.cloudguard.report.JsonReportWriter;
import com.lola.cloudguard.report.SarifReportWriter;
import com.lola.cloudguard.scan.PolicyScanner;
import picocli.CommandLine;

public final class CommandFactory {
    private CommandFactory() {
    }

    public static CommandLine create() {
        PolicyScanner scanner = new PolicyScanner(DefaultPolicyCatalog.rules());
        JsonReportWriter jsonReportWriter = new JsonReportWriter();
        SarifReportWriter sarifReportWriter = new SarifReportWriter();

        CommandLine commandLine = new CommandLine(new CloudPolicyGuardCommand());
        commandLine.addSubcommand("scan", new ScanCommand(
                scanner,
                new SecurityGroupYamlParser(),
                new TerraformPlanParser(),
                jsonReportWriter,
                sarifReportWriter
        ));
        commandLine.addSubcommand("scan-aws", new ScanAwsCommand(
                scanner,
                new AwsEc2ClientFactory(),
                jsonReportWriter,
                sarifReportWriter
        ));
        return commandLine;
    }
}
