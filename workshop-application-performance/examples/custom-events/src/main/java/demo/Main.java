package demo;

import module java.base;

public final class Main {

    static void main() throws InterruptedException {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        final ThreadGroup threadGroup = new ThreadGroup("WORKERS");
        for (int i = 0; i < availableProcessors; i++) {
            final String threadName = "WORKER-%02d".formatted(i + 1);
            System.out.println("Starting " + threadName + "...");

            final Thread thread = new Thread(threadGroup, new Worker(), threadName);
            thread.setDaemon(true);
            thread.start();
        }

        System.out.println("The application will terminate automatically in about 40 seconds");
        TimeUnit.SECONDS.sleep(40);
    }
}
