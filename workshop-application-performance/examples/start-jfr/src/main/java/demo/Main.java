package demo;

import module java.base;

public final class Main {

    static void main() throws InterruptedException {
        System.out.println("The application will keep running until the process is terminated.");
        new CountDownLatch(1).await();
    }
}
