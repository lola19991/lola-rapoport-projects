package com.lola.cloudguard;

import com.lola.cloudguard.cli.CommandFactory;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
public class CloudPolicyGuardApplication {

    public static void main(String[] args) {
        if (args.length > 0 && "serve".equalsIgnoreCase(args[0])) {
            SpringApplication.run(CloudPolicyGuardApplication.class, Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        int exitCode = CommandFactory.create().execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
