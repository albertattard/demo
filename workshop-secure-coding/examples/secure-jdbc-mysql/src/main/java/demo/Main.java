package demo;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;

public final class Main {

    private static final Properties secrets = new Properties();

    static {
        try {
            secrets.load(new FileReader(".local/secret.properties"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static final String DB_USER = "demo";
    private static final String DB_PASS;
    private static final String JDBC_URL_STUB = "jdbc:mysql://localhost:3306/demo?sslMode=";
    private static final String CONNECT_TIMEOUT = "&connectTimeout=5000";

    private static final Set<String> SSL_MODES = Set.of("DISABLED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY");
    private static final String TRUST_STORE_URL = "&trustCertificateKeyStoreUrl=file:target/truststore.pkcs12";
    private static final String TRUST_STORE_PASS;

    static {
        DB_PASS = secrets.getProperty("user");
        TRUST_STORE_PASS = "&trustCertificateKeyStorePassword=" + secrets.getProperty("keys");
    }

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2 || !SSL_MODES.contains(args[0])) {
            System.err.println("Usage: Main <DISABLED|REQUIRED|VERIFY_CA|VERIFY_IDENTITY> [truststore]");
            System.exit(2);
        }

        String sslMode = args[0];
        boolean useTrustStore = args.length == 2 && "truststore".equals(args[1]);
        if (args.length == 2 && !useTrustStore) {
            System.err.println("The optional second argument must be truststore.");
            System.exit(2);
        }
        if (useTrustStore && !("VERIFY_CA".equals(sslMode) || "VERIFY_IDENTITY".equals(sslMode))) {
            System.err.println("A truststore applies only to VERIFY_CA or VERIFY_IDENTITY.");
            System.exit(2);
        }

        String insecureProbeOption = "DISABLED".equals(sslMode) ? "&allowPublicKeyRetrieval=true" : "";
        String url = JDBC_URL_STUB + sslMode + CONNECT_TIMEOUT + insecureProbeOption
                + (useTrustStore ? TRUST_STORE_URL + TRUST_STORE_PASS : "");

        System.out.printf("\nConnecting with sslMode=%s; truststore=%s%n%n", sslMode, useTrustStore ? "configured" : "not configured");
        try (Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASS); Statement setupStatement = connection.createStatement(); Statement tlsStatement = connection.createStatement(); Statement queryStatement = connection.createStatement()) {

            printSensitivePayload(queryStatement);
            if (!"DISABLED".equals(sslMode)) {
                printTlsStatus(tlsStatement);
            }

            try (ResultSet resultSet = queryStatement.executeQuery("SELECT * FROM catalogue_item")) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("caption");
                    System.out.println("ID: " + id + ", Name: " + name);
                }
            }

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("VendorError: " + e.getErrorCode());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.out.println("\n~~~ Done! ~~~");
    }

    private static void printTlsStatus(Statement statement) throws SQLException {
        System.out.println("\n~~~ TLS session evidence ~~~");
        try (ResultSet resultSet = statement.executeQuery("SHOW SESSION STATUS WHERE Variable_name IN ('Ssl_cipher', 'Ssl_version')")) {
            while (resultSet.next()) {
                System.out.println(resultSet.getString("Variable_name") + ": " + resultSet.getString("Value"));
            }
        }
    }

    private static void printSensitivePayload(Statement statement) throws SQLException {
        System.out.println("\n~~~ sensitive payload sent to MySQL ~~~");
        try (ResultSet resultSet = statement.executeQuery("""
                SELECT 'workshop-password-marker=not-a-real-password' AS password_marker,
                       'customer-note=confidential-workshop-data' AS customer_note
                """)) {
            resultSet.next();
            System.out.println(resultSet.getString("password_marker"));
            System.out.println(resultSet.getString("customer_note"));
        }
    }
}
