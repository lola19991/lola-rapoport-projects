package com.lola.cloudguard.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
        name = "cloud-policy-guard",
        mixinStandardHelpOptions = true,
        version = "0.1.0",
        description = "Scan cloud network policies for risky connectivity.",
        synopsisSubcommandLabel = "COMMAND"
)
public class CloudPolicyGuardCommand implements Runnable {
    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
