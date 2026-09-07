# Client Certificates for Authentication

In this demonstration, we will demonstrate certificate authentication for both ends of a service-to-service connection
over HTTPS. Service "a" offers a server certificate identifying itself, but does not require a client certificate to 
authenticate its users. Service "b" offers a server cert, but also requires that a client authenticate itself.

We are leveraging Spring Boot to create most of the machinery, to reduce the code to essentials, but this process is
similar regardless of how the it is implemented. Bare Java/JDK code, various frameworks such as Javalin, Quarkus, 
Micronaut, Helidon, and so on all support the same capabilities but require varying amounts of other code to implement
the basic HTTPS connection and object exchange.

The basic steps are:

1. create a certificate authority cert to serve as the authentication root
2. create certificates for 2 small "hello world" services.
3. package certificates into trust stores and key stores for use with the services.
4. configure the services to use the certificate stores
5. verify that calls to service "a" are rejected if a client can not validate the server's identity by

   a. calling service a without the CA certificate being available to curl, and noting failure
   
	b. calling service a <i>with</i> the CA certificate being available to curl, and noting success

6. verify that calls to service "b" are rejected if an appropriate client certificate is not presented.

    a. calling service b <i>with</i> the CA certificate being available to curl, and noting a 401 "not authorized" failure

    b. calling service b with a client certificate from service a, and noting success.

In a real world, the server can be accessed by multiple client applications, in which case, either

a. the Public Key and Certificate has to be added to the server's trust store as a different alias represented by their client. (or)
b. using a trusted CA to issue client certificates; a self-signed CA cert can serve this purpose inside a single enterprise.

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven
- [openssl](https://github.com/openssl/openssl/releases) to generate certificates (macs and linux boxes typically have this installed already)
- root access to the machine you are working on, so you can edit the hosts file to add names for "localhost"
- curl

## Establish hostnames

We're going to create 2 different hostnames for our servers, because certificate names need to match hostnames, and
we want to be sure we're clear on which service is getting which certificate. Caches might also
complain if we use different certs for 2 different ports on the same "host".

On Linux or MacOS, you can update "/etc/hosts"; the same file format works for Windows and the file is located at 
\Windows\System32\drivers\etc.  You'll have to edit as root or Administrator, as this file is write protected.

Insert a new row as follows:

```
127.0.0.1   uhost-a, uhost-b
```

This will make the DNS resolver resolve "uhost-a" and "uhost-b" to the IP4 address 127.0.0.1.

## Set up certificates

### Generate Certificates

There's nothing special about certificate authorities, beyond the fact that they are trusted. You can issue a signing certificate
yourself (indeed, we will do so in a moment), and if you have told your browser to trust that signing certificate, then anything
it is used to sign is also trusted.

First, generate a certificate authority self-signed root certificate as follows:

```shell
# Generate a "cacert.config" file which describes the fact that the certificate to be generated is a
# "CA" cert, with a "pathlen" of 1 (indicating that only certificates signed directly by this cert
# are valid. Higher pathlens allow for longer certificate chains).
#
>cacert.config cat <<-EOF
basicConstraints = critical,CA:true,pathlen:1
keyUsage = keyCertSign
EOF

# now generate a key public/private keypair using the elliptical algorithm "prime246v1"
openssl ecparam -out cacert.key -name prime256v1 -genkey

# generate CSR
#
# Use the private key from the keypair to generate a "certificate signing request" (CSR)
# The "distinguished name" identifies who the certificate belongs to. Fundamentally, certificates
# bind a digital signature to a "distinguished name". Note the "CN" at the end--this is the "Common name", by
# which the cert is usually known. You'll see it when you import this as a root cert. Other components of the
# distingushed name are: C->Country, ST->state, L->Locale, O->Organizatoin, OU->Organization unit or
# department, emailAddress.
#
$ export DNAME="/C=US/ST=OR/L=Corvallis/O=Oracle/OU=Java_Sales/emailAddress=jerry.a.andrews@oracle.com/CN=JSC_root"
DNAME="/C=US/ST=OR/L=Corvallis/O=Oracle/OU=Java_Sales/emailAddress=jerry.a.andrews@oracle.com/CN=JSC_root"
openssl req -new -sha256 -key cacert.key -out cacert.csr -subj $DNAME

# sign the root cert
#
# We sign the root certificate with itself. This is the head of the chain; we now have a 
# Certificate Authority cert that we can import into the browser. The browser will trust any certificate
# signed by this root certificate
openssl x509 -req -sha256 -days 3650 -in cacert.csr -signkey cacert.key -out cacert.crt -extfile cacert.config

# create the trust store for use by the servers to authenticate clients of this root cert
keytool -importcert -storetype PKCS12 -keystore ca-truststore.p12 -storepass ca-truststore-pass -alias rootCert -file cacert.crt -noprompt

```

Now we do the same set of steps, this time using the new CA cert to sign a server certificate for our
two microservices. Change to the uservice_a directory, then:

```shell
# generate an eliptic-curve server key

openssl ecparam -out service-a.key -name prime256v1 -genkey

# generate a signing request for the server key

DNAME="/C=US/ST=CA/L=Redwood_Shores/O=Oracle/OU=Linux_Sales/emailAddress=albert.attard@oracle.com/CN=127.0.0.1/"
openssl req -new -sha256 -key service-a.key -out service-a.csr -subj $DNAME

# create the configuration file for certificate generation

>service-a.ext cat <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names
[alt_names]
DNS.1 = uhost-a # Be sure to include the domain name here because Common Name is not so commonly honoured by itself
IP.1 = 10.0.0.174 # Optionally, add an IP address (if the connection which you have planned requires it)
IP.2 = 127.0.0.1
EOF

# sign it
openssl x509 -req -days 3650 -CA ../cacert.crt -CAkey ../cacert.key -set_serial 001 -in service-a.csr -out service-a.crt -sha256 -extfile service-a.ext

# convert it to p12, creating a keystore
openssl pkcs12 -export -name service-a -out service-a.p12 -inkey service-a.key -in service-a.crt -passout pass:service_a-password

# display the contents of the keystore
keytool -list -v -keystore service-a.p12 <<< "service_a-password"

```
Then repeat the process in uservice_b's directory.

### Examine the client configuration for services "a" and "b"

Open application.yaml in service "a" an note that we have defined strings which point to the CA trust store
and the the server's client store.

Both services emit a json representation of the `Hello` record.

The `HelloController` defines the context path of `/hello` to do that. In addition, service "a" calls service "b",
parses the result, and returns that as well in its response.

`HttpServer` is processed automatically by the Spring framework to establish an HTTPS port using the defined
key store certificate from the configuration as its certificate. Note that both services offer both HTTP
and HTTPS, but service B puts these on ports 81 and 444 so that both services can run on the same laptop.

`ServiceProperties` is Spring's default way of managing dynamically-managed properties. In our case, we only
use it for the "Hello" message contents, which are static, but this is how a dynamically-managed property
would be managed. 

`SslConfig` generates our web client and an associated SSL Context which includes the server cert as the
identity certificate for outbound HTTPS requests. In Spring Boot, static properties are usually autoinjected
as shown in this class.

### Build and deploy the services

```shell
% cd uservice_a
% mvn spring-boot:run &
% cd ../uservice_b
% mvn spring-boot:run &
```

## Demonstration

```shell
% curl https://uhost-a/hello
```

Note that the connection fails because our self-signed root cert isn't trusted by curl. Now provide the CA cert to curl
so it trusts that uhost-a is who it says it is.

```shell
% curl --cacert ./cacert.crt https://uhost-a/hello
```

Now we get the expected result: a hello from service a.  Try that with service b:

```shell
% curl --cacert ./cacert.crt https://uhost-b:444/hello
```

You'll get "401--not authorized". Service b requires a client certificate.

Service a supplies such a certificate when it calls service b. Try that:

```shell
% curl --cacert ./cacert.crt https://uhost-a/hello_b
```

We succeeded!  And "b" emitted the cert it got from "a" to its log. As you can see from the source code, here's where we
would validate the certificate.  As shown in "b"'s configuration, Spring can do that automatically for you if you like.


