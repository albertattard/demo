# generate an eliptic-curve server key
openssl ecparam -out server.key -name prime256v1 -genkey

# generate a signing request for the server key
DNAME="/C=US/ST=CA/L=Redwood_Shores/O=Oracle/OU=Linux_Sales/emailAddress=albert.attard@oracle.com/CN=127.0.0.1/"
openssl req -new -sha256 -key server.key -out server.csr -subj $DNAME

>server.ext cat <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names
[alt_names]
DNS.1 = myhost # Be sure to include the domain name here because Common Name is not so commonly honoured by itself
IP.1 = 10.0.0.174 # Optionally, add an IP address (if the connection which you have planned requires it)
IP.2 = 127.0.0.1
EOF

# sign it
openssl x509 -req -days 3650 -CA ../cacert.crt -CAkey ../cacert.key -set_serial 001 -in server.csr -out server.crt -sha256 -extfile server.ext

# convert it to p12, creating a keystore
openssl pkcs12 -export -name server -out server.p12 -inkey server.key -in server.crt -passout pass:mitm-server-password

# display the contents of the keystore
keytool -list -v -keystore server.p12 <<< "mitm-server-password"
