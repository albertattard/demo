# Deserialization filters

For lots of reasons, we can't always avoid Java deserialization and serialization. Since Java 9, however, as a result of 
JEPs 290 and 415 in Java 9 and 17, respectively, we can help ensure that what we expect to be deserialized is what we are
actually deserializing.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## App.java

We met Cookie serialization / deserialization in the serialization leak demo. This demo uses the same basic code: we instantiate
a Cookie, print out its value, the serialize it to a file. App.java then goes the next step, and deserializes the cookie to 
demonstrate that is working as expect.

Then App.java serializes a NotCookie and saves it, much as an attacker might generate a payload from a custom object which would
then be used in place of the intended serialized object.

Look at NotCookie. Note that it contains a private method: `private void readObject(ObjectInputStream s)`. The Java deserialization spec
allows for this method to be called by the deserialization process to allow an object to manage its own deserialization process.
This is common for objects which need internal methods to be called to populate transient data, such as object counts and validity
state checks. In this case, we just emit a message to indicate that the method was called.

Now look again at `App.java`. As you can see, the deserialization process tries to cast a known NotCookie to a cookie. This will fail,
but the readObject... method is called before it fails. This is the basis for many gadget chains--deserialization is complete,
but if you supply the right kind of data to the right gadget chain, you can force a sequence of operations you didn't intend
(such as the DNS call we looked at in the presentation; see, for example,
[URLDNS.java](https://github.com/frohoff/ysoserial/blob/master/src/main/java/ysoserial/payloads/URLDNS.java). When we run
this code, the NotCookie deserialization process will print `Hello, readObject!`, even though the class cast fails.

```shell
% mvn package
% java -cp target/deser_filter-1.0-SNAPSHOT.jar com.oracle.jsc.filter.App
```

The resulting output should look like this:

```
token: 95988b16-1ee8-4b38-ae4a-9ba06a216ede
Hello, world!
Attempting to read in cookie
Read in cookie 95988b16-1ee8-4b38-ae4a-9ba06a216ede
Attempting to read in notCookie
Hello, readObject!
Exception in thread "main" java.lang.ClassCastException: class com.oracle.jsc.filter.NotCookie cannot be cast to class com.oracle.jsc.filter.Cookie (com.oracle.jsc.filter.NotCookie and com.oracle.jsc.filter.Cookie are in unnamed module of loader 'app')
    at com.oracle.jsc.filter.App.main(App.java:35)
```

How can we prevent this deserialization?

The Object read process opens the binary object stream, and then reads a binary header which identifies the class and provies
other information of interest. A serialization filter can then be called to decide if serialization should proceed.

## App_filtered.java

`App_filtered` implements a very simple deserialization filter. It only allows objects of class com.oracle.jsc.filter.Cookie to be
deserialized; all other objects are rejected. We set the filter as the first action after instantiation the ObjectInputStream
(this action can only be done once on each ObjectInputStream). Then, when we try to deserialize a thing that is not a Cookie,
the ObjectInputStream throws and exception and fails to deserialize:

```shell
% java -cp target/deser_filter-1.0-SNAPSHOT.jar com.oracle.jsc.filter.App_filtered
```

And the result:

```
token: 6f1a9585-4dd7-49d3-a860-7bd937614464
Hello, world!
Attempting to read in cookie
Read in cookie 6f1a9585-4dd7-49d3-a860-7bd937614464
Attempting to read in notCookie
Exception in thread "main" java.io.InvalidClassException: filter status: REJECTED
    at java.base/java.io.ObjectInputStream.filterCheck(ObjectInputStream.java:1439)
    at java.base/java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:2071)
    at java.base/java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1927)
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2252)
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1762)
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:540)
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:498)
    at com.oracle.jsc.filter.App_filtered.main(App_filtered.java:36)
```