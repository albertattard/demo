package demo;

import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Atomic Variables");

        logSection("AtomicBoolean example");
        AtomicBooleanExample.run();

        logSection("AtomicInteger example");
        AtomicIntegerExample.run();

        logSection("AtomicReference example");
        AtomicReferenceExample.run();

        logSection("AtomicStampedReference example");
        AtomicStampedReferenceExample.run();

        logSection("AtomicIntegerArray example");
        AtomicIntegerArrayExample.run();
    }
}
