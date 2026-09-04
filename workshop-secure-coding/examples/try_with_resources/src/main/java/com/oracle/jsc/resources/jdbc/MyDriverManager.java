package com.oracle.jsc.resources.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MyDriverManager {
    private MyDriverManager() {
        // don't instantiate this.
    }

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return new LoggingConnection(DriverManager.getConnection(url, username, password));
    }

}
