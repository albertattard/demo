package com.oracle.jsc.resources;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;

import com.oracle.jsc.resources.jdbc.MyDriverManager;

public final class FunWithJDBC {
    private static final String URL = "jdbc:h2:file:./test";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        System.out.println("~~~ setting up the database ~~~");
        Flyway flyway = Flyway.configure().dataSource(URL, USER, PASS).load();
        flyway.migrate();

        System.out.println("\n~~~ retrieving some data ~~~");
        try (Connection connection = MyDriverManager.getConnection(URL, USER, PASS);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM catalogue_item")) {

            while (resultSet.next()) {
                // Assuming 'id' is an integer column and 'name' is a string column
                int id = resultSet.getInt("id");
                String name = resultSet.getString("caption");
                System.out.println("ID: " + id + ", Name: " + name);
            }

            System.out.println("\n~~~ cleaning up ~~~");
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        System.out.println("\n~~~ Done! ~~~");
    }

}
