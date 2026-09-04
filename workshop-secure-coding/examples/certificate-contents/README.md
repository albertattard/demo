# examine a valid certificate chain

```shell
$ cat www-ww-oracle.com-chain.pem
```

Note there are 3 certificates in the pem file--all in a binary format, represented in base64.

PEM is a binary format, compact, and commonly used on the web.

```shell
$ openssl x509 -noout -text -in www-ww-oracle-com-chain.pem
```
Note that only the first certificate is listed. To examine each certificate, they each need to be in their own file.

I've done this edit in the "chain" subdirectory.

```shell
$ openssl x509 -noout -text -in chain/one.pem
```
Things to note:

* The subject key identifier and the authority key identifiers are the same: this is self-signed.
* extensions-key usage includes "Digital Signature" and "Certificate Sign", so this can be used to sign certificates and to sign digital signatures, as well as CRLs (Certificate Revocation Lists).
* Basic constraints includes CA:TRUE--this is a certificate authority, allowing it to be used to sign other certificates

```shell
$ openssl x509 -noout -text -in chain/two.pem
```

```shell
$ openssl x509 -noout -text -in chain/three.pem
```

Let's examine the fields in detail.

...analysis here
...note validity, CN and alternate names
...subject and authority key identifiers
...why we care about the public keys
...how we validate the signature
