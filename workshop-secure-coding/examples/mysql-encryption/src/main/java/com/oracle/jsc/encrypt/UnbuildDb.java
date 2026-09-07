package com.oracle.jsc.encrypt;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class UnbuildDb {
    private static final Properties database = new Properties();
    private static final Properties secrets = new Properties();
    static {
        try {
            database.load(new FileReader("./src/main/resources/database.properties"));
            secrets.load(new FileReader("./src/main/resources/secret.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("\n~~~ dropping all tables ~~~");
        try (Connection connection = DriverManager.getConnection(
                    database.getProperty("url"), 
                    database.getProperty("user"), 
                    secrets.getProperty("user"));
                Statement statement = connection.createStatement()) {
            
            statement.execute("DROP TABLE flyway_schema_history");
            statement.execute("DROP TABLE customer");
        }
        System.out.println("\n~~~ Done! ~~~");
    }

}
