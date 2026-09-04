package com.oracle.jsc.construction;

import java.io.IOException;

import com.oracle.jsc.construction.dao.User;
import com.oracle.jsc.construction.dao.UserDao;
import com.oracle.jsc.construction.dao.UserStateException;
import com.oracle.jsc.construction.dao.impl.InsecureDao;

/**
 * Hello world!
 *
 */
public final class App01 
{
    public static void main( String[] args ) throws Exception {
        //
        // Notice that to retrieve a DAO, we have to know its class, and that it can fail construction. 
        // Both of these facts leak information about a key security tool.
        //
        try (UserDao dao = new InsecureDao()) {
            User user = dao.getUser("Alice");

            System.out.println(user.name() + " is " + (user.isAdmin() ? "" : "not ") + "an admin.");

        } catch (UserStateException e) {
            System.err.println("Exception getting the DAO: " + e.getMessage());

        } catch (IOException e) {
            System.err.println("Exception closing the DAO: " + e.getMessage());
        }
    }
}
