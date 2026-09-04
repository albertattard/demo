package com.oracle.jsc.limit_check;

import java.nio.BufferOverflowException;

public final class App_fixed {
    private static final long MAX_BUFFER_CAPACITY = 10_000;

    public static void main(String[] args) {
        // Each subtraction is performed in long arithmetic because the left-hand
        // operand is already a long.
        long available = MAX_BUFFER_CAPACITY - RemoteServiceProxy.headerLength() - RemoteServiceProxy.bodyLength();

        if (available < 0) {
            System.err.println("I'm out of buffer! Available space = " + available);
            throw new BufferOverflowException();
        }

        // do stuff
        System.out.println("I'm doing stuff!\n");
        System.out.println("available buffer space = " + available);
    }
}
