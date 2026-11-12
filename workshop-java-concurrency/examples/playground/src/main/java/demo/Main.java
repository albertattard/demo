package demo;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Playground");
        log("Done");
    }

    private Main() {}
}
