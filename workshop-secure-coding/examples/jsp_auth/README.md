# Data Leakage via JSP Whitespace

JSP files can leak information inadvertently, through whitespace. Here's an example

## Prerequisites

- a recent version of Java -- this was tested with - [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- Maven
- Tomcat -- this was tested with [Tomcat 11](https://tomcat.apache.org/download-11.cgi)

## Configure your environment

Unpack your Tomcat distribution somewhere. From now on, we will refer to the Tomcat root directory as CATALINA_HOME.

Navigate to CATALINA_HOME/conf. The following actions will back up tomcat-users.xml, and append 2 lines:

```shell
% cp tomcat-users.xml tomcat-users.xml.bak
% cat >> tomcat-users.xml << EOF
  <role rolename="loginUser"/>
  <user username="Jerry" password="testing" roles="loginUser"/>
EOF
```

This defines a new role, `loginUser`, and a case-sensitive username and password.

Navigate to CATALINA_HOME, then define an appropriate environment variable, and update the Tomcat scripts so they are executable:

```shell
% export CATALINA_HOME=`pwd`
% chmod +x ${CATALINA_HOME}/bin/*.sh
```

Now you can start your Tomcat server:

```shell
%  ${CATALINA_HOME}/bin/startup.sh
```

Tomcat will start up.

Open a browser and verify that Tomcat is running:

```shell
% open http://localhost:8080
```

Navigate to this source directory, build and deploy the web app:

```shell
% mvn clean package
% cp target/ROOT.war $CATALINA_HOME/webapp
```

## Demo

Browse to your new web app by refreshing the browser page we opened before. You'll be greeted with a login page.

Remember that we defined a username and password above--you should try it now--case sensitive, username is 'Jerry' and the
password is 'testing'. You should see the "Hello" page with a logout button. Try the logout.

The interesting thing happens when login _fails_. Log out, then put in a random username and password. You'll get the login
failure as expected. "View source" on the error page--you'll see it has two blocks of whitespace 4 lines at the top, and
3 lines above the paragraph block. 

Now "try again"--but this time, use the known username "Jerry", but make up an incorrect password. Login still fails, but if
you "view source" on the error page, you'll see that the second text block has 5 lines -- 2 more than it did previously. To an
attacker, this is an important clue. Examine the loginError.jsp file to see why there are 2 extra lines in the output.

## Cleanup

```shell
% $CATALINA_HOME/bin/shutdown.sh
```