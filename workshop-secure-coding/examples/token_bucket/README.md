# Token Bucket Rate Limiting

Token Buckets can improve your service's resiliance by limiting the rate at which any one client can request results.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [`curl`](https://formulae.brew.sh/formula/curl) to retrieve service output.
- [`jq`](https://formulae.brew.sh/formula/jq) to format the JSON output.
- [`JMeter`](https://jmeter.apache.org) to load test the API.

## Build and run the catalog web service

This service implements a simple catalog service (see url_data_leak in this set of demos), and is a simple 
Spring Boot application with an H2 database. Inspect "CatalogueItemEntity" to see what a catalog item looks like. 

```shell
$ ./mvnw clean spring-boot:run
```

#Retrieve a catalog item or two

```shell
$ curl 'http://localhost:8080/catalogue/item/1' | jq
```

#Token Bucket

Spring Boot includes features based on what jars are available at runtime. In this project, we added 

```
        <dependency>
            <groupId>com.bucket4j</groupId>
            <artifactId>bucket4j-core</artifactId>
            <version>8.0.1</version>
        </dependency>
```

Bucket4J is a token bucket implementation that Spring Boot recognizes, so requests can leverage it clealy and automatically.

Look at our Controller implementation--`CatalogueItemController`--and you'll see a couple important features.

At the top of the class is the Bandwidth implementation, defined in the constructor as:

```java
        // Configure the rate limit
        limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
```

This defines a bucket size of 20, and 20 new tokens per minute.

We also declare:

```java
    private final Map<String, Bucket> ipToBucket = new HashMap<>();
```

We're going to use the source IP address of each client as the identifier for that client. You can use anything you want, or define a single bucket to handle the whole API, but this is the choice I made for this implementation.

At the top of each mapping, we have a small bit of code which retrieves a bucket from this map, or adds one and returns it, based on the source address of the request. We then query the bucket to see if there's an available token:

```java
        // Here's where the basic rate limit happens
        Bucket bucket = getBucket(request.getRemoteAddr());
        if (! bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
```

If no token is available, our choice in this case is to immediatly return a 429 response; of course, you can choose any behavior you want here.


#Load test

As you can see from src/main/resources/db/migration/V1__create_database.sql, the database contains 1 table with 5 data
rows. We will repeatedly request those rows at a high rate so we can see the token bucket in action.

Start JMeter and load the "load.jmx" test. As you can see, we have a random number generated at the start of each loop
through the test sequence, and we use that as the item ID. Then we make an HTTP GET request for that item's detail, after pausing a small amount of time.

Run test by clicking on the "Run" button--the thread group is configured for a 30 second test.

You'll note that early on, all the requests are successful. After a bit, though, a test succeeds only roughly every 3 seconds--which is the rate at which tokens are added to the bucket (60 seconds / 20 tokens per second).

## shut down the app server
```shell
$ ps -aef | grep -v "ps -aef" | grep -v "grep" | grep demo.Main | awk '{print $2}'| xargs kill -HUP 
```