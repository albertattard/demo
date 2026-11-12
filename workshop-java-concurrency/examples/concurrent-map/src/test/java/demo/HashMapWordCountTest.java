package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.nCopies;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Timeout(value = 20, threadMode = SEPARATE_THREAD)
class HashMapWordCountTest {

    @Test
    void countTheWordsUsingMultipleThreads() {
        /* Given */
        final Map<String, Integer> source = new HashMap<>();
        source.put("apple", 4);
        source.put("banana", 2);
        source.put("blueberry", 3);
        source.put("grapes", 1);
        source.put("kiwi", 9);
        source.put("mango", 6);
        source.put("orange", 7);
        source.put("pineapple", 4);
        source.put("strawberry", 8);
        source.put("watermelon", 5);

        /* Flatten the words */
        final String[] words = source.entrySet().stream()
                .flatMap(entry -> nCopies(entry.getValue(), entry.getKey()).stream())
                .toArray(String[]::new);

        final WordCount hashMapWordCount = new HashMapWordCount();

        /* When */
        countWords(hashMapWordCount, words);

        /* Then */
        assertTrue(hashMapWordCount.match(source));
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