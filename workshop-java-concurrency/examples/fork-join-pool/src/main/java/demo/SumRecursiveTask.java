package demo;

import java.io.Serial;
import java.util.concurrent.RecursiveTask;

import static demo.utils.Logs.log;

public final class SumRecursiveTask extends RecursiveTask<Long> {

    @Serial
    private static final long serialVersionUID = -1L;

    private static final int THRESHOLD = 100; /* Task size threshold */
    private final int[] numbers;
    private final int start;
    private final int end;

    public static RecursiveTask<Long> of(final int[] numbers) {
        return new SumRecursiveTask(numbers, 0, numbers.length);
    }

    private SumRecursiveTask(final int[] numbers, final int start, final int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        /* If the task is small enough, compute directly */
        if (end - start <= THRESHOLD) {
            log("Compute the sum between indices [" + start + "] and [" + end + ']');
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += numbers[i];
            }
            return sum;
        }

        /* Split the task into two subtasks */
        final int middle = (start + end) / 2;
        final SumRecursiveTask leftTask = new SumRecursiveTask(numbers, start, middle);
        final SumRecursiveTask rightTask = new SumRecursiveTask(numbers, middle, end);

        /* Fork the subtasks and join their results */
        leftTask.fork(); /* Asynchronously execute the left task */
        final long rightResult = rightTask.compute(); /* Compute the right task */
        final long leftResult = leftTask.join(); /* Wait for the left task */

        /* Combine the results */
        return leftResult + rightResult;
    }
}
