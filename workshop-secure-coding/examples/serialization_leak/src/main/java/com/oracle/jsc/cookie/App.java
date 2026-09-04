package com.oracle.jsc.cookie;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

public class App 
{
    public static void main( String[] args ) throws IOException
    {
        Cookie cookie = new Cookie(UUID.randomUUID().toString());
        System.out.println("token: " + cookie.getToken());
        
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("cookie.bin"))) {
            out.writeObject(cookie);
        }
    }
}
