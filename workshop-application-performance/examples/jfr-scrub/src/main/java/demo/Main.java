package demo;

import java.util.concurrent.TimeUnit;

public final class Main {

    public static void main(final String[] args) throws InterruptedException {
        System.out.println("The application will terminate automatically in about 40 seconds");
        TimeUnit.SECONDS.sleep(40);
    }
}
