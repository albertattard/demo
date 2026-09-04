# Reflection Mutability

While the JVM can protect some data from mutation, designers should be thoughtful about creating immutable classes. Records
provide the kind of immutability we are usually looking for, but if you need a class or are working with JDK 11 or 8, you
might want to mark fields immutable to block the easiest ways of making a field mutable.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## Demo

Look at "Data.java". In this class, we declare a private field, and we don't provide a setter. In many contexts, this would be
considered immutable. Malicious code, though, can easily mutate this class, as we'll see.

Look at "App.java". As you can see, by finding the field of interest and changing its visibility, we can set the field's contents
directly.

Now look at "FixedData.java". In this class, we declare the private field `final`. This prevents the field's content from being
mutated. If you try, you'll get an IllegalArgumentException, thrown by the JVM upon attempting to update a field marked final.

Run the example:

```shell
% mvn package                                                                      
% java -cp target/reflection_mutability-1.0-SNAPSHOT.jar com.oracle.jsc.reflect.App
```

Results should be, as expected:

```
Data's secret is "Hello, world!"
Now data's secret is "Goodbye, cruel world!"
----
FixedData's secret is "Hello, world!"
Caught class java.lang.IllegalArgumentException
FixedData's secret is "Hello, world!"
```
