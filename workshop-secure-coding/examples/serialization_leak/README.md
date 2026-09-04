# Serialization leaks

Storing serialized Java objects exposes their internal state in a persistent form. Serialization leaks are more common than
you might expect. One obvious source of Java serialized files is Java objects stored in cookies and delivered to a user's 
browser.

Here we will see how easy it is to leak sensitive data.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## run the demo

Examine App.java.  This instantiates a "Cookie" object containing a GUID, which might then be used to connect to a session
in a web browser. App.java prints the cookie's stored token value, then serializes the cookie and stores it in a file, much
as you would if you serialized the cookie to a web server's session context.

Build and execute the program:

```shell
% mvn package
% java -cp target/serialization_leak-1.0-SNAPSHOT.jar com.oracle.jsc.cookie.App
```

Note the output contains the cookie value:

```
token: 38313dbf-fe46-47dc-896d-04f656587d52
```

The serialized object is stored in "cookie.bin" in our local default directory:

```shell
% cat cookie.bin
```

You'll note the cookie's value is there for all to see in unprotected form, allowing an attacker to take over the session
in progress.

Also note that most browsers encrypt cookie data for you. Do you want to depend on that?
