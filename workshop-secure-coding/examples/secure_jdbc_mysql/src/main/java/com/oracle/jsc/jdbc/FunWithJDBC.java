package com.oracle.jsc.jdbc;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.flywaydb.core.Flyway;

public final class FunWithJDBC {
    private static final Properties secrets = new Properties();
    static {
        try {
            secrets.load(new FileReader("./src/main/resources/secret.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String DB_USER = "jsc";
    private static final String DB_PASS;
    private static final String JDBC_URL_STUB = "jdbc:mysql://158.101.109.115:3306/jsc?sslMode=";

    private static final String TRUST_STORE_URL = "&trustCertificateKeyStoreUrl=file:./src/main/resources/truststore.pkcs12";
    private static final String TRUST_STORE_PASS;

    static {
        DB_PASS = secrets.getProperty("user");
        TRUST_STORE_PASS = "&trustCertificateKeyStorePassword=" + secrets.getProperty("keys");
    }

    public static void main(String[] args) {
        // Step 1: DISABLED (insecure)
        // Step 2: REQUIRED (encrypted)
        // Step 3: VERIFY_CA (trusted CA)
        // Step 4: VERIFY_IDENTITY (full verification)

        String sslMode = "VERIFY_IDENTITY";

        String url = JDBC_URL_STUB + sslMode + TRUST_STORE_URL + TRUST_STORE_PASS;

        System.out.printf("\nUsing URL: %s\n\n", url);
        System.out.println("~~~ setting up the database ~~~");

        Flyway flyway = Flyway.configure().dataSource(url, DB_USER, DB_PASS).load();
        flyway.migrate();

        System.out.println("\n~~~ retrieving some data ~~~");
        try (Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASS);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM catalogue_item")) {

            while (resultSet.next()) {
                // Assuming 'id' is an integer column and 'name' is a string column
                int id = resultSet.getInt("id");
                String name = resultSet.getString("caption");
                System.out.println("ID: " + id + ", Name: " + name);
            }

            System.out.println("\n~~~ cleaning up ~~~");
            // NOP
        } catch (SQLException e) {
            // handle any errors
            System.err.println("SQLException: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("VendorError: " + e.getErrorCode());
            e.printStackTrace(System.err);
        }

        System.out.println("\n~~~ Done! ~~~");
    }

}
