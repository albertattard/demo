package com.oracle.jsc.jsp_auth;

public class AttackLogger {

    public static boolean isKnown(String username) {
        System.err.println("isKnown(" + username + ")?");
        if (username != null && username.toUpperCase().equals("JERRY")) {
            return true;
        }
        return false;
    }
    
    public static boolean log(String username) {
        System.err.println(username + " login failed.");
        return true;
    }
}
