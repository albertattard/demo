# Secure JDBC

JDBC is often used in its default configuration. This can be secure, but that depends on the driver and database defaults.

In this example, we will inspect and use some of the features that provide enhanced security in a MySQL database connection.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven
- Docker
- If you are using Docker: a valid login to the Oracle software container registry at
    [https://container-registry.oracle.com/](https://container-registry.oracle.com/).
    
## Setup

### Create a default installation of MySQL

MySQL setup is straightforward and is well documented in the MySQL reference manual at
[https://dev.mysql.com/doc/refman/9.5/en/](https://dev.mysql.com/doc/refman/9.5/en/).

This demonstration executes MySQL from within a docker container, so that it can be easily initialized/reinitialized.
Instructions for installation are at
[https://dev.mysql.com/doc/refman/9.5/en/linux-installation-docker.html](https://dev.mysql.com/doc/refman/9.5/en/linux-installation-docker.html)
on MacOS or Linux. These instructions may work on Windows if you have done Linux-based installations. 

You will need to perform the Authentication steps using a web browser, as described in the instructions. Once you have
a local image of mysql (`docker pull  container-registry.oracle.com/mysql/enterprise-server:latest`), then you can start following the
directions below.

Our first steps are to

* launch the downloaded container running MySQL, while
    * mapping its default MySQL port to a localhost port,
    * mapping its default configuration file to a file we control, my.cnf, located in our resources directory
* acquire the auto-generated root password, 
* determine what the gateway IP address is, allowing us to configure MySQL to allow connections from it, and thus our Java code, and
* gain access to the MySQL SQL client.

```shell
% docker run \
    --name mysql \
     -p 3306:3306 \
     --mount type=bind,src=./src/main/resources/my.cnf,dst=/etc/my.cnf  \
     --restart on-failure \
     -d container-registry.oracle.com/mysql/enterprise-server:latest
% docker logs mysql 2>&1 | grep GENERATED
% docker inspect mysql | grep Gateway
% docker exec -it mysql mysql -uroot -p  --ssl-mode=REQUIRED
```

At this point, you're logged in (you copy/pasted the root password, right?).

Next we need to:

* change the root password
* inspect the list of users
* show existing databases, then create a database and verify it was created,
* create a user to access that database using a connection from the docker container's inbound gateway

```shell
mysql> alter user 'root'@'localhost' identified by 'MyNewRootPassword';
mysql> select host, user, account_locked from mysql.user;
mysql> show databases;
mysql> create user jsc@172.17.0.1 identified by 'JSCuserPassword12345';
mysql> create database jsc;
mysql> use jsc;
mysql> grant all privileges on *.* to jsc@172.17.0.1;
mysql> show databases;
mysql> select host, user, account_locked from mysql.user;
mysql> exit;
```

You can inspect the container contents with:

```shell
# docker exec -it mysql bash
```
We need to retrieve the server's auto-generated CA certificate and package it for later use:

```shell
% docker cp mysql /var/lib/mysql/ca.pem .
% keytool -importcert -alias mysqlcacert -file ca.pem -keystore truststore.pkcs12 -storepass MySQLtrustPass
```

If you want to view the truststore's content, you can:

```shell
% keytool -list  -keystore ./truststore.jks -v
```

This is all basic setup. The my.cnf file we are using insists on an SSL connection, and auto-generates a self-signed
certificate in order to anchor the TLS connection. Our client will insist that this certificate be used, and we'll look at
using that self-signed CA cert later in the process as well.

Now we're ready to demonstrate some secure transport features using Java.

## Presentation

### Look at the default configuration

Our default JDBC configuration is controlled by the URL:

```
    private static String URL = "jdbc:mysql://150.136.77.155:3306/jsc?sslMode=DEFAULT";
```

This makes a direct socket connection to MySQL without any encryption.

Run the code. This makes 2 connections to the database. First, FlyWay connects using the URL, username, and password, and
sets up the database. Then our code instantiates a JDBC connector, connects to the database, and retrieves the contents
of the one table present in the DB (see `V1__create_database.sql`, which Flyway uses to set up the database).

The problem here is that anyone with network access can see your connection, login, password, and all your database traffic.

### Require SSL

Change the sslMode to "REQUIRED". This ensures that if a TLS connection can not be established, the connection will fail.

MySQL will default to SSL, but it's nice to specify it.

Run the code. There's no obvious change in behavior, but now a network observer can no longer read your traffic.

### Require a valid certificate chain

As a basic next step, we can instruct the client to verify the certificate chain anchoring the TLS connection. This step
ensures that the certificate our client receives is valid and can be trusted.

Change the SSL mode from "REQUIRED" to "VERIFY_CA", and re-run the code. This will fail, because the certificate is
self-signed; Java doesn't trust the Java doesn’t trust this certificate because it didn’t come from a known
certificate authority—so it blocks the connection.

We can fix this by adding the certificate's signer, the "certificate authority", or CA, to Java's set of trusted CAs,
or by adding a trust store specification to the JDBC URL as shown in the example code. A truststore tells your app which
certificate authorities it should trust. It does NOT store your private keys.

Add the two properties "trustCertificateKeyStoreUrl", which identifies a trust store carrying
the CA certificate used by the server and "trustCertificateKeyStorePassword", which provides
the password to that trust store. Rerun the code, and you will see that the connection is now successful.

The resulting flow is as follows:

    Client (Java) ---> [TLS Encryption] ---> MySQL
                    (truststore validates server)

### Verify the server identity

You want to ensure that the server you are connecting to is the one you expected. You do this by changing the SSL mode
from "VERIFY_CA" TO "VERIFY_IDENTITY". Run the code, and note the cause of failure: the server name does not match the 
certificate passed from the server, who's CN is MySQL_Server_9.5.0_Auto_Generated_Server_Certificate'. This does not
match the server name of '150.136.77.155'. You can remedy this by generating a more complete server cert. See the 
client_certs example. 

### Some other notes on MySQL security

* Encryption at rest is free, but key management is not free (you can use file-based keys, but that's not terribly secure).
* The JDBC driver URL format can include multiple servers in a comma-separated list, which provides automatic failover
    (and thus, some protection against Denial o Service, or "DoS").  E.g.: `jdbc:mysql://150.136.77.155:3306,150.136.77.156:3306/jsc?sslMode=REQUIRED`.
* The JDBC driver for MySQL includes OpenTelemetry support.

### shut down and remove the container
```shell
% docker stop mysql
% docker rm mysql
```
