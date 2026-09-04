package com.oracle.jsc.reflect;

public class FixedData {
    private final String secret;

    public FixedData(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}
