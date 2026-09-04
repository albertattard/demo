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
