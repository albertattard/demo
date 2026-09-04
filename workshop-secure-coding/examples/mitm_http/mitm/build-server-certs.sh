# generate an eliptic-curve server key
openssl ecparam -out mitm.key -name prime256v1 -genkey

# generate a signing request for the server key
DNAME="/C=US/ST=CA/L=Redwood_Shores/O=Oracle/OU=Blackhat/emailAddress=jerry@jrandrews.org/CN=127.0.0.1/"
openssl req -new -sha256 -key mitm.key -out mitm.csr -subj $DNAME

>mitm.ext cat <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names
[alt_names]
DNS.1 = myh0st # Be sure to include the domain name here because Common Name is not so commonly honoured by itself
IP.1 = 10.0.0.174 # Optionally, add an IP address (if the connection which you have planned requires it)
IP.2 = 127.0.0.1
EOF

# sign it
openssl x509 -req -days 3650 -CA ../cacert.crt -CAkey ../cacert.key -set_serial 002 -in mitm.csr -out mitm.crt -sha256 -extfile mitm.ext

# convert it to p12, creating a keystore
openssl pkcs12 -export -name mitm -out mitm.p12 -inkey mitm.key -in mitm.crt -passout pass:mitm-mitm-password

# display the contents of the keystore
keytool -list -v -keystore mitm.p12 <<< "mitm-mitm-password"

# package the CA cert for outbound HTTPS requests
keytool -import -trustcacerts -file ../cacert.crt -storepass mitm-mitm-password -keystore catruststore.jks
