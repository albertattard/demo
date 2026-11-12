package demo;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logError;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;
import static demo.utils.Threads.sleepForAFewSeconds;

public final class Main {

    public static void main(final String[] args) throws InterruptedException {
        logHeaderAndJavaVersion("Thread and Runnable");


        logSection("Runnable");
        log("Start a thread using a lambda expression that implements Runnable");
        final Runnable runnable = () -> {
            log("Running task");
            sleepForAFewSeconds();
            log("Task completed");
        };

        final Thread thread1 = new Thread(runnable, "Thread-1");
        thread1.start();


        logSection("Runnable");
        log("Start a thread using an implementation of the Runnable interface");
        final Thread thread2 = new Thread(new Task(), "Thread-2");
        thread2.start();


        logSection("Thread (run())");
        log("Start a thread by overriding the Thread run() method");
        final Thread thread3 = new Thread("Thread-3") {
            @Override
            public void run() {
                log("Running task");
                sleepForAFewSeconds();
                log("Task completed");
            }
        };
        thread3.start();


        logSection("Handle Uncaught RuntimeException");
        log("using the UncaughtExceptionHandler to capture runtime exceptions that escaped the run() method");
        final Thread thread4 = new Thread(() -> {
            log("Running faulty task");
            throw new RuntimeException("Simulating an error!");
        }, "Thread-4");
        thread4.setUncaughtExceptionHandler((t, e) -> logError("The thread " + t + " failed", e));
        thread4.start();

        log("Waiting for the threads to finish");
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        log("All threads completed");
    }
}
