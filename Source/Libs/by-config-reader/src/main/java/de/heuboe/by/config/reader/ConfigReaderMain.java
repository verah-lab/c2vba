package de.heuboe.by.config.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * Main class
 */
@Slf4j
@SpringBootApplication
@CommandLine.Command(subcommands = {ImportCommand.class},
        description = "With this tool, configurations are imported into the configuration service.")
public class ConfigReaderMain implements ExitCodeGenerator, CommandLineRunner, Callable<Integer> {

    private picocli.CommandLine.IFactory factory;
    private int exitCode;

//    @picocli.CommandLine.ArgGroup(validate = false, heading = "This is the first section")
//    private ImportCommand importCommand;

    @picocli.CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    // constructor injection
    ConfigReaderMain(picocli.CommandLine.IFactory factory) {
        this.factory = factory;
    }

    @Override
    public Integer call() {
        return 0;
    }

    @Override
    public void run(String... args) {
        log.info("Start ConfigTool");
        if (args.length == 0) {
            args = new String[]{"-h"};
        }
        picocli.CommandLine cmd = new picocli.CommandLine(this, factory);
        cmd.setExecutionStrategy(new picocli.CommandLine.RunLast());
        try {
            exitCode = cmd.execute(args);
        } catch (Exception e) {
            log.error("Error while running command", e);
            exitCode = -1;
        }
        log.info("Finish");
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Application entry point
     *
     * @param args command line arguments (ignored here, Spring handles those)
     */
    public static void main(String... args) {
        System.exit(execute(args));
    }

    static int execute(String... args) {
        return SpringApplication.exit(SpringApplication.run(ConfigReaderMain.class, args));
    }
}
