package com.oracle.jsc.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class App {
    public static void main(String[] args) {
        List<FileOutputStream> openFiles = new ArrayList<>();
        int fileCount = 0;

        try {
            for (fileCount = 1; fileCount <= 20_000; ++fileCount) {
                // Create a temporary file
                File tempFile = File.createTempFile("demo", ".tmp");

                // Open a FileOutputStream to the file without closing it
                FileOutputStream fos = new FileOutputStream(tempFile);
                openFiles.add(fos);

                if (fileCount % 1000 == 0) {
                    System.out.println("Opened file: " + fileCount);
                }
            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e.getMessage());
            System.err.println("Number of files opened before exhaustion: " + fileCount);
            e.printStackTrace(System.err);
        } finally {
            // Attempt to close all opened file streams (important for real applications)
            for (FileOutputStream fos : openFiles) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e.getMessage());
                }
            }
            System.out.println("All opened file streams closed in the finally block.");
        }
    }
}
