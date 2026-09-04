# Generate Certificates

Certificates bind a "distinguished name" to a digital signature. The keytool "-dname" option provides the distinguished name
to be bound to our signature. openssl uses "subject" ("-subj") to set the dname.

    cn: common name
    ou: Organizational unit
    o: Organization
    c: country
    st: state

    -nodes : don't encrypt the private key (deprecated)
    -new : new request
    -newkey :  generate a new key using a 2048-bit RSA algorithm
    -x509 : generate an x509 certificate instead of a cert request
    -extensions : include the cited extension
    -keyout : output file for the key
    -out : output file for the cert
    -days : cert validity period
    -subj : cert distinguished name

openssl req -help for a full list of options

## Generate the root cert

Here's one way to generate the root cert and private key all at once:

```shell
# generate root cert and private key
$ DNAME="/C=US/ST=OR/L=Corvallis/O=Oracle/OU=Java_Sales/emailAddress=jerry.a.andrews@oracle.com/"
$ openssl req -nodes -new -newkey rsa:2048 -x509 -extensions v3_ca -keyout cakey.pem -out cacert.crt -days 3650 -subj $DNAME
```

For clarity, though, we're going to do each step separately:

```shell
# generate root cert and private key

>cacert.config cat <<-EOF
basicConstraints = critical,CA:true,pathlen:1
keyUsage = keyCertSign
EOF

# generate key
openssl ecparam -out cacert.key -name prime256v1 -genkey

# generate CSR
DNAME="/C=US/ST=OR/L=Corvallis/O=Oracle/OU=Java_Sales/emailAddress=jerry.a.andrews@oracle.com/CN=JSC_root"
openssl req -new -sha256 -key cacert.key -out cacert.csr -subj $DNAME

# sign the root cert
openssl x509 -req -sha256 -days 3650 -in cacert.csr -signkey cacert.key -out cacert.crt -extfile cacert.config
```

## Generate the server cert

Build the server certificate signing request

```shell
# generate an eliptic-curve server key
openssl ecparam -out server.key -name prime256v1 -genkey

# generate a signing request for the server key
DNAME="/C=US/ST=CA/L=Redwood_Shores/O=Oracle/OU=Linux_Sales/emailAddress=albert.attard@oracle.com/CN=127.0.0.1/"
openssl req -new -sha256 -key server.key -out server.csr -subj $DNAME

# and describe the certificate details in a configuration file
>server.config cat <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names
[alt_names]
DNS.1 = myhost # Be sure to include the domain name here because Common Name is not so commonly honoured by itself
IP.1 = 10.0.0.174 # Optionally, add an IP address (if the connection which you have planned requires it)
IP.2 = 127.0.0.1
EOF
```

Now we sign the server certificate with our CA certificate.

```shell
$ openssl x509 -req -days 3650 -CA ../cacert.crt -CAkey ../cacert.key -set_serial 001 -in server.csr -out server.crt -sha256 -extfile server.config
```

## Verify our certificate chain

Let's verify our certificate chain.

```shell
$ openssl verify -show_chain -CAfile cacert.crt server.crt

```

Let's see if we can fool the verifier. First, we extract a root cert from the Java cert store.

```shell
# We can specify the cacerts file directly, but keytool will warn us:

# $ keytool -export -alias "amazonrootca1 [jdk]" -keystore $JAVA_HOME/lib/security/cacerts -rfc -file amazon_root_cert_1.crt
# Warning: use -cacerts option to access cacerts keystore
# ...

# better to use the -cacerts flag, which is a shortcut to the CA certs keystore

$ keytool -export -alias "amazonrootca1 [jdk]" -cacerts -rfc -file amazon_root_cert_1.crt
```

Then run our verify again. Note that our "cacert.crt" is NOT signed by our root cert.

```shell
$ openssl verify -show_chain -CAfile amazon_root_cert_1.crt -untrusted cacert.crt server.crt

```

cleanup...

```shell
rm *.crt *.pem *.csr *.key
```