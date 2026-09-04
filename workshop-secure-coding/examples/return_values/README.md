# Return values

Mutation errors can be introduced by returning mutable state from getters and other accessors.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## run the demo

This app contains some objects related to a Person--the name and birth date are immutable, phone number and 
contact are mutable by design. The phone number object contains a lot of input data checks, and while that
makes it complex to create, it does help ensure that the data are valid.

Examine App.java.

Here we construct "Person" objects, print out their state, then request the birthday of one of the constructed objects.
We update that supplied birthday object, without returning it to the Person (remember, the birth date has no setter and
is assumed to be immutable). Then we print the state of the person, and note the stored birthdate has changed.

Build and execute the program:

```shell
% mvn clean package
% java -cp target/return_values-1.0-SNAPSHOT.jar com.oracle.jsc.returns.App
```

Note the output contains the updated birth date:

```
Alice: Person{Alice, born Tue Jul 11 00:00:00 PDT 1967, phone: +1-555-7776021, contact: Bob}
  Bob: Person{Bob, born Sat Nov 17 00:00:00 PST 1979, phone: +1-555-7776021, contact: Alice}
-----
  Bob: Person{Bob, born Mon Dec 24 00:00:00 PST 1956, phone: +1-555-7776021, contact: Alice}

```

Now look at the Person object. The getter for birthdate returns the stored `java.util.Date`, which is mutable. There are two
options for fixing this:

1. return a copy of the date, e.g. `return new Date(birthDate.getTime());`.
2. store an immutable object using the newer `java.time` objects; in this case a `LocalDate` would be the obvious choice. You
can then return either a Date or a LocalDate, e.g.: `return Date.from(birthDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()); `