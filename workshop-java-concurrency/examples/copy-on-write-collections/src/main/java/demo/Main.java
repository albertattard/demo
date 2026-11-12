package demo;

import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Copy on Write Collections");

        logSection("CopyOnWriteArrayList example");
        CopyOnWriteArrayListExample.run();

        logSection("CopyOnWriteArraySet example");
        CopyOnWriteArraySetExample.run();
    }
}
