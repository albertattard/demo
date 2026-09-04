# generate an eliptic-curve server key
openssl ecparam -out service-b.key -name prime256v1 -genkey

# generate a signing request for the server key
DNAME="/C=US/ST=CA/L=Redwood_Shores/O=Oracle/OU=Linux_Sales/emailAddress=albert.attard@oracle.com/CN=uService-B/"
openssl req -new -sha256 -key service-b.key -out service-b.csr -subj $DNAME

>service-b.ext cat <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names
[alt_names]
DNS.1 = uhost-b # Be sure to include the domain name here because Common Name is not so commonly honoured by itself
IP.1 = 10.0.0.174 # Optionally, add an IP address (if the connection which you have planned requires it)
IP.2 = 127.0.0.1
EOF

# sign it
openssl x509 -req -days 3650 -CA ../cacert.crt -CAkey ../cacert.key -set_serial 001 -in service-b.csr -out service-b.crt -sha256 -extfile service-b.ext

# convert it to p12, creating a keystore
openssl pkcs12 -export -name service-b -out service-b.p12 -inkey service-b.key -in service-b.crt -passout pass:service_b-password

# display the contents of the keystore
keytool -list -v -keystore service-b.p12 <<< "service_b-password"
