package com.oracle.jsc.filter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 *  writes and reads objects (unsafely).
 */
public final class App {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Cookie cookie = new Cookie(UUID.randomUUID().toString());
        System.out.println("token: " + cookie.getToken());

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("cookie.bin"))) {
            out.writeObject(cookie);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("notCookie.bin"))) {
            out.writeObject(new NotCookie());
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("cookie.bin"))) {
            System.out.println("Attempting to read in cookie");

            cookie = (Cookie)in.readObject();
            System.out.println("Read in cookie " + cookie.getToken());
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("notCookie.bin"))) {
            System.out.println("Attempting to read in notCookie");

            cookie = (Cookie) in.readObject();
            System.out.println("Read in notCookie");
        }
    }
    
    private App() {}
}
