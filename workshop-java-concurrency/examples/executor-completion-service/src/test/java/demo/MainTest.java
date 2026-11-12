package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

final class MainTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void reportsEveryCompletionIncludingFailure() throws Exception {
        final PrintStream original = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, "UTF-8")) {
            System.setOut(capture);
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }

        final List<String> messages = Arrays.stream(output.toString("UTF-8").split("\\R"))
                .map(line -> line.replaceFirst("^\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\] \\[.*\\] ", ""))
                .collect(Collectors.toList());

        assertThat(messages).hasSize(6);
        assertThat(messages.subList(0, 3)).containsExactly(
                "Executor Completion Service",
                "Using Java " + System.getProperty("java.version"),
                "Submitting: first, second, third");
        assertThat(messages.subList(3, 6))
                .containsExactlyInAnyOrder(
                        "Completed: first: result",
                        "Completed with failure: java.lang.IllegalStateException: second: failed",
                        "Completed: third: result");
    }
}
