package demo;

import demo_a.LibA;
import demo_b.LibB;

public final class Main {
    public static void main(final String[] args) {
        System.out.println("Hello");
        System.out.println("LibB: " + new LibB().sayHello());
        System.out.println("LibA: " + new LibA().hello());
    }
}
