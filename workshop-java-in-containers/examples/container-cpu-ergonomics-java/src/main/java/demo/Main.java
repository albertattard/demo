package demo;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public final class Main {

    private static final List<String> VM_OPTIONS = List.of(
            "UseContainerSupport",
            "ActiveProcessorCount",
            "ParallelGCThreads",
            "ConcGCThreads",
            "CICompilerCount"
    );

    private static final List<Path> CGROUP_FILES = List.of(
            Path.of("/sys/fs/cgroup/cpu.max"),
            Path.of("/sys/fs/cgroup/cpu.weight"),
            Path.of("/sys/fs/cgroup/cpuset.cpus"),
            Path.of("/sys/fs/cgroup/cpuset.cpus.effective"),
            Path.of("/sys/fs/cgroup/cpu/cpu.cfs_quota_us"),
            Path.of("/sys/fs/cgroup/cpu/cpu.cfs_period_us"),
            Path.of("/sys/fs/cgroup/cpu/cpu.shares")
    );

    static void main() {
        printSection("Java runtime");
        print("java.version", System.getProperty("java.version"));
        print("java.vm.name", System.getProperty("java.vm.name"));
        print("java.vm.version", System.getProperty("java.vm.version"));
        print("os.name", System.getProperty("os.name"));
        print("os.arch", System.getProperty("os.arch"));

        printSection("Processor view");
        print("Runtime.availableProcessors", Runtime.getRuntime().availableProcessors());
        print("OperatingSystemMXBean.availableProcessors", ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
        print("ForkJoinPool.commonPool.parallelism", getParallelism());

        printSection("Garbage collectors");
        ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(GarbageCollectorMXBean::getName)
                .forEach(name -> print("collector", name));

        printSection("Selected VM options");
        final HotSpotDiagnosticMXBean diagnostics = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        VM_OPTIONS.forEach(option -> printVmOption(diagnostics, option));

        printSection("Container CPU control files");
        CGROUP_FILES.forEach(Main::printFileIfPresent);
    }

    private static int getParallelism() {
        try (var pool = ForkJoinPool.commonPool()) {
            return pool.getParallelism();
        }
    }

    private static void printVmOption(final HotSpotDiagnosticMXBean diagnostics, final String name) {
        try {
            final var option = diagnostics.getVMOption(name);
            print(name, option.getValue() + " (" + option.getOrigin() + ")");
        } catch (final IllegalArgumentException e) {
            print(name, "not available");
        }
    }

    private static void printFileIfPresent(final Path path) {
        if (Files.isRegularFile(path)) {
            try {
                final String contents = Files.readString(path).strip();
                print(path.toString(), contents.isEmpty() ? "empty" : contents);
            } catch (final IOException e) {
                print(path.toString(), "could not read: " + e.getMessage());
            }
        } else {
            print(path.toString(), "not present");
        }
    }

    private static void printSection(final String title) {
        System.out.println();
        System.out.println("[" + title + "]");
    }

    private static void print(final String name, final Object value) {
        System.out.printf("%-48s %s%n", name, value);
    }

    private Main() {}
}
