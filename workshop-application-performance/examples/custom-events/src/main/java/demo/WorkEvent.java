package demo;

import module jdk.jfr;

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
}
