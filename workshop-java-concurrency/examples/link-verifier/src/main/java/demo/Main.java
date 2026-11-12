package demo;

import picocli.CommandLine;

public final class Main {

    static void main(final String[] args) {
        System.exit(new CommandLine(new LinkVerifierCommand()).execute(args));
    }

    private Main() {}
}
