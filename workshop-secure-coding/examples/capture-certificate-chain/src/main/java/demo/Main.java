package demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.cert.Certificate;
import java.util.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


/**
 * This example demonstrates how to use a SSLSocket as client to send a HTTP request and get response from an HTTPS server.
 * </p><p>
 * see <a ahref="https://docs.oracle.com/javase/10/security/sample-code-illustrating-secure-socket-connection-client-and-server.htm">
 *      https://docs.oracle.com/javase/10/security/sample-code-illustrating-secure-socket-connection-client-and-server.htm</a>.
 * </p><p>
 * Latest security developers guide: https://docs.oracle.com/en/java/javase/25/security/security-developer-guide.pdf
 * </p><p>
 * It assumes that the client is not behind a firewall.
 */
public class Main {
    private static PrintStream certificateOutput = System.out;

    public static void main(String[] args) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, new TrustManager[] { new VeryTrustingTrustManager() }, null);

        SSLSocketFactory factory = sslContext.getSocketFactory();

        try (SSLSocket socket = (SSLSocket) factory.createSocket("www.oracle.com", 443)) {
            /*
             * send http request
             *
             * Before any application data is sent or received, the SSL socket will handshake; we want the certs from that.
             *
             * SSL handshaking can be initiated by either flushing data down the pipe, or by starting the handshaking by hand.
             *
             * Handshaking is started manually in this example because PrintWriter catches all IOExceptions (including
             * SSLExceptions), sets an internal error flag, and then returns without rethrowing the exception.
             *
             * Unfortunately, this means any error messages are lost, which caused lots of confusion for others using this
             * code. The only way to tell there was an error is to call PrintWriter.checkError().
             */
            socket.startHandshake();
            SSLSession session = socket.getSession();

            for (Certificate cert : session.getPeerCertificates()) {

                //  here's where you open a new file for each certificate you want to save.

                certificateOutput.println("-----BEGIN CERTIFICATE-----");
                String encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
                for (int i = 0; i < encodedCert.length(); i += 64) {
                    certificateOutput.println(encodedCert.substring(i, Math.min(i + 64, encodedCert.length())));
                } 
                certificateOutput.println("-----END CERTIFICATE-----");
            }

            PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter( socket.getOutputStream())));

            out.println("GET / HTTP/1.0");
            out.println("Host: www.oracle.com");
            out.println("Accept: text/html");
            out.println("Accept-Language: en-us");
            out.println("Accept-Charset: utf-8");
            out.println("Cache-Control: no-cache");

            out.println();
            out.flush();

            /*
             * Make sure there were no surprises
             */
            if (out.checkError()) System.out.println( "Main:  java.io.PrintWriter error");

            /* read response */
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);

            }
        }
    }
}
