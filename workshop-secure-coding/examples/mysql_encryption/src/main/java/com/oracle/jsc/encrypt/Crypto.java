package com.oracle.jsc.encrypt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {
    public static void main(String[] args) {
        generateAndStoreKeys();
    }

    public static String getCipher() {
        return "AES/CFB8/NoPadding";
    }

    public static void generateAndStoreKeys() {
        System.out.println("\n~~~ generating secret key and saving it in a secret file ~~~");
        SecretKey key = generateKey();
        writeKey(key);

        IvParameterSpec iv = generateIv();
        writeIv(iv);

        System.out.println("\n~~~ Done! ~~~");

        System.exit(0);
    }

    private static SecretKey generateKey() {
        KeyGenerator keygenerator = null;
        try {
            keygenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(10);
        }
        keygenerator.init(128);
        return keygenerator.generateKey();
    }

    private static IvParameterSpec generateIv() {
        byte[] initializationVector = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
        return new IvParameterSpec(initializationVector);
    }

    private static final String KEY_FILENAME = "src/main/resources/secret.key";
    private static final String IV_FILENAME = "src/main/resources/secret.iv";

    private static void writeObject(String filename, Object thing, int errorCodeOnFailure) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(thing);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(errorCodeOnFailure);
        }
    }

    private static Object readObject(String filename, int errorCodeOnFailure) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(errorCodeOnFailure);
        }
        return null;
    }

    private static void writeKey(SecretKey key) {
        writeObject(KEY_FILENAME, key, 20);
    }

    public static SecretKey readKey() {
        return (SecretKey) readObject(KEY_FILENAME, 22);
    }

    private static void writeIv(IvParameterSpec iv) {
        String s = Base64.getEncoder().encodeToString(iv.getIV());
        writeObject(IV_FILENAME, s, 21);
    }

    public static IvParameterSpec readIv() {
        return new IvParameterSpec(Base64.getDecoder().decode((String)readObject(IV_FILENAME, 23)));
    }
}
