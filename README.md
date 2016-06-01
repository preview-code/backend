# backend

Running the backend with ssl:

1. Go to `src/main/resources` in the terminal.
*  execute: `keytool -genkey -alias sitename -keyalg RSA -keystore keystore.jks -keysize 2048`
with
first and Last name: `localhost`,
fill the rest in as you wish
and save your password somewhere
*  `keytool -delete -alias sitename -keystore keystore.jks`
*  `keytool -list -keystore keystore.jks`
verify that the keytool has no entries.
*  Put all the certificates in your resource folder. The files are named `server.crt` and `server.key`.
When asked for a (new) password use the password from step 2.
*  `keytool -keystore keystore.jks -import -alias jetty -file server.crt -trustcacerts`
*  `openssl pkcs12 -inkey server.key -in server.crt -export -out server.pkcs12`
* `keytool -importkeystore -srckeystore server.pkcs12 -srcstoretype PKCS12 -destkeystore keystore.jks`
*  Rename`src/main/resources/jetty-ssl-context.xml.example` to `src/main/resources/jetty-ssl-context.xml` and put your password in it.
*  Ask a developer for the `firebase-auth.json` to put in the resource folder.
*  Start the server.
