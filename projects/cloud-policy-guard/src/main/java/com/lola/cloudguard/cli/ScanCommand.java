package com.lola.cloudguard.cli;

import com.lola.cloudguard.domain.ScanContext;
import com.lola.cloudguard.domain.ScanResult;
import com.lola.cloudguard.domain.SecurityGroup;
import com.lola.cloudguard.parser.SecurityGroupYamlParser;
import com.lola.cloudguard.parser.TerraformPlanParser;
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
import picocli.CommandLine.Parameters;

@Command(
        name = "scan",
        mixinStandardHelpOptions = true,
        description = "Scan a YAML security-group document or Terraform plan JSON file."
)
public class ScanCommand implements Callable<Integer> {
    private final PolicyScanner scanner;
    private final SecurityGroupYamlParser yamlParser;
    private final TerraformPlanParser terraformPlanParser;
    private final JsonReportWriter jsonReportWriter;
    private final SarifReportWriter sarifReportWriter;

    @Parameters(index = "0", description = "Input file. Supports YAML or terraform show -json output.")
    private Path input;

    @Option(names = "--input-format", defaultValue = "auto", description = "auto, yaml, or terraform-plan.")
    private String inputFormat;

    @Option(names = "--format", defaultValue = "json", description = "json or sarif.")
    private String reportFormat;

    @Option(names = {"-o", "--output"}, description = "Write report to a file instead of stdout.")
    private Path output;

    @Option(names = "--environment", description = "Environment override, for example dev, staging, or production.")
    private String environment;

    @Option(names = "--fail-on", defaultValue = "NONE", description = "Exit with code 2 at this severity or above: none, low, medium, high, critical.")
    private String failOn;

    public ScanCommand(
            PolicyScanner scanner,
            SecurityGroupYamlParser yamlParser,
            TerraformPlanParser terraformPlanParser,
            JsonReportWriter jsonReportWriter,
            SarifReportWriter sarifReportWriter
    ) {
        this.scanner = scanner;
        this.yamlParser = yamlParser;
        this.terraformPlanParser = terraformPlanParser;
        this.jsonReportWriter = jsonReportWriter;
        this.sarifReportWriter = sarifReportWriter;
    }

    @Override
    public Integer call() {
        try {
            List<SecurityGroup> groups = parseInput();
            ScanResult result = scanner.scan(groups, new ScanContext(input.toString(), environment));
            String rendered = render(result);
            write(rendered);
            return ExitThreshold.from(failOn).isViolatedBy(result) ? 2 : 0;
        } catch (Exception exception) {
            System.err.println("Scan failed: " + exception.getMessage());
            return 1;
        }
    }

    private List<SecurityGroup> parseInput() throws Exception {
        ScanInputFormat resolved = resolveInputFormat();
        return switch (resolved) {
            case YAML -> yamlParser.parse(input);
            case TERRAFORM_PLAN -> terraformPlanParser.parse(input);
            case AUTO -> throw new IllegalStateException("Auto input format was not resolved");
        };
    }

    private ScanInputFormat resolveInputFormat() {
        ScanInputFormat requested = ScanInputFormat.from(inputFormat);
        if (requested != ScanInputFormat.AUTO) {
            return requested;
        }
        String filename = input.getFileName().toString().toLowerCase();
        if (filename.endsWith(".tfplan.json") || filename.contains("terraform") || filename.endsWith(".tf.json")) {
            return ScanInputFormat.TERRAFORM_PLAN;
        }
        return ScanInputFormat.YAML;
    }

    private String render(ScanResult result) throws Exception {
        return switch (ReportFormat.from(reportFormat)) {
            case JSON -> jsonReportWriter.write(result);
            case SARIF -> sarifReportWriter.write(result);
        };
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
