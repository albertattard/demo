package demo;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Main {

    public static void main(final String[] args) {
        System.out.println("The application will terminate automatically in about 40 seconds");

        try (ScheduledExecutorService service = Executors.newScheduledThreadPool(2)) {
            final Future<?> future = service.submit(new FaultyWorker());
            service.schedule(() -> future.cancel(true), 40, TimeUnit.SECONDS);
        }
    }
}
