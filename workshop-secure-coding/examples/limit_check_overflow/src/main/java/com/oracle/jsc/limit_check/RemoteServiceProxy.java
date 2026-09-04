package com.oracle.jsc.limit_check;

public final class RemoteServiceProxy {
    public static int headerLength() {
        return 20_000;
    }

    public static int bodyLength() {
        return Integer.MAX_VALUE - 5_000;
    }
}
