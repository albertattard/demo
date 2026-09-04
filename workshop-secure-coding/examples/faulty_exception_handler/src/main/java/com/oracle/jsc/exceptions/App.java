package com.oracle.jsc.exceptions;

import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Hello! Please enter some text:");

        int in = 0;
        while (((char)in) != 'Q') {
            File outputDirectory = new File("output");
            try {
                outputDirectory.mkdir();
                File file = new File(outputDirectory, "demo.txt");

                try (FileOutputStream out = new FileOutputStream(file)) {
                    for (in = System.in.read(); ((char)in) != 'Q'; in = System.in.read()) {
                        out.write(in);
                    }
                }
            } catch (Exception e) {
                // BUG: swallowing the exception means the application keeps looping
                // even though it can no longer make progress.
                // log.error("error", e);
            }
        }
    }
}
