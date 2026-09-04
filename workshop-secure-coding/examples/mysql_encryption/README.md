# MySQL Encryption

At rest, most databases store data in unencrypted form in a binary file. This provides the fastest possible access, but exposes sensitive data to intruders.

In this demo, we will show 2 different approaches to data encryption-one which encrypts the entire data store, and the other which encrypts data before storing and retrieving it.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 25](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html)
- Maven
- a remote Linux instance, e.g. on OCI

## Create an installation of MySQL

MySQL setup is straightforward and is well documented in the MySQL reference manual at
[https://dev.mysql.com/doc/refman/9.5/en/](https://dev.mysql.com/doc/refman/9.5/en/). To install MySQL 8 on an OCI OL9 machine:

1. navigate to https://dev.mysql.com/downloads/repo/yum/ and download the appropriate version of MySQL. This will require a login.
2. scp the file to your Linux installation, e.g.: `scp -i ~/.ssh/ssh-key-2026-02-25.key ~/Downloads/mysql84-community-release-el9-3.noarch.rpm opc@158.101.109.115:~`
3. log in to your Linux instance and run the following commands:

```shell
$ export MYSQL_FILENAME=mysql84-community-release-el9-3.noarch.rpm
$ sudo yum localinstall ${MYSQL_FILENAME}
$ yum repolist enabled | grep mysql.*-community
$ sudo yum install mysql-community-server
```

This demonstration executes MySQL from within an OCI instance, so that it can be easily initialized/reinitialized.

Once your MySQL instance is running, you'll need to initialize it and create a user that can reach it from your 
development computer.

Our first steps are to

* launch MySQL
* acquire the auto-generated root password, 
* determine what the gateway IP address is, allowing us to configure MySQL to allow connections from it, and thus our Java code, and
* gain access to the MySQL SQL client.

```shell
% sudo firewall-cmd --permanent --zone=public --add-port=3306/tcp
success
% sudo firewall-cmd --reload
success
% sudo systemctl start mysqld
% sudo grep 'temporary password' /var/log/mysqld.log
% who   # note your inbound IP address
% mysql -u root -p
```

At this point, you're logged in (you copy/pasted the root password, right?).

Next we need to:

* change the root password
* inspect the list of users
* show existing databases, then create a database and verify it was created,
* create a user to access that database using a connection from the docker container's inbound gateway

```shell
mysql> alter user 'root'@'localhost' identified by 'My-New-R00t-Password';
mysql> select host, user, account_locked from mysql.user;
mysql> show databases;
mysql> create user jsc@172.17.0.1 identified by 'JSCuserPassword12-45';
mysql> create database jsc;
mysql> use jsc;
mysql> grant all privileges on *.* to jsc@172.17.0.1;
mysql> show databases;
mysql> select host, user, account_locked from mysql.user;
mysql> exit;
```

We need to retrieve the server's auto-generated CA certificate and package it for later use:

```shell
% cp /var/lib/mysql/ca.pem .
% keytool -importcert -alias mysqlcacert -file ca.pem -keystore truststore.pkcs12 -storepass MySQLtrustPass
```

If you want to view the truststore's content, you can:

```shell
% keytool -list  -keystore ./truststore.jks -v
```

This is all basic setup. Note that the my.cnf file we are using insists on an SSL connection, and auto-generates a self-signed
certificate in order to anchor the TLS connection. Our client will insist that this certificate be used, and we'll look at
using that self-signed CA cert later in the process as well.

Now we're ready to demonstrate some secure transport features using Java.

## Generate a database & view customer data

Our first step logs in to the newly-created database and stores some data into it using Flyway.

```shell
% mvn clean package
% java -cp ./target/mysql_encryption-1.0-SNAPSHOT.jar com.oracle.jsc.encrypt.BuildDB
```

BuildDB uses flyway to add a "customer" table and populate it with some potentially sensitive data.

Now let's go see if we can view the data. We log in to the MySQL container and cat the relevant
data file:

```shell
% sudo cd /var/lib/
% sudo cd mysql
% sudo strings jsc/customer.ibd
```
As you can see, names, addresses, and social security numbers are shown in plain text.

## Encrypt some of our data programmatically

Examine EncryptKeyData.java. It encrypts the SSN field on insert and decrypts it on retrieval, using a key
stored in "secrets.properties". Naturally, you'd normally use a secrets wallet implementation of some kind
to store sensitive information like keys, but for the purposes of this demo, we'll call that an 
"implementation detail".

Let's run this class, which retrieves each row, encrypts the contents, then re-stores it. Then we can
again look at the file to see what we've done.

```shell
# Build and store an encryption key
% java -cp ./target/mysql_encryption-1.0-SNAPSHOT.jar com.oracle.jsc.encrypt.Crypto

# Encrypt some data
% java -cp ./target/mysql_encryption-1.0-SNAPSHOT.jar com.oracle.jsc.encrypt.EncryptKeyData

# Now look at the raw database again:
% sudo strings /var/lib/mysql/jsc/customer
```

In this case, we have encrypted the stored social security numbers, which results in much harder-to-read
data in the event of a file system breach.  Retrieval requires that we decrypt the data on the fly, but
that's not a burden. Look at the source code of DecryptKeyData.java, then run it:

```shell
% java -cp ./target/mysql_encryption-1.0-SNAPSHOT.jar com.oracle.jsc.encrypt.DecryptKeyData
```

## Encrypt the entire database

First, let's drop our database tables; we can rebuild them later:

```shell
% java -cp ./target/mysql_encryption-1.0-SNAPSHOT.jar com.oracle.jsc.encrypt.UnbuildDb
```

MySQL encrypts tables individually, and configuring that is done through the installation of a plugin component.
There are 2 (or more) configuration files involved; we are going to set up a global configuration, but local ones
are also available. See the documentation. 

First, we need to add a component configuration file in the same directory as mysqld, which on Linux is /usr/sbin. That
file is named `mysqld.my`, and looks like the following:

```
{
  "components": "file://component_keyring_encrypted_file"
}
```

Note that despite the `file://` lead-in, this is *not* a file descriptor; it's a "magic word", so the file will always
contain exactly what is shown.

Then we need a component configuration, which is located in a file named `component_keyring_encrypted_file.cnf`, and that file
is located in the same directory as the plugin itself, which on my installation is `/usr/lib64/mysql/plugin`. File contents are
self-explanetory, and follow:

```
{
  "path": "/var/lib/mysql-keyring/component_keyring_encrypted_file.keys",
  "password": "Pa5sword~x",
  "read_only": false
}
```

**The 'path' element in this configuration does not match the documentation for this plugin--and only the path above actually
works out of the box.**
Other paths are useable, but you need to perform some system administration tasks to allow MySQL access to those file paths.

This file is owned by mysql:mysql, and has a file protection mod of 600: only mysql and read this file.

For further configuration details, look at
[https://dev.mysql.com/doc/refman/8.4/en/innodb-data-encryption.html](https://dev.mysql.com/doc/refman/8.4/en/innodb-data-encryption.html).

Now create the key file, restart MySQL, and turn on encryption.

```shell
% sudo mkdir --parents /usr/local/mysql/keyring
% sudo touch /var/lib/mysql-keyring/component_keyring_encrypted_file.keys
% sudo chown -R mysql:mysql /usr/local/mysql
% sudo chmod 600 /usr/local/mysql/keyring/component_keyring_encrypted_file
% sudo systemctl restart mysqld
% mysql -u root -p
```

```
> SET GLOBAL default_table_encryption=ON;
> alter database jsc encryption='Y';
> exit;
```

Finally, create the database, load data, and then read the on-disk content, noting that it's encrypted:

```shell
% java -cp ./target/mysql_encryption-1.0-SNAPSHOT.jar com.oracle.jsc.encrypt.BuildDB
% sudo strings /var/lib/mysql/jsc/customer.ibd
```

Our default JDBC configuration is controlled by the URL:

```
    private static String URL = "jdbc:mysql://150.136.77.155:3306/jsc?sslMode=REQUIRED";
```

MySQL will default to SSL, but it's nice to specify it.

Run the code. This makes 2 connections to the database. First, FlyWay connects using the URL, username, and password, and
sets up the database. Then our code instantiates a JDBC connector, connects to the database, and retrieves the contents
of the one table present in the DB (see `V1__create_database.sql`, which Flyway uses to set up the database).

As a basic next step, we can instruct the client to verify the CA (but not the server name). Change the SSL mode from
"REQUIRED" to "VERIFY_CA", and re-run the code. As you can see, this will fail, because the CA is self-signed and is
not part of Java' ds default set of CAs.

We can fix this by adding the CA certificate to Java's set of CAs, or by adding a trust store specification to the JDBC URL
as shown in the example code. Add the two properties "trustCertificateKeyStoreUrl", which identifies a trust store carrying
the CA certificate used by the server (but note: not the server cert), and "trustCertificateKeyStorePassword", which provides
the password to that trust store. Rerun the code, and you will see that the connection is now successful.

You want to ensure that the server you are connecting to is the one you expected. You do this by changing the SSL mode
from "VERIFY_CA" TO "VERIFY_IDENTITY". Run the code, and note the cause of failure: the server name does not match the 
certificate passed from the server, who's CN is MySQL_Server_9.5.0_Auto_Generated_Server_Certificate'. This does not
match the server name of '150.136.77.155'. You can remedy this by generating a more complete server cert. See the 
client_certs example. You can also 

### Some other notes on MySQL security
* Encryption at rest is free, but key management is not free (you can use file-based keys, but that's not terribly secure).
* The JDBC driver URL format can include multiple servers in a comma-separated list, which provides automatic failover
    (and thus, some protection against DoS).  E.g.: `jdbc:mysql://150.136.77.155:3306,150.136.77.156:3306/jsc?sslMode=REQUIRED`.
* The JDBC driver for MySQL includes OpenTelemetry support.

## shut down and remove the container
```shell
% docker stop mysql
% docker rm mysql
```
