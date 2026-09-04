# generate an eliptic-curve server key

openssl ecparam -out service-a.key -name prime256v1 -genkey

# generate a signing request for the server key

DNAME="/C=US/ST=CA/L=Redwood_Shores/O=Oracle/OU=Linux_Sales/emailAddress=albert.attard@oracle.com/CN=uService-A/"
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
