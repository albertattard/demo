package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Timeout(value = 5, threadMode = SEPARATE_THREAD)
class DequeExampleTest {

    @Test
    void processesEveryTaskExactlyOnceWhenWorkersPollConcurrently() throws InterruptedException {
        final int numberOfTasks = 100;
        final Deque<Runnable> deque = new ConcurrentLinkedDeque<>();
        final AtomicIntegerArray executionCounts = new AtomicIntegerArray(numberOfTasks);
        final CountDownLatch firstTwoTasksStarted = new CountDownLatch(2);
        final CountDownLatch startRemainingTasks = new CountDownLatch(1);

        IntStream.range(0, numberOfTasks).forEach(taskNumber -> deque.addLast(() -> {
            firstTwoTasksStarted.countDown();
            try {
                assertThat(startRemainingTasks.await(1, TimeUnit.SECONDS)).isTrue();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting to start task", e);
            }
            executionCounts.incrementAndGet(taskNumber);
        }));

        final CompletableFuture<Integer> completedTaskCount = CompletableFuture.supplyAsync(() -> DequeExample.processTasks(deque, 2));
        try {
            assertThat(firstTwoTasksStarted.await(1, TimeUnit.SECONDS)).isTrue();
        } finally {
            startRemainingTasks.countDown();
        }

        assertThat(completedTaskCount.join()).isEqualTo(numberOfTasks);
        assertThat(IntStream.range(0, numberOfTasks).map(executionCounts::get))
                .containsOnly(1);
        assertThat(deque).isEmpty();
    }

    @Test
    void stealsOldestTasksAfterDrainingItsLocalDeque() throws InterruptedException {
        final Deque<Runnable> localDeque = new ConcurrentLinkedDeque<>();
        final Deque<Runnable> victimDeque = new ConcurrentLinkedDeque<>();
        final List<String> executedTasks = new CopyOnWriteArrayList<>();
        final CountDownLatch localTaskStarted = new CountDownLatch(1);
        final CountDownLatch completeLocalTask = new CountDownLatch(1);

        localDeque.addLast(() -> {
            localTaskStarted.countDown();
            try {
                assertThat(completeLocalTask.await(1, TimeUnit.SECONDS)).isTrue();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting to complete local task", e);
            }
            executedTasks.add("local");
        });
        victimDeque.addLast(() -> executedTasks.add("oldest victim task"));
        victimDeque.addLast(() -> executedTasks.add("newest victim task"));

        final WorkStealingExample.WorkStealingWorker worker =
                new WorkStealingExample.WorkStealingWorker(localDeque, victimDeque);
        final CompletableFuture<Void> workerCompletion = CompletableFuture.runAsync(worker);
        try {
            assertThat(localTaskStarted.await(1, TimeUnit.SECONDS)).isTrue();
        } finally {
            completeLocalTask.countDown();
        }

        workerCompletion.join();

        assertThat(executedTasks).containsExactly(
                "local",
                "oldest victim task",
                "newest victim task");
        assertThat(localDeque).isEmpty();
        assertThat(victimDeque).isEmpty();
    }

    @Test
    void executesEachTaskExactlyOnceWhenWorkerStealsWhileItsVictimPollsLocally()
            throws InterruptedException, ExecutionException, TimeoutException {
        final int numberOfTasks = 12;
        final Deque<Runnable> localDeque = new ConcurrentLinkedDeque<>();
        final Deque<Runnable> victimDeque = new ConcurrentLinkedDeque<>();
        final AtomicIntegerArray executionCounts = new AtomicIntegerArray(numberOfTasks);
        final CountDownLatch firstTasksStarted = new CountDownLatch(2);
        final CountDownLatch startRemainingTasks = new CountDownLatch(1);

        localDeque.addLast(() -> {
            firstTasksStarted.countDown();
            try {
                assertThat(startRemainingTasks.await(1, TimeUnit.SECONDS)).isTrue();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting to start task", e);
            }
            executionCounts.incrementAndGet(0);
        });
        IntStream.range(2, numberOfTasks)
                .forEach(taskNumber -> victimDeque.addLast(() -> executionCounts.incrementAndGet(taskNumber)));
        victimDeque.addLast(() -> {
            firstTasksStarted.countDown();
            try {
                assertThat(startRemainingTasks.await(1, TimeUnit.SECONDS)).isTrue();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting to start task", e);
            }
            executionCounts.incrementAndGet(1);
        });

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CompletableFuture<Void> workersComplete = CompletableFuture.allOf(
                CompletableFuture.runAsync(new WorkStealingExample.WorkStealingWorker(localDeque, victimDeque), executor),
                CompletableFuture.runAsync(new WorkStealingExample.WorkStealingWorker(victimDeque, localDeque), executor));
        try {
            assertThat(firstTasksStarted.await(1, TimeUnit.SECONDS)).isTrue();
            startRemainingTasks.countDown();
            assertThat(workersComplete.get(1, TimeUnit.SECONDS)).isNull();
        } finally {
            startRemainingTasks.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(IntStream.range(0, numberOfTasks).map(executionCounts::get))
                .containsOnly(1);
        assertThat(localDeque).isEmpty();
        assertThat(victimDeque).isEmpty();
    }
}
