package com.oracle.jsc.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class App_fixed {
    public static void main(String[] args) {
        int fileCount = 0;

        try {
            for (fileCount = 1; fileCount <= 20_000; ++fileCount) {
                // File itself is not Closeable. The resource we must release is the
                // FileOutputStream created for it.
                File tempFile = File.createTempFile("demo", ".tmp");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    // The stream is closed automatically at the end of this block.
                    fos.write(new byte[0]);
                }

                if (fileCount % 1000 == 0) {
                    System.out.println("Opened file: " + fileCount);
                }
            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e.getMessage());
            System.err.println("Number of files opened before exhaustion: " + fileCount);
        }
        System.out.println("All file streams were closed automatically by try-with-resources.");
    }
}
