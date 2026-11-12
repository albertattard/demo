package demo;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("ForkJoinPool");

        logSection("Sum array content (sequential)");
        final long sequentialResult = sumSequentially(createArray(1_000));
        log("Sum of the array: " + sequentialResult);

        logSection("Sum array content (invoke())");
        log("Use invoke() to sum all the array's content using a RecursiveTask");
        try (ForkJoinPool pool = new ForkJoinPool()) {
            log("Invoke the sum recursive task");
            final int[] numbers = createArray(1_000);
            final long result = pool.invoke(SumRecursiveTask.of(numbers));
            log("Sum of the array: " + result);
        }

        logSection("Sum two arrays (submit())");
        log("Use submit() to queue two RecursiveTasks before waiting for their results");
        try (ForkJoinPool pool = new ForkJoinPool()) {
            final ForkJoinTask<Long> firstSum = pool.submit(SumRecursiveTask.of(createArray(1_000)));
            final ForkJoinTask<Long> secondSum = pool.submit(SumRecursiveTask.of(createArray(500)));
            log("Both sum tasks submitted");
            final long result = firstSum.join() + secondSum.join();
            log("Sum of both arrays: " + result);
        }

        logSection("Increment array content (invoke())");
        log("Use RecursiveAction to increment the array's content using a RecursiveAction");
        try (ForkJoinPool pool = new ForkJoinPool()) {
            log("Invoke the increment recursive action");
            final int[] numbers = createArray(1_000);
            pool.invoke(IncrementRecursiveAction.of(numbers));
            log("First array value: " + numbers[0]);
            log("Last array value: " + numbers[numbers.length - 1]);
        }
    }

    private static int[] createArray(final int size) {
        final int[] numbers = new int[size];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = i + 1;
        }
        return numbers;
    }

    private static long sumSequentially(final int[] numbers) {
        long sum = 0;
        for (final int number : numbers) {
            sum += number;
        }
        return sum;
    }
}
