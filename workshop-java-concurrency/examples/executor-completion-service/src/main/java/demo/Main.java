package demo;

import java.util.concurrent.*;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;

public final class Main {

    public static void main(final String[] args) throws InterruptedException {
        logHeaderAndJavaVersion("Executor Completion Service");

        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final CompletionService<String> completed = new ExecutorCompletionService<>(executor);

        try {
            log("Submitting: first, second, third");
            completed.submit(() -> {
                TimeUnit.MILLISECONDS.sleep(500);
                return "first: result";
            });
            completed.submit(() -> {throw new IllegalStateException("second: failed");});
            completed.submit(() -> "third: result");

            for (int i = 0; i < 3; i++) {
                final Future<String> future = completed.take();
                try {
                    log("Completed: " + future.get());
                } catch (final ExecutionException e) {
                    log("Completed with failure: " + e.getCause());
                }
            }
        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log("Executor did not terminate within five seconds");
            }
        }
    }
}
