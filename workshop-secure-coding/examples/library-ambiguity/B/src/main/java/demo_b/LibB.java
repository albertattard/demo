package demo_b;

import demo_a.LibA;

public class LibB {
    public String sayHello(){
        return new LibA().hello();
    }
}