package com.oracle.jsc.encrypt;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.flywaydb.core.Flyway;

public final class BuildDB {
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

    public static void main(String[] args) {
        System.out.println("~~~ setting up the database ~~~");

        Flyway flyway = Flyway.configure().dataSource(
                database.getProperty("url"), 
                database.getProperty("user"), 
                secrets.getProperty("user")).load();
        flyway.migrate();

        System.out.println("\n~~~ Done! ~~~");
    }

}
