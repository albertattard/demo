package com.oracle.jsc.mitm.mitm;

public class OutboundHttpsException extends Exception {

    private static final long serialVersionUID = 1L;

    public OutboundHttpsException() {
    }

    public OutboundHttpsException(String message) {
        super(message);
    }

    public OutboundHttpsException(Throwable cause) {
        super(cause);
    }

    public OutboundHttpsException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutboundHttpsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
