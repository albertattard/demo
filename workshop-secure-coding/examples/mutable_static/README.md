# Mutable Statics

Mutable statics are global variables--and while there may be cases where they are useful, they're far more likely to
be the source of hard-to-diagnose errors.

This demo is a transparent example of both lack of thread safety and unpredictable side effects of using a mutable
global where a local variable would be a better choice.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## Demo

`App.java` launches itself in 10 separate threads. Each thread waits a bit, writes a string identifying itself,
and then waits some more, until the main program declares itself done using the global static "done" variable.
The output string is a global static, as is the "done" variable; each thread updates the output string to identify
itself. The update process is spread out over time intentionally--imagine, for example, that a coordinated update
of a set of separate databases is the action here instead of just assigning a string.

```shell
% mvn clean package
% java -cp target/mutable_static-1.0-SNAPSHOT.jar com.oracle.jsc.mutable.App
```

Inspecting the output, we see that, as expected, multiple threads append to the output variable, resulting in random
output.

Now look at `App_fixed.java`. The variables are now all instance variables, and we keep a reference to each generated
App_fixed, so we can tell them they should stop running individually at the end of `main(...)`. No changes were made
to the `run()` method.  Now run `App_fixed`:

```shell
% java -cp target/mutable_static-1.0-SNAPSHOT.jar com.oracle.jsc.mutable.App_fixed
```

As you can see, the behavior is much more like we'd expect it to be.
