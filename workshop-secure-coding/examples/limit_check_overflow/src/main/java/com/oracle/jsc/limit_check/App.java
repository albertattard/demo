package com.oracle.jsc.limit_check;

import java.nio.BufferOverflowException;

public final class App {
    private static final long MAX_BUFFER_CAPACITY = 10_000;

    public static void main(String[] args) {
        // BUG: both operands are int, so this addition overflows before the result
        // is widened to long.
        long inbound = RemoteServiceProxy.headerLength() + RemoteServiceProxy.bodyLength();

        long available = MAX_BUFFER_CAPACITY - inbound;

        if (available < 0) {
            System.err.println("I'm out of buffer! Available space = " + available);
            throw new BufferOverflowException();
        }

        // do stuff
        System.out.println("I'm doing stuff!\n");
        System.out.println("available buffer space = " + available);
    }
}
