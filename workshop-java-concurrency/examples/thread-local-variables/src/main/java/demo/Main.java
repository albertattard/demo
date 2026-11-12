package demo;

import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Thread Local");

        logSection("ThreadLocal example");
        ThreadLocalExample.run();

        logSection("ThreadLocalRandom example");
        ThreadLocalRandomExample.run();
    }
}
