package demo;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logError;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;
import static demo.utils.Threads.sleepForTwoSeconds;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("CompletableFuture");

        logSection("Run a task on a thread (runAsync())");
        log("Use runAsync() to run a task in the background");
        final Object result1 = CompletableFuture.runAsync(Task.runnable("Task 1"))
                .join(); /* Wait for task to complete before running the next example and obtain the value
                             (which is always null as this is a runnable) */
        log("This task implements Runnable, thus join() does not return a value (" + result1 + ')');


        logSection("Wait for the task (join())");
        log("Use join() to wait for task to complete and retrieve its value");
        final String result2 = CompletableFuture.supplyAsync(Task.supplier("Task 2"))
                .join(); /* Wait for task to complete before running the next example and obtain the value */
        log("This task implements Supplier, thus join() returns a value (" + result2 + ')');


        logSection("Act on completion (whenComplete())");
        log("Use whenComplete() to handle completion.");
        final Object result3 = CompletableFuture.runAsync(Task.runnable("Task 3"))
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        logError("Failed", exception);
                    } else {
                        log("Completed.");
                        log("This task implements runnable, thus it does not return a value (" + result + ')');
                    }
                })
                .join(); /* Wait for task to complete and obtain its original result. */
        log("The whenComplete() observes the result; join() still returns it (" + result3 + ')');


        logSection("Handle result when completed (whenComplete())");
        log("Use whenComplete() to handle the result.");
        final String result4 = CompletableFuture.supplyAsync(Task.supplier("Task 4"))
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        logError("Failed", exception);
                    } else {
                        log(result + " completed");
                    }
                })
                .join();
        log("The whenComplete() observes the result; join() still returns it (" + result4 + ')');


        logSection("Handle errors and results (handle())");
        log("Use handle() to fall back to a default value when supplier fails, or process the result");
        final String result5 = CompletableFuture.supplyAsync(Task.failingSupplier("Task 5"))
                .handle((result, exception) -> {
                    if (exception != null) {
                        logError("Error occurred in Task 5", exception);
                        return "Fallback Task 5"; /* Return a fallback value */
                    }
                    return result.toUpperCase();
                })
                .join();
        log("The handle() processes the returned value, thus join() returns a value (" + result5 + ')');


        logSection("Fallback on error (exceptionally())");
        log("Use exceptionally() to fall back to a default value when supplier fails");
        final String result6 = CompletableFuture.supplyAsync(Task.failingSupplier("Task 6"))
                .exceptionally(throwable -> {
                    logError("Error occurred while handling Task 6", throwable);
                    return "Fallback Task 6"; /* Return a fallback value */
                })
                .join();
        log("The exceptionally() processes exceptions and returns a fallback value, thus join() returns a value (" + result6 + ')');


        logSection("Cancelling a CompletableFuture");
        log("Use cancel() to cancel a future; it does not interrupt the running task");
        final CompletableFuture<Void> task7Started = new CompletableFuture<>();
        final CompletableFuture<Void> task7Completed = new CompletableFuture<>();
        final CompletableFuture<Void> task7 = CompletableFuture.runAsync(() -> {
            log("Task 7 started");
            task7Started.complete(null);
            sleepForTwoSeconds();
            log("Task 7 complete");
            task7Completed.complete(null);
        });
        task7Started.join();
        task7.cancel(true);
        try {
            /* Join throws a CancellationException when the future is cancelled. */
            task7.join();
        } catch (final CancellationException e) {
            log("Task 7’s future was cancelled while its action was running");
        }
        task7Completed.join();
        log("Task 7’s action completed after its future was cancelled");


        logSection("Combining Futures (thenCombine())");
        log("Use thenCombine() to run two independent tasks and combine their results.");
        final CompletableFuture<String> task8 = CompletableFuture.supplyAsync(() -> "Hello");
        final CompletableFuture<String> task9 = CompletableFuture.supplyAsync(() -> "World");
        final String result8 = task8.thenCombine(task9, (a, b) -> a + " " + b + "!")
                .join();
        log("The combined result (" + result8 + ')');


        logSection("Dependent then parallel work (thenCompose() and thenCombine())");
        log("Use thenCompose() for work that needs a prior result, then thenCombine() for independent work.");
        final String result10 = CompletableFuture.supplyAsync(() -> {
                    log("Fetching user ID...");
                    return "User123";
                })
                .thenCompose(userId ->
                        CompletableFuture.supplyAsync(() -> {
                                    log("Fetching data for " + userId);
                                    sleepForTwoSeconds();
                                    return "Data for " + userId;
                                })
                                .thenCombine(CompletableFuture.supplyAsync(() -> {
                                    log("Fetching preferences for " + userId);
                                    return "Preferences for " + userId;
                                }), (data, preferences) -> userId + " | " + data + " | " + preferences))
                .join();
        log("The combined result (" + result10 + ')');


        logSection("Wait for all tasks to complete (allOf())");
        log("Use allOf() to wait for all tasks to complete");
        final CompletableFuture<Void> task11 = CompletableFuture.runAsync(Task.runnable("Task 11"));
        final CompletableFuture<Void> task12 = CompletableFuture.runAsync(Task.runnable("Task 12"));
        final CompletableFuture<Void> task13 = CompletableFuture.runAsync(Task.runnable("Task 13"));
        CompletableFuture.allOf(task11, task12, task13)
                .join();/* Wait for all tasks to complete */
        log("All tasks are done.");


        /* Return the value from the first to complete */
        logSection("Wait for the first task to complete (anyOf())");
        log("Use anyOf() to obtain the first result to complete; it does not cancel the other futures");
        final CompletableFuture<String> task14 = CompletableFuture.supplyAsync(Task.supplier("Task 14"));
        final CompletableFuture<String> task15 = CompletableFuture.supplyAsync(Task.supplier("Task 15"));
        final CompletableFuture<String> task16 = CompletableFuture.supplyAsync(Task.supplier("Task 16"));
        final CompletableFuture<Object> firstCompletedTask = CompletableFuture.anyOf(task14, task15, task16)
                .whenComplete((value, throwable) -> {
                    if (throwable != null) {
                        logError("Failed", throwable);
                    } else {
                        log(value + " completed first");
                    }
                });
        firstCompletedTask.join(); /* Wait for the first task to complete. */
        log("At least one task completed; the other tasks were not cancelled.");
        CompletableFuture.allOf(task14, task15, task16).join();
        log("All tasks are done.");
    }
}
