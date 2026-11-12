package demo;

import java.util.concurrent.TimeUnit;

public final class Main {

    static void main() throws InterruptedException {
        System.out.println("The application will terminate automatically in about 40 seconds");
        TimeUnit.SECONDS.sleep(40);
    }
}
