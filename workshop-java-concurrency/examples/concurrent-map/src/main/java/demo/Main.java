package demo;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Concurrent Map");

        /* Array of words to simulate multiple threads updating the map */
        final String[] words = {"apple", "banana", "apple", "orange", "banana", "apple"};

        log("Counting word occurrences using a HashMap protected by external synchronization");
        final WordCount hashMapWordCount = new HashMapWordCount();
        countWords(hashMapWordCount, words);

        log("Counting words occurrences using the thread safe Collections.synchronizedMap()");
        final WordCount synchronizedMapWordCount = new SynchronizedMapWordCount();
        countWords(synchronizedMapWordCount, words);

        log("Counting words occurrences using the thread safe ConcurrentHashMap");
        final WordCount concurrentHashMapWordCount = new ConcurrentHashMapWordCount();
        countWords(concurrentHashMapWordCount, words);

        /* Print the final word counts */
        log("Final word counts:");
        log(" HashMap + external synchronization: " + hashMapWordCount);
        log(" Collections.synchronizedMap(): " + synchronizedMapWordCount);
        log(" ConcurrentHashMap:             " + concurrentHashMapWordCount);
    }

    private static void countWords(final WordCount wordCount, final String... words) {
        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(words.length);
        try {
            CompletableFuture.allOf(
                            Arrays.stream(words)
                                    .map(word -> CompletableFuture.runAsync(() -> wordCount.merge(word), executor))
                                    .toArray(CompletableFuture[]::new))
                    .join();
        } finally {
            executor.shutdown();
        }
    }
}
