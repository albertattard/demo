package com.oracle.jsc.obfuscated;

public final class Example04 {

    public static void main(final String[] args) {
        long x = (1 << 24) + 1;
        if (x != (x += 0.0f)) {
            System.out.println("What??");
        } else {
            System.out.println("OK!");
        }
        System.out.println("x = " + x + "; ... (x += 0.0f) = " + (x += 0.0f));
    }
}
