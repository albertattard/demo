package com.oracle.jsc.modules;

import hello.LibA;
import hello.dao.A_DAO;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        A_DAO dao = new A_DAO();
        System.out.println(dao.getHello().greeting());

        System.out.println(new LibA().hello());
    }
}
