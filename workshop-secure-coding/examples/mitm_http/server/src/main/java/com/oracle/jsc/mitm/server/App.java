package com.oracle.jsc.mitm.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "com.oracle.jsc.mitm.server")
public class App 
{
    public static void main(String[] args) {
      SpringApplication.run(App.class, args);
    }
}
