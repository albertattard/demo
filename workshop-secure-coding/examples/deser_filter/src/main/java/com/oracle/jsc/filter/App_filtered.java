package com.oracle.jsc.filter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * writes and reads objects (slightly less unsafely). Requires Java 9 or higher.
 */
public final class App_filtered {
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
            in.setObjectInputFilter(getFilter());
            cookie = (Cookie) in.readObject();
            System.out.println("Read in cookie " + cookie.getToken());
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("notCookie.bin"))) {
            System.out.println("Attempting to read in notCookie");
            in.setObjectInputFilter(getFilter());
            cookie = (Cookie) in.readObject();
            System.out.println("Read in notCookie");
        }
    }

    private App_filtered() {}

    private static ObjectInputFilter getFilter() {
        // this filter allows only one class and rejects all others.
        // You can filter on a wide variety of things besides class, including size,
        // module, path, and so on.
        return ObjectInputFilter.Config.createFilter("com.oracle.jsc.filter.Cookie;!*");
    }
}
