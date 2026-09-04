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