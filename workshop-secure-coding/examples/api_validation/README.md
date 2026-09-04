# API Validation

There's a tendency to treat APIs with more trust than user inputs and files. However, APIs run by other teams, even internal ones,
can be vectors for attack, both accidental and intentional. For example, a service I was once responsible for trusted another team's
API to show available device inventory in an AWS region. When that API began to report capacity that wasn't present, my service
sequestered all the available capacity in AWS for several regions, making it impossible for customers to launch new instances.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven
- an Oracle Cloud account configured with an API gateway as described in 
    "OCI [Required Keys and OCIDs](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/apisigningkey.htm#two)".

## App.java

Consider App.java. This is extracted from some demo code we provide to Java Management Service clients. It:
* uses stored configuration data (`ConfigFileReader.parseDefault()`) to create an Authentication Details Provider, then
* generates an OCI Identity service client, then
* generates a "list compartments" request, and 
* calls OCI to deliver the request and receive the response.

All that is well and good--but the stored configuration is an interesting problem. Is it inside or outside the
trust boundary?

And are the results of the OCI API inside or outside the trust boundary?

Run the example code:

```shell
% mvn package
% java -cp "target/lib/*:target/api_validation-1.0-SNAPSHOT.jar" com.oracle.jsc.validation.App
```

## App_better.java

In App_better, we have 2 blocks of improvement code. In the first part, we validate the configuration file to some extent,
and also the secret key:

```java
        verifyConfigFile(configFilePath);
        ConfigFile config = ConfigFileReader.parse(configFilePath.toString());
        verifyPrivateKey(config.get("key_file"));
```

The verify... methods check that the files are read-only, and read-only by the user. And also, they put some parameters
on the configuration file, to verify it exists, isn't overly long, and is readable.

The second improvement is represented by TODO statements, because you really need a concrete use case to write these 
verifications. Is the number of compartments reasonable? Did it largely match yesterday's list?

The third improvement is to transfer the data we care about to an API object that is not the native APIs. This allows
us to isolate the code we don't own to the one method that actually makes the call.

Run the example code:

```shell
% mvn package
% java -cp "target/lib/*:target/api_validation-1.0-SNAPSHOT.jar" com.oracle.jsc.validation.App_better
```