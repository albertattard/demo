package com.oracle.jsc.filter;

import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * This could be anything--see <a href=
 * "https://github.com/frohoff/ysoserial">https://github.com/frohoff/ysoserial</a>
 * for examples of code that does malicious things upon deserialization.
 */
public class NotCookie implements Serializable {
    private static final long serialVersionUID = 1L;
    private int state = 0;

    public NotCookie() {
        System.out.println("Hello, world! state = " + state);
    }

    private void readObject(ObjectInputStream s) {
        System.out.println("Hello, readObject!");
    }
}
