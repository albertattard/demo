package demo;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualThreadsTest {

    @Test
    void virtualThreadsAreDaemonThreadsAndJoinPublishesTheirCompletedWork() throws InterruptedException {
        final StringBuilder completedWork = new StringBuilder();
        final Thread thread = Thread.ofVirtual()
                .name("test-request")
                .unstarted(() -> completedWork.append("complete"));

        assertThat(thread.isVirtual()).isTrue();
        assertThat(thread.isDaemon()).isTrue();
        assertThat(thread.getName()).isEqualTo("test-request");

        thread.start();

        assertThat(thread.join(Duration.ofSeconds(1))).isTrue();
        assertThat(completedWork).hasToString("complete");
    }
}
