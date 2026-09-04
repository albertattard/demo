package com.oracle.jsc.mutable;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public final class App_fixed implements Runnable {
    // These are no longer static
    private String message = "Hello, World!";
    private boolean done = false;

    private long sleepTime = 500l;

    public static void main(String[] args) throws IOException {
        App_fixed[] apps = new App_fixed[10];   // now we're retaining a references to each App

        for (int i = 0; i < apps.length; ++i) {
            apps[i] = new App_fixed(1250 + 100 * i);
            new Thread(apps[i]).start();
        }

        //  wait for 10 seconds, then exit
        sleep(10 * 1000L);
        for (int i = 0; i < apps.length; ++i) {
            apps[i].done = true;
        }
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


    public App_fixed(long sleepTime) {
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
