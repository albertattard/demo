package com.oracle.jsc.filter;

import java.io.Serializable;

public class Cookie implements Serializable {
    private static final long serialVersionUID = 1L;

    String token;

    public Cookie(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
