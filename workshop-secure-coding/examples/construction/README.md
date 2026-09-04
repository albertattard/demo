# Construction

We need to pay special attention to construction when creating a class which handles sensitive data. In this case, 
we are constructing a DAO which produces User objects. This class is sensitive because Users have an "isAdmin" field
which must be populated correctly to deliver admin privileges. Users are immutable, so once a User is created, we have
no concern that the admin property will be changed at runtime.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven

## App01.java

App01 instantiates a UserDao by creating a new InsecureDao. The constructor can (and does) throw an exception, which might
happen if, for example, a database connection was made in the constructor. There are several drawbacks here:

* App01 has to know the class of the DAO, so it is tightly coupled to that class.
* The DAO constructor can throw an exception, which leaves App01 without any alternatives should that happen.
* Even if construction succeeds, App01 needs to handle an I/O Exception during user retrieval, which leaves few alternatives.
* Exceptions during construction can be leveraged by malicious code. See, for example,
[https://wiki.sei.cmu.edu/confluence/display/java/OBJ11-J.+Be+wary+of+letting+constructors+throw+exceptions](https://wiki.sei.cmu.edu/confluence/display/java/OBJ11-J.+Be+wary+of+letting+constructors+throw+exceptions)

Build and run the app:

```shell
% mvn package                                                                    
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.oracle.jsc:construction >---------------------
[INFO] Building construction 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] --- jar:3.4.1:jar (default-jar) @ construction ---
[INFO] Building jar: /Users/jeandre/Documents/dev/workshops/workshop-secure-coding/examples/construction/target/construction-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.131 s
[INFO] Finished at: 2025-07-03T13:56:16-07:00
[INFO] ------------------------------------------------------------------------

 % java -cp target/construction-1.0-SNAPSHOT.jar com.oracle.jsc.construction.App01
Exception getting the DAO: Unable to connect to the database or find the backup config.
```

## App02.java

App02 instantiates the UserDao by creating a new SecureDao. The new DAO implementation won't throw an exception during
construction. There are still some drawbacks, but it does have some advantages too. Retrieving a user could throw an
exception in InsecureDao, if you managed to construct one successfully. Acknowledging that fact, SecureDao defers initialization
until first use. You can still throw an exception if you can't gain access to the external User authentication and authorization
resources, but you have to handle the retrieval exception anyway--this allows you to use the same code to handle both exceptions.
If you're concerned about finding out if you have a valid access, you can call "isValid" early on to determine that you have
a valid working DAO.

We still have some drawbacks:
* App02 hasd to know the class of the DAO, so it is still tightly coupled to that class.
* Even though construction succeeds, App01 needs to handle an I/O Exception during user retrieval, which leaves few alternatives.

```shell
 % java -cp target/construction-1.0-SNAPSHOT.jar com.oracle.jsc.construction.App02
Exception during getting the user: Unable to connect to the database or find the backup config.
```

### App03.java

App03 uses a factory, which is recommended for sensitive classes. It validates the DAO, discovers it's invalid, and returns
a stub implementation which allows the application to continue running with reduced capability. That is, it degrades gracefully.
It does log the failure, so app management can determine that there is a problem. However, no exceptions are thrown:

```shell
% java -cp target/construction-1.0-SNAPSHOT.jar com.oracle.jsc.construction.App03
Unable to connect to the user database. Returning a DAO that only produces guest users.
Bob is not an admin.
```


