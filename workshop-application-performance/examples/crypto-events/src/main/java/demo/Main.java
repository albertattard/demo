package demo;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class Main {

    static void main() {
        System.out.println("A simple Java application that uses the insecure TLSv1.1 cryptographic protocol to demonstrate the crypto events");
        System.out.println("Please do not use TLSv1.1 in production as this is considered insecure.");
        System.out.println("The application will terminate automatically after about 60 seconds.");

        for (int i = 0; i < 10; i++) {
            connect("tls-v1-1.badssl.com", 1011, "TLSv1.1");
            sleepQuietly(5);
        }

        System.out.println("Done");
    }

    private static void connect(final String host, int port, final String protocol) {
        try (SSLSocket socket = createTlsSocket(host, port, protocol);
             PrintWriter out = createWriter(socket);
             BufferedReader in = createReader(socket)) {

            out.println("GET / HTTP/1.1");
            out.println("Host: " + host);
            out.println();
            if (out.checkError()) {
                throw new RuntimeException("Failed to make request");
            }

            /* Print the headers */
            for (String headerLine; (headerLine = in.readLine()) != null && !headerLine.isEmpty(); ) {
                System.out.println(headerLine);
            }

            System.out.println("Connected to " + host + " using the " + protocol + " cryptographic protocol");
        } catch (final IOException e) {
            System.out.println("Failed to connect to " + host + " using the " + protocol + " cryptographic protocol (e.getClass().getSimpleName())");
        }
    }

    private static SSLSocket createTlsSocket(final String host, final int port, final String protocol) throws IOException {
        final SocketFactory factory = SSLSocketFactory.getDefault();
        final SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        /* The insecure TLS 1.1 is used for demonstration purpose only. Please
           do not use this in production as this protocol is insecure. */
        if ("TLSv1.1".equals(protocol)) {
            System.out.println("Making use of insecure cryptographic protocol " + protocol + ". Do not use in production!!");
        }
        socket.setEnabledProtocols(new String[]{protocol});
        socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(5));
        socket.startHandshake();
        return socket;
    }

    private static PrintWriter createWriter(final Socket socket) throws IOException {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
    }

    private static BufferedReader createReader(final Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private static void sleepQuietly(final int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
    }
}
