# Data Leakage in simple HTTP applications

HTTP "GET" is probably the most common operation in HTTP--but if your design is not thoughtful, it can 
result in sensitive data leaks.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [`curl`](https://formulae.brew.sh/formula/curl) to retrieve service output.
- [`jq`](https://formulae.brew.sh/formula/jq) to format the JSON output

## Build and run the catalog web service

This service implements a simple catalog service  and is used in a number of demonstrations in this workshop. It is a simple
Spring Boot application with an H2 database. Inspect "CatalogueItemEntity" to see what a catalog item looks like. 

```shell
$ ./mvnw clean spring-boot:run
```

#Retrieve a catalog item or two

```shell
$ curl 'http://localhost:8080/catalogue/item/1' | jq
```

Note that with an ID of 1, there's a hint that IDs are just simple integers, which gives an attacker a "hook" to retrieve the
entire catalogue by just trying all the integers:

```shell
$ curl 'http://localhost:8080/catalogue/item/2' | jq
$ curl 'http://localhost:8080/catalogue/item/3' | jq
$ curl 'http://localhost:8080/catalogue/item/4' | jq
$ curl 'http://localhost:8080/catalogue/item/5' | jq
```
If you bookmark this URL, you'll note that the item ID is recorded in the bookmark, and so is now available to anyone who
might have access to your bookmarks file or a backup of your machine.

A better strategy would be to use GUIDs instead of integers for IDs. It's no slower, but the resulting retrieval doesn't give an attacker any information that would help them retrieve data they're not entitiled to see:

```shell
$ curl 'http://localhost:8080/catalogue/item/3787efde-9365-4b4b-bd67-ce567393afb1' | jq
$ curl 'http://localhost:8080/catalogue/item/722c76af-82f8-4124-ab28-b2cb392e04ea' | jq
```

Other strategies for protecting more complex searches include putting the search data in a cookie or other non-bookmarkable
entity, such as the GET request body.

## shut down the app server
```shell
$ ps -aef | grep -v "ps -aef" | grep -v "grep" | grep demo.Main | awk '{print $2}'| xargs kill -HUP 
```