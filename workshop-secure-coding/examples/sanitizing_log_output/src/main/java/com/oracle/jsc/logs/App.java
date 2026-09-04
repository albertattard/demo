package com.oracle.jsc.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrate some sensitive data logging.
 */
public final class App 
{
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        log.info("Here's an innocuous log statement.");
        log.info("captured card number 4403-9999-8888-7777 expiration 0126");
        log.info("captured card number 4406999988887777 expiration 0126");
        log.info("All done!");
    }
}
