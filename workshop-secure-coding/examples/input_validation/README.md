# Input Validation

If input files are outside your trust boundary, you will want to validate them extensively on input. Here's an example of both
"typical" and "somewhat better" validation of untrusted input.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## App.java

App.java is a pretty normal file reader. It loads a small JSON file into memory, instantiating several Person objects and
making links between them. Once it has done so, it prints out the resulting data structure for investigation. Scan the code
so you can see how it works, then build and run it.

```shell
% mvn package
% java -cp target/input_validation-1.0-SNAPSHOT.jar:target/lib/json-simple-1.1.1.jar com.oracle.jsc.valid.App
```

If everything goes right, you'll see the following output:

```
Person[name=Ellen, city=Paris, role=null, staff=(null)]
Person[name=Bob, city=Cologne, role=Manager, staff=[Alice, Cindy]]
Person[name=Alice, city=Berlin, role=Teacher, staff=[]]
Person[name=David, city=Oslo, role=Sales, staff=[(null)]]
Person[name=Cindy, city=Dallas, role=Assistant, staff=[]]
```

Now, the good news is, we didn't get an exception, and it looks like the file was decoded properly. Let's look at the JSON
file we parsed:

```json
[  
    {  
        "name":"Alice",
        "city":"Berlin",
        "role":"Teacher"
        "staff":[]
    },
    {
        "name":"Bob",
        "city":"Cologne",
        "role":"Manager"
        "staff":["Alice", "Cindy"]
    },
    {
        "name":"Cindy",
        "city":"Dallas",
        "role":"Assistant"
        "staff":[]
    },
    {
        "name":"David",
        "city":"Oslo",
        "role":"Sales"
        "staff":["Helen"]
    },
    {
        "name":"Ellen",
        "city":"Paris",
    }
]
```

There are some things to note:
* the top level element is an array
* record 4 names a person who is not in the file.
* record 5 is missing most fields

And looking at App.java, we note:
* we are loading a file from outside the trust boundary, be we didn't do any checks on the file before we started the load.
* what if the file didn't match the expected format? We'd be throwing all kinds of exceptions.
* what if the file was bigger than memory would hold? If we crash the app, we have effectively attacked ourselves with DoS.

Looking at the output, we see many nulls in the resulting data structure. What caused these? Is the data missing, or invalid?
What does null mean in this context?

## BetterApp.java

While `BetterApp.java` isn't perfect, it contains a lot more defense-in-depth than App.java. It doesn't assume the file
can be parsed or even exists, for example. Here are some of the checks:

* does the file exist?
* is it readable? is a directory?
* is it too big to read comfortably?
* can it be parsed as JSON?
* is the outer element an array?
* are the inner objects JSONObjects?
* does every entry have a "name"--the identifier for a Person object?
* is the staff array an array?
* do the mentioned staff exist?

In every case, if a failure occurs, enough information is posted in an error message to identify the problem. In addition,
the resulting parser checks for nulls and replaces them with empty strings, which makes the resulting data structure easily
handled by downstream code.

Run App_better:

```shell
% java -cp target/input_validation-1.0-SNAPSHOT.jar:target/lib/json-simple-1.1.1.jar com.oracle.jsc.valid.BetterApp
```

If everything goes right, you'll see the following output:

```

Staffer "Helen" for staff "David" does not exist.

Person[name=Ellen, city=Paris, role=, staff=[]]
Person[name=Bob, city=Cologne, role=Manager, staff=[Alice, Cindy]]
Person[name=Alice, city=Berlin, role=Teacher, staff=[]]
Person[name=David, city=Oslo, role=Sales, staff=[]]
Person[name=Cindy, city=Dallas, role=Assistant, staff=[]]
```

## BetterAppRefactored.java

`BetterApp.java` is a bit long, and there's a lot going on. This straightforward refactor just extracts and properly names 
each bit of validation. It makes the code easier to understand, and focuses each validation on one small aspect, making 
debugging and enhancement really straightforward.

None of the changes between `BetterApp.java` and `BetterAppRefactored.java` were done by hand--they were all done using
my IDE's "Extract Method" refactoring tool. Thus, this is quick, easy, and very unlikely to introduce new bugs.