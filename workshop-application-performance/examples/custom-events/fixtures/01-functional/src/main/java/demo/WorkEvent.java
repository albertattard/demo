package demo;

import module java.base;
import module jdk.jfr;

import jdk.jfr.Label;

import static java.util.Objects.requireNonNull;

@Name("demo.customevents.WorkEvent")
@Label("Work Event")
@Description("Data from one loop run in the worker thread")
@Category({"Demo", "Custom Events"})
public final class WorkEvent extends Event {

    @Label("Transaction Id")
    @Description("The unique id that identifies the transaction")
    private long transactionId;

    public void transactionId(final long transactionId) {
        this.transactionId = transactionId;
    }

    public static void accept(final Consumer<WorkEvent> consumer) {
        requireNonNull(consumer);

        final WorkEvent event = new WorkEvent();
        event.begin();
        try {
            consumer.accept(event);
        } finally {
            event.commit();
        }
    }
}
