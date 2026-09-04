package com.oracle.jsc.construction.dao;

import java.io.IOException;

public final class UserStateException extends IOException {

    private static final long serialVersionUID = 1L;

    public UserStateException() {
        super();
    }

    public UserStateException(String message) {
        super(message);
    }

    public UserStateException(Throwable cause) {
        super(cause);
    }

    public UserStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
