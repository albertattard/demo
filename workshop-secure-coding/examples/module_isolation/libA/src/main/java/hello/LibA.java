package hello;

import hello.dao.A_DAO;
import hello.dao.Hello;

public class LibA {
    public String hello(){
        A_DAO dao = new A_DAO();
        Hello hello = dao.getHello();

        return hello.greeting();
    }
}