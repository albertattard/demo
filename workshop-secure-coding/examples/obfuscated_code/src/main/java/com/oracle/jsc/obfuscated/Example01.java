package com.oracle.jsc.obfuscated;

public final class Example01 {

    public static void main(final String[] args) {
        final Integer a = 42;
        final Integer b = 42;
        System.out.println(a == b);

        final Integer c = 666;
        final Integer d = 666;
        System.out.println(c == d);
    }
}
