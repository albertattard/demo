package com.oracle.jsc.encrypt;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public final class EncryptKeyData {
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

    private static final SecretKey KEY;
    private static final IvParameterSpec IV;

    static {
        KEY = Crypto.readKey();
        IV = Crypto.readIv();
    }

    public static void main(String[] args) {
        System.out.println("\n~~~ retrieving some data ~~~");
        try (Connection connection = DriverManager.getConnection(
                    database.getProperty("url"), 
                    database.getProperty("user"), 
                    secrets.getProperty("user"));
                Statement statement = connection.createStatement();
                PreparedStatement updateStatement = connection
                        .prepareStatement("update customer set ssn = ? where id = ?");
                ResultSet resultSet = statement.executeQuery("SELECT * FROM customer")) {

            while (resultSet.next()) {
                // Assuming 'id' is an integer column and 'name' is a string column
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String ssn = resultSet.getString("ssn");
                System.out.println("ID: " + id + ", Name: " + name + ", SSN: " + ssn + ". Encrypting and re-storing.");

                ssn = encrypt(ssn);
                updateStatement.setString(1, ssn);
                updateStatement.setInt(2, id);
                updateStatement.execute();
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

    private static String encrypt(String ssn) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(Crypto.getCipher());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            System.exit(30);
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, KEY, IV);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            System.exit(31);
        }

        byte[] cipherText = null;
        try {
            cipherText = cipher.doFinal(ssn.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            System.exit(32);
        }

        return Base64.getEncoder().encodeToString(cipherText);
    }

}
