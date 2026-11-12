package demo;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationTest {

    @Test
    void publishesTheMessageBeforeTheReaderObservesReady() throws InterruptedException {
        final Publication publication = new Publication();
        final String[] observed = new String[1];
        final Thread reader = new Thread(() -> observed[0] = publication.awaitMessage());

        reader.setDaemon(true);
        reader.start();
        publication.publish("published before ready");
        reader.join(TimeUnit.SECONDS.toMillis(1));

        assertThat(reader.isAlive()).isFalse();
        assertThat(observed[0]).isEqualTo("published before ready");
    }
}
