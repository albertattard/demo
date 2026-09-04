package com.oracle.jsc.encrypt;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class EncryptDb {
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
        System.out.println("\n~~~ setting default to \"encrypt\" ~~~");
        try (Connection connection = DriverManager.getConnection(
                database.getProperty("url"), 
                database.getProperty("user"), 
                secrets.getProperty("user"));
                Statement statement = connection.createStatement()) {
            
            statement.execute("SET GLOBAL default_table_encryption=ON");
        }
        System.out.println("\n~~~ Done! ~~~");
    }

}
