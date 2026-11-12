package demo.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Logs {

    public static void logHeaderAndJavaVersion(final String header) {
        log(header);
        log("Using Java " + System.getProperty("java.version"));
    }

    public static void logSection(final String section) {
        log("---");
        log(section);
    }

    public static void log(final String message) {
        System.out.println(messagePrefix() + ' ' + message);
    }

    public static void logError(final String message, final Throwable exception) {
        System.out.println(messagePrefix() + ' ' + message);
        exception.printStackTrace(System.out);
    }

    private static String messagePrefix() {
        return "[" + currentTimeAsString() + "] [" + currentThreadAsString() + ']';
    }

    private static String currentTimeAsString() {
        return DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }

    private static String currentThreadAsString() {
        final int width = 64;
        final String threadToString = Thread.currentThread().toString();
        return threadToString.length() > width
                ? threadToString.substring(0, width)
                : padLeft(threadToString, width);
    }

    private static String padLeft(final String input, final int length) {
        final int paddingSize = length - input.length();
        if (paddingSize <= 0) {return input;}

        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < paddingSize; i++) {
            sb.append(' ');
        }
        sb.append(input);
        return sb.toString();
    }

    private Logs() {}
}
