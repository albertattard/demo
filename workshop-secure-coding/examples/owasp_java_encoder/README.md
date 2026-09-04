# OWASP Java Encoder

This is based on the SQL Injection demonstration code, and shows how to mitigate the problem with the OWASP Java Encoder.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- a modern web browser

## Build and run the application

This spring boot application takes an input string and encodes it for various uses.

```shell
$ ./mvnw clean spring-boot:run
```

## Play with the encoder a little, so you can see how it works.

Navigate to [http://localhost:8080](http://localhost:8080).  Enter a term in the blank, then click "Encode". Best results are achieved by including
some special characters, especially slash, backslash, asterisk, ampersand, and angle brackets.

Try the SQL injection code from a previous demo:

```
' union all select 1, id, name, pass from users where name like '
```

## Review index.html, and DemoString.java

At the top of index.html, you see 

```html
<form action="#" th:action-"@{/}" th:object="${DemoString}" method="POST">
    <input type="text" th:field="*{string}" placeholder="enter term to encode..." class="form-control mb-4">
```

This code stores your term in a "DemoString" object in the "string" field. Details are found in UIController.java,
if you're interested.

The table below that retrieves that string, encoded with various Encoder methods,, e.g.

```html
    <tr> <td>forJava</td> <td th:text="${DemoString.forJava}"></td> </tr>
```

You can see, for example, that the "forJava" method in DemoString calls a static Encode method found in the Encode class:

```java
    public String forJava() {
        return Encode.forJava(string);
    }
```

## shut down the server

```shell
$ ps -aef | grep -v "ps -aef" | grep -v "grep" | grep demo.Main | awk '{print $2}'| xargs kill -HUP 
```