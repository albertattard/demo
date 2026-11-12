package demo;

import static java.util.Objects.requireNonNull;

public final class Publication {

    private String message;
    private volatile boolean ready;

    public void publish(final String message) {
        this.message = requireNonNull(message, "The message cannot be null");
        ready = true;
    }

    public String awaitMessage() {
        while (!ready) {
            // Wait until the volatile read observes the publisher's volatile write.
        }

        return message;
    }
}
