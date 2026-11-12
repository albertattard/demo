package demo;

import java.io.Serial;
import java.util.concurrent.RecursiveAction;

import static demo.utils.Logs.log;

public final class IncrementRecursiveAction extends RecursiveAction {

    @Serial
    private static final long serialVersionUID = -1L;

    private static final int THRESHOLD = 100;
    private final int[] numbers;
    private final int start;
    private final int end;

    public static RecursiveAction of(final int[] numbers) {
        return new IncrementRecursiveAction(numbers, 0, numbers.length);
    }

    private IncrementRecursiveAction(final int[] numbers, final int start, final int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (end - start <= THRESHOLD) {
            log("Increment values between indices [" + start + "] and [" + end + ']');
            for (int i = start; i < end; i++) {
                numbers[i]++;
            }
            return;
        }

        final int middle = (start + end) / 2;
        invokeAll(new IncrementRecursiveAction(numbers, start, middle),
                new IncrementRecursiveAction(numbers, middle, end));
    }
}
