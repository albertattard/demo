package com.oracle.jsc.mutable;

import java.io.IOException;

public final class App implements Runnable {
    // These are static, and therefore global, variables.
    // right away, you can guess this is not thread safe.
    private static String message = "Hello, World!";
    private static boolean done = false;

    private long sleepTime = 500L;

    public static void main(String[] args) throws IOException {
        // launch 10 threads, each with a slightly different sleep time.
        for (int i = 0; i < 10; ++i) {
            new Thread(new App(1250 + 100 * i)).start();
        }
        
        //  wait for 10 seconds, then exit
        sleep(10 * 1000L);
        done = true;
    }

    // encapsulate the try/catch block for demo clarity
    private static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            // ignored
            // in a real app, you would take some action here
        }
    }

    public App(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        while (!done) {
            System.out.println(message);    // this isn't thread safe either; but this is a demo
            sleep(sleepTime/2);

            // we intentionally ensure that multiple threads will update message simultaneously
            message = "Hello from ";
            sleep(sleepTime/2);

            message += Thread.currentThread().getName();
        }
    }
}
