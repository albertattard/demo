package com.oracle.jsc.construction;

import java.io.IOException;

import com.oracle.jsc.construction.dao.User;
import com.oracle.jsc.construction.dao.UserDao;
import com.oracle.jsc.construction.dao.UserStateException;
import com.oracle.jsc.construction.dao.impl.SecureDao;

public class App02 {
    public static void main(String[] args) {
        // still need to know exactly what class we're getting, but now there's no chance of exception on construction.
        try (UserDao dao = new SecureDao()){
            try {
                User user = dao.getUser("Bob");
                // do things with user

                System.out.println(user.name() + " is " + (user.isAdmin() ? "" : "not ") + "an admin.");

            } catch (UserStateException e) {
                System.err.println("Exception during getting the user: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Exception during closing: " + e.getMessage());
        } 
    }

}
