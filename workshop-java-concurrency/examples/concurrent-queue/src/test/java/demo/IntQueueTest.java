package demo;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(5)
class IntQueueTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("queues")
    void loadsAndUnloadsUsingMultipleThreads(final String name, final Supplier<IntQueue> queueFactory) {
        /* Given */
        final int range = 100;
        final IntQueue queue = queueFactory.get();

        /* When */
        final List<Integer> values = loadUnloadUsingQueueConcurrently(queue, range);

        /* Then */
        assertThat(values)
                .isEqualTo(IntStream.range(0, range).boxed().collect(Collectors.toList()));
    }

    private static Stream<Arguments> queues() {
        return Stream.of(
                Arguments.of("synchronized LinkedList", (Supplier<IntQueue>) LinkedListIntQueue::new),
                Arguments.of("ConcurrentLinkedQueue", (Supplier<IntQueue>) ConcurrentLinkedIntQueue::new));
    }

    private static List<Integer> loadUnloadUsingQueueConcurrently(final IntQueue queue, final int range) {
        final ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            final List<CompletableFuture<Void>> loaders = IntStream.range(0, range)
                    .mapToObj(number -> CompletableFuture.runAsync(() -> queue.add(number), executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(loaders.toArray(new CompletableFuture<?>[0])).join();

            final List<CompletableFuture<Integer>> unloaders = IntStream.range(0, range)
                    .mapToObj(number -> CompletableFuture.supplyAsync(queue::poll, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(unloaders.toArray(new CompletableFuture<?>[0])).join();

            return unloaders.stream()
                    .map(CompletableFuture::join)
                    .sorted()
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }
}
