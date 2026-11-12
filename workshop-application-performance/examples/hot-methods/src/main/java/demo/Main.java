package demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Main {

    public static void main(final String[] args) {
        System.out.println("The application will terminate automatically in about 40 seconds");

        try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(2)) {
            final Future<?> future = executor.submit(() -> {
                final Map<Long, Integer> timeTakenA = new HashMap<>();

                while (!Thread.currentThread().isInterrupted()) {
                    final HolderOfUniqueValues i1 = new HolderOfUniqueValues();
                    final HolderOfUniqueValues i2 = new HolderOfUniqueValues();
                    i1.initialize(3);
                    i2.initialize(5);

                    final long start = System.currentTimeMillis();
                    final int intersectionSize = i1.countIntersection(i2);
                    if (intersectionSize != 5333) {
                        throw new IllegalStateException("This is awkward! Have you changed anything?");
                    }
                    final long end = System.currentTimeMillis();
                    final long takenInMills = end - start;
                    final int count = timeTakenA.computeIfAbsent(takenInMills, key -> 0);
                    timeTakenA.put(takenInMills, count + 1);
                }

                final long totalTime = timeTakenA.entrySet().stream().mapToLong(e -> e.getValue() * e.getKey()).sum();
                final long numberOfInvocations = timeTakenA.values().stream().mapToLong(l -> l).sum();
                final double averageTime = totalTime * 1D / numberOfInvocations;
                System.out.printf("The method average execution time was %.2f ms%n", averageTime);
            });

            executor.schedule(() -> future.cancel(true), 40, TimeUnit.SECONDS);
        }
    }
}
