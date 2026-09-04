package com.oracle.jsc.exceptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App_fixed {
    private static final Logger log = LoggerFactory.getLogger(App_fixed.class);

    public static void main(String[] args) throws IOException {
        log.info("Hello! Please enter some text:");

        int in = 0;
        while (((char) in) != 'Q') {
            File outputDirectory = new File("output");
            if (!outputDirectory.exists() && !outputDirectory.mkdir()) {
                throw new IOException("Could not create output directory: " + outputDirectory.getAbsolutePath());
            }

            File file = new File(outputDirectory, "demo.txt");
            try (FileOutputStream out = new FileOutputStream(file)) {
                for (in = System.in.read(); ((char) in) != 'Q'; in = System.in.read()) {
                    out.write(in);
                }
            } catch (IOException e) {
                log.error("Could not persist console input to {}", file.getAbsolutePath(), e);
                throw e;
            }
        }
    }
}
