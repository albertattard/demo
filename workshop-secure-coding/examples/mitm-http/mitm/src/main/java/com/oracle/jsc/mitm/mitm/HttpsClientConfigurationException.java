package com.oracle.jsc.mitm.mitm;

public class HttpsClientConfigurationException extends Exception {

    private static final long serialVersionUID = 1L;

    public HttpsClientConfigurationException() {
    }

    public HttpsClientConfigurationException(String message) {
        super(message);
    }

    public HttpsClientConfigurationException(Throwable cause) {
        super(cause);
    }

    public HttpsClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpsClientConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
