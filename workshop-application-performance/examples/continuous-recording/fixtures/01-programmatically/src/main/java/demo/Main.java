package demo;

import module java.base;
import module jdk.jfr;

import jdk.jfr.Configuration;

public final class Main {

    static void main() throws Exception {
        try (Recording recording = new Recording(Configuration.getConfiguration("default"))) {
            recording.setDestination(Path.of("./jfr/demo-programmatically.jfr"));
            recording.setName("Start-JFR");
            recording.start();

            /* Wait while capturing the events and saved the to the recording. */
            System.out.println("The application will terminate automatically in about 40 seconds");
            TimeUnit.SECONDS.sleep(40);
        }
        /* The recording is completed and written to file. */
        /* No more events are captured by this recording. */
    }
}
