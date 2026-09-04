# Module Isolation

The design philosophy of the Java platform is "secure by design". One important part of that is **reducing the attack surface**: we want to make it harder for code to call APIs that it was never meant to use.

The **Java module system**, added in Java 9, lets you:
- **Say which packages are part of your public API** (exported).
- **Hide internal implementation packages** so other code cannot use them, even if those classes are on the module path.

In this example we will:

1. Run code using the **traditional classpath** (no modules) and see that it can call internal classes directly.
2. Turn on the **module system** and see that the same access is now **blocked by the compiler**.
3. Fix the code to use only the intended public API.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven
- ~/.m2/toolchains.xml with the following entry (at least):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <!-- JDK toolchains -->
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
      <vendor>Oracle Corporation</vendor>
    </provides>
    <configuration>
      <jdkHome>/path/to/your/java/installation/example/21.0.3-oracle</jdkHome>
    </configuration>
  </toolchain>
</toolchains>

```

This demonstration's POM mentions "jdk" "21", and "Oracle Corporation" explicitly. The toolchain declaration above associates
a specific JDK with that declaration.

## Project Structure

Starting with libA, the intended public entry point. The LibA class `hello.LibA` calls an internal DAO, `hello.dao.A_DAO`, to
retrieve a `hello.dao.Hello` object. That object contains a "hello world" string from some data store
(in this case, a hard-coded one). The DAO isn't intended to be public, but if it's on the classpath, anything can access it;
it has to be public, or `hello.LibA` wouldn't be able to see it.

Now look at "mods"--LibA's caller. This is the only class is `modules.App`, and it outputs two strings--one retrieved
by reaching directly into the DAO, and the other from `hello.LibA`.

Now, build the entire project. As you can see, it produces two .jar files--one for the library libA, and the other for the 
main program `modules.App`:

## Step 1: Build and Run Without Modules (Classpath):

```shell
% mvn clean package
% find . -name "*.jar"
```

You should see that both `libA` and `mods` build successfully, and you should see two JAR files.

We construct a java command line putting those files on the classpath, and execute App:

```shell
% java -classpath ./libA/target/libA-1.1-SNAPSHOT.jar:./mods/target/mods-1.0-SNAPSHOT.jar com.oracle.jsc.modules.App
```

As expected, we see 2 lines of output:

```
Greetings from the DAO!
Greetings from the DAO!
```
Even though A_DAO is meant to be internal, the classpath lets any code that can see the .jar call it directly.

## Step 2: Turn on the Module System

This project contains 2 module files, but currently named with the extension ".hidden" instead of ".java". Let's rename them, 
so they become real module descriptors, then examine them one at a time:

```shell
% mvn clean
% mv mods/src/main/java/module-info.java.hidden mods/src/main/java/module-info.java
% mv libA/src/main/java/module-info.java.hidden libA/src/main/java/module-info.java
```

First, `module-info.java` from `libA`:

```java
module hello {
    exports hello;
}
```

This declares declares a module named "hello", which exports all the classes found in the package named "hello". In our case,
this is just `LibA`. All other packages (e.g. `hello.dao`) are not "exported", meaning "not visible".

Second, look at `module-info.java` from `mods`:

```java
module modules {
    requires hello;
}
```

This declares a module named "modules". That module doesn't export anything at all--which means nothing can use the classes
found in this module--and it requires a module named "hello" to execute.

Once we have renamed these files so they act as module-info.java files, Maven can't even build the application:

```shell
% mvn clean package
```

"Mods" fails to comple; the error is:

```
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] module_isolation 1.0-SNAPSHOT ...................... SUCCESS [  0.111 s]
[INFO] libA 1.1-SNAPSHOT .................................. SUCCESS [  1.149 s]
[INFO] mods 1.0-SNAPSHOT .................................. FAILURE [  0.407 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.723 s
[INFO] Finished at: 2025-07-01T15:05:08-07:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile (default-compile) on project mods: Compilation failure
[ERROR] /Users/jeandre/Documents/dev/workshops/workshop-secure-coding/examples/module_isolation/mods/src/main/java/com/oracle/jsc/modules/App.java:[4,12] error: package hello.dao is not visible
[ERROR]   (package hello.dao is declared in module hello, which does not export it)
[ERROR] 
```

Exactly the behavior we hoped for: the module system is blocking access to the internal hello.dao package.

## Step 3: Fix App.java to use only the public API

You can update `App.java` to remove the offending lines and import, and it will execute correctly, accessing only the classes it's 
allowed to access by the module declaration from libA.

```java
import hello.LibA;

public class App {
    public static void main(String[] args) {
        System.out.println(new LibA().hello());
    }
}
```
Rebuild:
```shell
$ mvn clean package
```

## Step 4: Run using modules

When using modules, we don't declare a classpath--we use a ***module path*** instead. You also specify the module containing
the main class.

Here's an example using the long form of the module-path and main module definition:

```shell
% java --module-path ./libA/target/libA-1.1-SNAPSHOT.jar:./mods/target/mods-1.0-SNAPSHOT.jar --module modules/com.oracle.jsc.modules.App
```

or, using hte short form -p and -m abbreviations...

```shell
% java -p ./libA/target/libA-1.1-SNAPSHOT.jar:./mods/target/mods-1.0-SNAPSHOT.jar -m modules/com.oracle.jsc.modules.App
```

## Summary

- On the __classpath__, any public class in any package inside a JAR can be used by any other code.

- With the __module system__:

  - Each JAR belongs to a named __module__.
  - Only __exported__ packages are visible to other modules.
  - Internal packages (like `hello.dao`) are hidden, improving encapsulation and reducing attack surface.

For day-to-day coding, using modules helps you:

- Clearly separate public API from internal implementation.
- Prevent accidental or malicious use of internal classes.
- Catch unwanted dependencies at __compile time__, not in production.
